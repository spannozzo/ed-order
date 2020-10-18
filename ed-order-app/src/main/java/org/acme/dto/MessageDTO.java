package org.acme.dto;

import java.time.LocalDateTime;

import org.acme.entity.Order;

public class MessageDTO {
	String id;
	String oldStatus;
	String newStatus;
	Double totalAmount;
	
	LocalDateTime createdAt;
	
	LocalDateTime upadtedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOldStatus() {
		return oldStatus;
	}

	public void setOldStatus(String oldStatus) {
		this.oldStatus = oldStatus;
	}

	public String getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(String newStatus) {
		this.newStatus = newStatus;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpadtedAt() {
		return upadtedAt;
	}

	public void setUpadtedAt(LocalDateTime upadtedAt) {
		this.upadtedAt = upadtedAt;
	}

	public MessageDTO(String id, String oldStatus, String newStatus, Double totalAmount, LocalDateTime createdAt,
			LocalDateTime upadtedAt) {
		super();
		this.id = id;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
		this.totalAmount = totalAmount;
		this.createdAt = createdAt;
		this.upadtedAt = upadtedAt;
	}


	public static MessageDTO fromOrderToMessage(String oldStatus, Order updatedOrder) {
		return new MessageDTO(updatedOrder.id, oldStatus, 
								updatedOrder.status, updatedOrder.totalAmount, 
								updatedOrder.createdAt, updatedOrder.upadtedAt
							);
	}
	
	
	
}
