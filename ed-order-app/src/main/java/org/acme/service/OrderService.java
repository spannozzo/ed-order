package org.acme.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.acme.dto.OrderDTO;
import org.acme.dto.OrderRequestDTO;

@Singleton
public class OrderService {

	static List<OrderDTO> orders=new ArrayList<>();
	
	public Optional<List<OrderDTO>> getOrders() {
		
		if (orders.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(orders);
	}
	
	public @Valid OrderDTO saveOrder(@NotNull OrderRequestDTO orderRequestDTO) {
		String id=UUID.randomUUID().toString();
	
		OrderDTO order=new OrderDTO(id, orderRequestDTO.getStatus(), orderRequestDTO.getTotalAmount());
		
		orders.add(order);
		
		return order;
		
	}
	
	public Optional<OrderDTO> findById(@NotBlank String id) {
		
		return orders.parallelStream().filter(order->order.getId().equals(id)).findFirst();
	}
	
	public OrderDTO delete(OrderDTO toDelete) {
		orders=orders.parallelStream().filter(order -> !order.equals(toDelete)).collect(Collectors.toList());
		
		return new OrderDTO(toDelete.getId(), "DELETED", toDelete.getTotalAmount());
	}
	
	public @Valid OrderDTO edit(OrderDTO toEdit) {
				
		orders.parallelStream().forEach(order->{
			if (order.equals(toEdit)) {
				
				this.patch(order,toEdit);
				return;
			}
		});
		
		return toEdit;
	}

	void patch(OrderDTO toEdit, OrderDTO newValues) {
		if (newValues.getStatus()!=null && !newValues.getStatus().isEmpty()) {
			toEdit.setStatus(newValues.getStatus());
		}
		if (newValues.getTotalAmount()!=null && newValues.getTotalAmount()>=0) {
			toEdit.setTotalAmount(newValues.getTotalAmount());
		}
	}
	
}
