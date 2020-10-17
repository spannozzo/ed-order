package org.acme.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.acme.dto.OrderDTO;
import org.acme.dto.OrderRequestDTO;
import org.acme.entity.Order;
import org.acme.exceptions.NotModifiedException;
import org.acme.repository.OrderRepository;

@Singleton
public class OrderService {

	@Inject
	OrderRepository orderRepository;
	
	public Optional<List<Order>> getOrders() {
		
		return orderRepository.getOrders();
	}
	
	public @Valid Order saveOrder(@NotNull OrderRequestDTO orderRequestDTO) {
		String id=UUID.randomUUID().toString();
	
		Order order=new Order(id, orderRequestDTO.getStatus(), orderRequestDTO.getTotalAmount());
		
		orderRepository.save(order);
		
		return order;
				
	}
	
	public Optional<Order> findById(@NotBlank String id) {
		
		return orderRepository.findById(id);
	}
	
	public void delete(Order toDelete) {
		orderRepository.delete(toDelete);
	}
	
	public void edit(@NotNull Order order,@NotNull OrderDTO editRequestDTO) { 
  
		boolean isSameStatus=order.status.equals(editRequestDTO.getStatus());
		boolean isSameAmount=order.totalAmount.equals(editRequestDTO.getTotalAmount());
		
		if (isSameStatus && isSameAmount) {
			throw new NotModifiedException("Nothing to modify");
		}
		
		if (!isSameAmount) {
			order.status=editRequestDTO.getStatus();
		}
		
		orderRepository.edit(order,editRequestDTO);
	}
	
}
