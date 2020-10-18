package org.acme.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.dto.MessageDTO;
import org.acme.dto.OrderDTO;
import org.acme.dto.OrderRequestDTO;
import org.acme.entity.Order;
import org.acme.service.OrderService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.quarkus.security.Authenticated;

@RequestScoped
@Path("/orders")
@Authenticated
@SecurityScheme(securitySchemeName = "oauth2", type = SecuritySchemeType.HTTP, scheme = "Bearer")
public class OrderController {

	@Inject
	OrderService orderService;
	
	@Inject 
	@Channel("m2") 
	Emitter<MessageDTO> messageEmitter;

	@RolesAllowed({"user","admin"})
	@SecurityRequirement(name = "oauth2")
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "get list of orders")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "List of Orders", content = {
			@Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = OrderDTO.class))

			}), @APIResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
			@APIResponse(responseCode = "404", description = "Couldn't find any order", content = @Content)

	})
	public Response getOrders() {

		Optional<List<Order>> maybeOrders=orderService.getOrders();
				
		if (maybeOrders.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		return Response.status(Response.Status.OK).entity(OrderDTO.map(maybeOrders.get())).build();
		

	}
	@RolesAllowed({"admin"})
	@SecurityRequirement(name = "oauth2")
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "save an order")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Order created", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDTO.class))

			}), @APIResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
			@APIResponse(responseCode = "400", description = "Bad request", content = @Content)

	})
	public Response saveOrder(
			@Parameter(
		            description = "Json rapresentation of the order to insert",
		            required = true,
		            schema = @Schema(implementation = OrderDTO.class))
			
			@Valid OrderRequestDTO orderRequestDTO){

		
		Order savedOrder=orderService.saveOrder(orderRequestDTO);
		
		return Response.status(Response.Status.CREATED).entity(OrderDTO.fromEntityToDTO(savedOrder)).build();

	}
	
	@RolesAllowed({"user","admin"})
	@SecurityRequirement(name = "oauth2")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	@Operation(summary = "get one order from its id")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Order rapresentation", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDTO.class))

			}), @APIResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
			@APIResponse(responseCode = "404", description = "Couldn't find any order", content = @Content)

	})
	public Response getOrder(
			@Parameter(
		            description = "ID of the order",
		            required = true,
		            example = "3777616f-a80e-4b24-a20f-55e947a81b07",
		            schema = @Schema(type = SchemaType.STRING))
			
			@PathParam(value = "id") @NotBlank String id) {

		
		Optional<Order> maybeOrder=orderService.findById(id);
				
		if (maybeOrder.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		return Response.status(Response.Status.OK).entity(OrderDTO.fromEntityToDTO(maybeOrder.get())).build();
		

	}
	
	@RolesAllowed({"admin"})
	@SecurityRequirement(name = "oauth2")
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	@Operation(summary = "remove an order")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Order deleted", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDTO.class))

			}), @APIResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
			@APIResponse(responseCode = "400", description = "Bad request", content = @Content),
			@APIResponse(responseCode = "404", description = "Couldn't find order to delete", content = @Content)

	})
	public Response deleteOrder(
			@Parameter(
		            description = "ID of the order",
		            required = true,
		            example = "3777616f-a80e-4b24-a20f-55e947a81b07",
		            schema = @Schema(type = SchemaType.STRING))
			
			@PathParam(value = "id") @NotBlank String id){

		
		Optional<Order> maybeIsStored=orderService.findById(id);
		
		if (maybeIsStored.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		Order toDelete=maybeIsStored.get();
		
		orderService.delete(toDelete);
		
		return Response.status(Response.Status.OK).entity(OrderDTO.getDeleted(toDelete)).build();

	}

	@RolesAllowed({"admin"})
	@SecurityRequirement(name = "oauth2")
	@PATCH
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "edit an order")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Order modified", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDTO.class))

			}), @APIResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
			@APIResponse(responseCode = "400", description = "Bad request", content = @Content),
			@APIResponse(responseCode = "404", description = "Couldn't find order to modify", content = @Content),
			@APIResponse(responseCode = "304", description = "Nothing to Change", content = @Content)

	})
	public Response editOrder(
			
			@Parameter(
		            description = "Json rapresentation of the fields to modify",
		            required = true,
		            schema = @Schema(implementation = OrderDTO.class))
			
			@Valid OrderDTO editRequestDTO
			
			){

		
		Optional<Order> maybeIsStored=orderService.findById(editRequestDTO.getId());
				
		if (maybeIsStored.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		Order toEdit=maybeIsStored.get();
		
		String oldStatus=toEdit.status;
		
		orderService.edit(toEdit,editRequestDTO);
			
		checkAndSend(toEdit, oldStatus);
		
		return Response.status(Response.Status.OK).entity(OrderDTO.fromEntityToDTO(toEdit)).build();

	}
	
	void checkAndSend(Order toEdit, String oldStatus) {
		if (!toEdit.status.equals(oldStatus)) {
			
			MessageDTO message=MessageDTO.fromOrderToMessage(oldStatus,toEdit);
			
			messageEmitter.send(message);  
		}
	}
}
