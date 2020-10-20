package org.acme.health;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Singleton
public class HealtCheckService {

	@ConfigProperty(name = "acme.http.port")
	Integer httpPort;
	
    static final Logger LOG = Logger.getLogger(HealtCheckService.class);
    
	public boolean isDbUp(){
		
		HttpGet request = new HttpGet("http://localhost:"+httpPort+"/health/ready");

		boolean ready =false;
				
        try(	CloseableHttpClient client = HttpClients.createDefault();
        		CloseableHttpResponse response = client.execute(request);
        	){
			if (response!=null) {
				if (response.getStatusLine().getStatusCode()==HttpStatus.SC_OK) {
					ready= true;
				}
				if (response.getStatusLine().getStatusCode()==HttpStatus.SC_SERVICE_UNAVAILABLE) {
					HttpEntity entity = response.getEntity();
					
					try(JsonReader jsonReader = Json.createReader(new StringReader(EntityUtils.toString(entity)));) {
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
			
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}

        return ready;
	}

	
}

