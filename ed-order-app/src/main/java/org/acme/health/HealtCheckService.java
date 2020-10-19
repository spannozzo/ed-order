package org.acme.health;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Singleton
public class HealtCheckService {

	@ConfigProperty(name = "quarkus.http.port")
	String httpPort;
	
	private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    static final Logger LOG = Logger.getLogger(HealtCheckService.class);

	final String healcheckReadyUrl="http://localhost:"+httpPort+"/health/ready";
    
	HttpRequest request=null;
	
	public boolean isDbUp(){
		
		if (request==null) {
			request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(healcheckReadyUrl))
                .build();
		}
		
		boolean ready =false;
		HttpResponse<String> response =null;
        try{
        	response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			
			
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
        catch (InterruptedException e) {
			LOG.error(e.getMessage(),e);
			Thread.currentThread().interrupt();
		}
        if (response!=null) {
			if (response.statusCode()==HttpStatus.SC_OK) {
				ready= true;
			}
			if (response.statusCode()==HttpStatus.SC_SERVICE_UNAVAILABLE) {
			
				try(JsonReader jsonReader = Json.createReader(new StringReader(response.body()));) {
					JsonObject jsonResponse=jsonReader.readObject();
				
					JsonArray checks=jsonResponse.getJsonArray("checks");
					
					Optional<JsonValue> dbHealtCheckOptional=checks.parallelStream().
															filter(
																json ->	json.asJsonObject().getString("name")
																		.equals("Database connections health check")
															)
															.findFirst();
					
					if (dbHealtCheckOptional.isPresent() && dbHealtCheckOptional.get().asJsonObject().getString("status").equals("UP")) {
						ready= true;
					}
				} 	
			}
		}
        
        return ready;
	}

	
}

