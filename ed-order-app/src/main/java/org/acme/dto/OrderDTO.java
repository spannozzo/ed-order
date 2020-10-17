package org.acme.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

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
	
	
}
