package org.acme;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
@OpenAPIDefinition(
	    tags = {
	            @Tag(name="Order", 
	            		description="order CRUD operations"),
	    },
	    info = @Info(
	        title="ED Order Main Application",
	        version = "3.1",
	        contact = @Contact(
	            name = "spannozzo",
	            url = "http://acme.org/contacts",
	            email = "spannozzo@acme.org"),
	        license = @License(
	            name = "Apache 2.0",
	            url = "http://www.apache.org/licenses/LICENSE-2.0.html"))
	)
	public class EDOrderApplication extends Application {
	}
