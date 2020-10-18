package org.acme.listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class OrderMessageListener {

	
	
    static final Logger LOG = Logger.getLogger(OrderMessageListener.class);
	
	@Incoming("m1")
	public CompletionStage<Void> processKafkaChar(Message<JsonObject> message) {
		return CompletableFuture.runAsync(() -> {
			StringBuilder sb=new StringBuilder();
			
			JsonObject payLoad=message.getPayload();
			
			sb.append("Order: ").append(payLoad.getString("id")).append(" changed status from ")
								.append(payLoad.getString("oldStatus")).append(" to ")
								.append(payLoad.getString("newStatus"))
								.append(" at ").append(payLoad.getString("upadtedAt")).append(".")
								.append(" Total amount is:").append(payLoad.getDouble("totalAmount"));
			
			
			LOG.info(sb.toString());			
			

		});
	}

}
