package org.acme.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "order_table")
public class Order extends PanacheEntityBase{

	@Id
	public String id;
	
	public String status;
	
	public Double totalAmount;
	
	public LocalDateTime createdAt;
//	
	public LocalDateTime upadtedAt;

	public Order() {
	}
	
	public Order(String id, String status, Double totalAmount) {
		super();
		this.id = id;
		this.status = status;
		this.totalAmount = totalAmount;
	}
	
}
