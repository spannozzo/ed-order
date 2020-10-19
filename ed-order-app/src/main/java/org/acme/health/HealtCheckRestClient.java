package org.acme.health;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient
public interface HealtCheckRestClient {

		
	@GET
	@Path("/ready")
	@Produces(MediaType.APPLICATION_JSON)	
	Response getReadyHealthChecks();
	

	
}

