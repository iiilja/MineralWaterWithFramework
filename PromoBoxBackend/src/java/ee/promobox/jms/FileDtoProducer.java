/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractJmsListeningContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * 
 * @author vitalispiridonov
 */
public class FileDtoProducer implements Runnable {

	private static final Log log = LogFactory.getLog(FileDtoProducer.class);

	private ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://kioskBroker");;
	private final FileDto fileDto;
	
	public FileDtoProducer(FileDto fileDto) {
		super();
		this.connectionFactory = connectionFactory;
		this.fileDto = fileDto;
	}

	public void run() {
		int clientId = fileDto.getClientId();

		try {
			Connection connection = connectionFactory.createConnection();
			connection.start();

			// Create a Session
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination d = session.createQueue("fileDestination" + clientId);

			MessageProducer producer = session.createProducer(d);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			ObjectMessage message = session.createObjectMessage();
			message.setObject(fileDto);

			producer.send(message);
			
			session.close();
			connection.close();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public ActiveMQConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

}
