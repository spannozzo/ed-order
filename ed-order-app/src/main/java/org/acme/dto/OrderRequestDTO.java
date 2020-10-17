package org.acme.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="Order request DTO", description="map the request on an order")
public class OrderRequestDTO {

		
	@Schema(title = "Status", required = true, example = "Dispatched")
	@NotBlank
	String status;
	
	@Schema(title = "Order Total Amount", required = true, example = "99.99")
	@PositiveOrZero
	Double totalAmount;

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
		return "OrderDTO [status=" + status + ", totalAmount=" + totalAmount + "]";
	}

	public OrderRequestDTO() {}
	
	public OrderRequestDTO(String status, Double totalAmount) {
		super();
	
		this.status = status;
		this.totalAmount = totalAmount;
	}

	
	
}
