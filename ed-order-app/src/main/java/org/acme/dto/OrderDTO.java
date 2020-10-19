package org.acme.dto;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;

import org.acme.entity.Order;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="Order DTO", description="Return information about an order")
public class OrderDTO extends OrderRequestDTO{

	@Schema(title = "Id Order", required = true, example = "3777616f-a80e-4b24-a20f-55e947a81b07")
	@NotBlank
	String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
	public int hashCode() {
		return 0;
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
