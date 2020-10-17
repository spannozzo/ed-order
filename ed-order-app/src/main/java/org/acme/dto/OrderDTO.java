package org.acme.dto;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

import org.acme.entity.Order;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="Order DTO", description="Return information about an order")
public class OrderDTO {

	@Schema(title = "Id Order", required = true, example = "3777616f-a80e-4b24-a20f-55e947a81b07")
	@NotBlank
	String id;
	
	@Schema(title = "Status", required = true, example = "Dispatched")
	@NotBlank
	String status;
	
	@Schema(title = "Order Total Amount", required = true, example = "99.99")
	@PositiveOrZero
	Double totalAmount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Override
	public String toString() {
		return "OrderDTO [id=" + id + ", status=" + status + ", totalAmount=" + totalAmount + "]";
	}

	public OrderDTO() {}
	
	public OrderDTO(String id, String status, Double totalAmount) {
		super();
		this.id = id;
		this.status = status;
		this.totalAmount = totalAmount;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OrderDTO) {
			return ((OrderDTO) obj).getId().equals(this.getId());
		}
		return false;
	}

	public static List<OrderDTO> map(List<Order> list) {
		
		return list
				.parallelStream()
				.map(OrderDTO::fromEntityToDTO)
				.collect(Collectors.toList())
			
				
				;
		
	}
	public static OrderDTO fromEntityToDTO(Order order) {
		return new OrderDTO(order.id,order.status,order.totalAmount);
	}

	public static OrderDTO getDeleted(Order order) {
		return new OrderDTO(order.id,"DELETED",order.totalAmount);

	}

	public static OrderDTO patch(Order toEdit, OrderDTO newValues) {
		if (newValues.getStatus()!=null && !newValues.getStatus().isEmpty()) {
			toEdit.status=newValues.getStatus();
		}
		if (newValues.getTotalAmount()!=null && newValues.getTotalAmount()>=0) {
			toEdit.totalAmount=newValues.getTotalAmount();
		}
		return fromEntityToDTO(toEdit);
	}
	
}
