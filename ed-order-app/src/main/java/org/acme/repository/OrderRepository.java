package org.acme.repository;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.core.Response;

import org.acme.dto.OrderDTO;
import org.acme.entity.Order;
import org.acme.health.HealtCheckRestClient;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;



@Singleton
@ActivateRequestContext
//@Timeout(value = 10000)
@CircuitBreaker(requestVolumeThreshold=2,failureRatio=0.5,delay=5000,successThreshold = 2)
public class OrderRepository {

	@Inject
	EntityManager em;
	
	@Inject
	FallBackRepository staticRepository;
	
	 private static final HttpClient httpClient = HttpClient.newBuilder()
	            .version(HttpClient.Version.HTTP_1_1)
	            .connectTimeout(Duration.ofSeconds(2))
	            .build();
	
	@Fallback (fallbackMethod = "fallbackGetOrders")
	public Optional<List<Order>> getOrders() {
		
		synchStaticListWithDB();
		
		List<Order> orders=Order.listAll();
				
		if (orders.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(orders);
	}
	
	Optional<List<Order>> fallbackGetOrders() {
		staticRepository.setFalling(true);
		return staticRepository.getOrders();
	}
	
	@Fallback (fallbackMethod = "fallbackFindById")
	public Optional<Order> findById(@NotBlank String id) {
		
		synchStaticListWithDB();
		
		Optional<Order> list=Order.findByIdOptional(id);

		return list;
	}
	Optional<Order> fallbackFindById(@NotBlank String id){
		staticRepository.setFalling(true);
		return staticRepository.findById(id);
	}
	
	@Fallback (fallbackMethod = "fallbackDelete")
	@Transactional
	public void delete(Order toDelete) {		
		synchStaticListWithDB();
		
		toDelete = em.merge(toDelete);
		em.remove(toDelete);
		
		syncStaticListDeleteOk(toDelete);
	}
	
	public void fallbackDelete(Order toDelete) {
		staticRepository.setFalling(true);
		staticRepository.delete(toDelete);
	}

	@Fallback (fallbackMethod = "fallbackEdit")
	@Transactional
	public void edit(Order order, OrderDTO editRequestDTO) {
		
		synchStaticListWithDB();
		
		boolean isSameStatus=order.status.equals(editRequestDTO.getStatus());
		boolean isSameAmount=order.totalAmount.equals(editRequestDTO.getTotalAmount());
				
		if (!isSameStatus) {
			order.status=editRequestDTO.getStatus();
		}
		if (!isSameAmount) {
			order.totalAmount=editRequestDTO.getTotalAmount();
		}
		
		order.upadtedAt=LocalDateTime.ofInstant(Instant.now(), ZoneOffset.systemDefault());

		
		em.merge(order);
		
		syncStaticListEditOk(order,editRequestDTO);
	}
	
	public void fallbackEdit(Order order, OrderDTO editRequestDTO) {
		staticRepository.setFalling(true);
		staticRepository.edit(order,editRequestDTO);
	}

	@Fallback (fallbackMethod = "fallbackSave")
	@Transactional
	public void save(Order order) {
		synchStaticListWithDB();
		
		order.createdAt=LocalDateTime.ofInstant(Instant.now(), ZoneOffset.systemDefault());
		
		em.merge(order);
			
		syncStaticListSaveOk(order);
	}
	
	public void fallbackSave(Order order) {
		staticRepository.setFalling(true);
		staticRepository.save(order);
	}
	
	@Fallback (fallbackMethod = "fallbackAllignDB")
	@Transactional
	void allignDB() {
		
		staticRepository.getOrderList().stream().forEach(order->{
			
			Optional<Order> maybeIsStored=Order.findByIdOptional(order.id);
			
			if(maybeIsStored.isPresent()) {
				Order storedOrder=maybeIsStored.get();
				
				boolean isSameStatus=order.status.equals(storedOrder.status);
				boolean isSameAmount=order.totalAmount.equals(storedOrder.totalAmount);
						
				if (!isSameStatus || !isSameAmount) {
					em.merge(order);
				}
			}
			else {
				em.merge(order);
			}
		});
		staticRepository.setFalling(false);
	}
	
	public void fallbackAllignDB() {
		staticRepository.setFalling(true);
	}
//	@Timeout(value = 200000)
	void synchStaticListWithDB() {
		if (staticRepository.wasFalling()) {
			HttpRequest request = HttpRequest.newBuilder()
	                .GET()
	                .uri(URI.create("http://localhost:8080/health/ready"))
	                .setHeader("User-Agent", "Java 11 HttpClient") // add request header
	                .build();
			
	        try {
				HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				
				if (response.statusCode()==HttpStatus.SC_OK) {
					allignDB();
					
				}
				if (response.statusCode()==HttpStatus.SC_SERVICE_UNAVAILABLE) {
					JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
					JsonObject jsonResponse=jsonReader.readObject();
					
					JsonArray checks=jsonResponse.getJsonArray("checks");
					
					Optional<JsonValue> dbHealtCheckOptional=checks.parallelStream().
												filter(json->json.asJsonObject().getString("name").equals("Database connections health check"))
												.findFirst();
					if (dbHealtCheckOptional.isPresent()) {
						if(dbHealtCheckOptional.get().asJsonObject().getString("status").equals("UP")) {
							allignDB();
						}
					}
					
					System.out.println(jsonResponse);
				}
				
				
				
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	private void disableStaticFallBack() {
		staticRepository.setFalling(false);
	}
	private void syncStaticListDeleteOk(Order toDelete) {
		staticRepository.setFalling(false);
		staticRepository.delete(toDelete);
	}
	private void syncStaticListSaveOk(Order order) {
		staticRepository.setFalling(false);
		staticRepository.save(order);
	}
	private void syncStaticListEditOk(Order order, OrderDTO editRequestDTO) {
		staticRepository.setFalling(false);
		staticRepository.edit(order, editRequestDTO);
	}
}
