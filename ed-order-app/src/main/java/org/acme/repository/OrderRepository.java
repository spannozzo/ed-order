package org.acme.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import org.acme.dto.OrderDTO;
import org.acme.entity.Order;
import org.acme.health.HealtCheckService;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;



@Singleton
@ActivateRequestContext
@CircuitBreaker(requestVolumeThreshold=2,failureRatio=0.5,delay=5000,successThreshold = 2)
public class OrderRepository {

	@Inject
	EntityManager em;
	
	@Inject
	FallBackRepository staticRepository;

	@Inject
	private HealtCheckService healtCheckService;
		
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
		
		return Order.findByIdOptional(id);

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

	@Fallback (fallbackMethod = "fallbackAllignDB")
	void synchStaticListWithDB() {
		if (staticRepository.wasFalling() &&
				healtCheckService.isDbUp()) {
			allignDB();
		}
	}
	
	private void syncStaticListDeleteOk(Order toDelete) {
		staticRepository.delete(toDelete);
	}
	private void syncStaticListSaveOk(Order order) {
		staticRepository.save(order);
	}
	private void syncStaticListEditOk(Order order, OrderDTO editRequestDTO) {
		staticRepository.edit(order, editRequestDTO);
	}
}
