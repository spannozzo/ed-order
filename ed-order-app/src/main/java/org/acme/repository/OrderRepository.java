package org.acme.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;

import org.acme.dto.OrderDTO;
import org.acme.entity.Order;

@Singleton
public class OrderRepository {

	@Inject
	EntityManager em;
	
	public Optional<List<Order>> getOrders() {
		List<Order> orders=Order.listAll();
		if (orders.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(orders);
		
		
	}

	public Optional<Order> findById(@NotBlank String id) {
		
		Optional<Order> list=Order.findByIdOptional(id);
		
		return list;
	}
	
	@Transactional
	public void delete(Order toDelete) {		
		toDelete = em.merge(toDelete);
		em.remove(toDelete);
		
	}

	@Transactional
	public void edit(Order order, OrderDTO editRequestDTO) {

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
	}

	@Transactional
	public void save(Order order) {
		
		order.createdAt=LocalDateTime.ofInstant(Instant.now(), ZoneOffset.systemDefault());
		order.persist();		
	}
	
}
