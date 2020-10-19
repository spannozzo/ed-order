package org.acme.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.validation.constraints.NotBlank;

import org.acme.dto.OrderDTO;
import org.acme.entity.Order;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;

import io.quarkus.runtime.StartupEvent;

/**
 * used for handling data when 
 * database is not responding
 * 
 *
 */
@ApplicationScoped
public class FallBackRepository {
	
	private List<Order> orderList;
	boolean falling;
	
	public List<Order> getOrderList() {
		return orderList;
	}
	
	void init() {
		orderList=new ArrayList<>();
		falling=false;
		
		loadFromDb();
	}

	void onStart(@Observes StartupEvent ev) {
       init();
    }

	@Fallback (fallbackMethod = "fallbackLoadFromDB")
    @CircuitBreaker(requestVolumeThreshold=2,
                    failureRatio=0.5,
                    delay=5000)
	void loadFromDb() {
		orderList=Order.listAll();	
	}
	void fallbackLoadFromDB() {
		falling=false;
	}
	
	public boolean wasFalling() {
		return falling;
	}

	public void setFalling(boolean falling) {
		this.falling = falling;
	}

	public Optional<List<Order>> getOrders(){
		if (orderList.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(orderList);
	}

	public Optional<Order> findById(@NotBlank String id) {
		return orderList.parallelStream().filter(order->order.id.equals(id)).findFirst();
	}

	public synchronized void delete(Order toDelete) {
		orderList=orderList.parallelStream().filter(order->!order.id.equals(toDelete.id)).collect(Collectors.toList());
	}

	public synchronized void edit(Order order, OrderDTO editRequestDTO) {
	
		orderList.parallelStream().map(o->{
			if (o.id.equals(order.id)) {
				boolean isSameStatus=o.status.equals(editRequestDTO.getStatus());
				boolean isSameAmount=o.totalAmount.equals(editRequestDTO.getTotalAmount());
						
				if (!isSameStatus) {
					o.status=editRequestDTO.getStatus();
				}
				if (!isSameAmount) {
					o.totalAmount=editRequestDTO.getTotalAmount();
				}
				
				o.upadtedAt=LocalDateTime.ofInstant(Instant.now(), ZoneOffset.systemDefault());
			}
			return o;
		}).collect(Collectors.toList());
	
	}

	public synchronized void save(Order order) {
		
		order.createdAt=LocalDateTime.ofInstant(Instant.now(), ZoneOffset.systemDefault());		
		orderList.add(order);		
	}
	
}
