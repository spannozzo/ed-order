package org.acme.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.acme.dto.MessageDTO;
import org.acme.entity.Order;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Singleton
public class ChangeStatusService {
	
	@Inject 
	@Channel("change-status") 
	Emitter<MessageDTO> messageEmitter;
	
	@ConfigProperty(name = "acme.messaging.enabled")
	private boolean messagesAreEnabled;
	
	public void checkAndSend(Order toEdit, String oldStatus) {
		if (messagesAreEnabled && !toEdit.status.equals(oldStatus)) {
				
			MessageDTO message=MessageDTO.fromOrderToMessage(oldStatus,toEdit);
			messageEmitter.send(message);  
		}
		
	}
	
}
