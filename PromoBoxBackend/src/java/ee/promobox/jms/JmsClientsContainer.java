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
import javax.jms.Destination;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.AbstractJmsListeningContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 *
 * @author vitalispiridonov
 */
public class JmsClientsContainer {
    
    private static final Log log = LogFactory.getLog(JmsClientsContainer.class);
    
    private ActiveMQConnectionFactory connectionFactory;
    private JmsConsumerManager consumerManager;
    
    @Autowired
    private FileConsumerService fileConsumerService;
    
    private Map<Integer, Destination> destMap = new HashMap();
    private Map<Integer, AbstractJmsListeningContainer> contMap = new HashMap();
    
    public Destination getOrCreateClientDest(int clientId) {
        synchronized (destMap) {
            try {
                Destination d = destMap.get(clientId);

                if (d == null) {
                    Connection connection = connectionFactory.createConnection();
                    connection.start();

                    // Create a Session
                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                    // Create the destination (Topic or Queue)
                    Destination destination = session.createQueue("fileDestination " + clientId);

                    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
                    container.setConnectionFactory(connectionFactory);
                    container.setMaxConcurrentConsumers(3);
                    container.setSessionTransacted(false);
                    container.setDestination(destination);
                    container.setMessageListener(fileConsumerService);
                    
                    destMap.put(clientId, d);
                    contMap.put(clientId, container);
                    
                    consumerManager.getListeningContainers().add(container);

                    return destination;
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        
        return null;
    }

    public ActiveMQConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public JmsConsumerManager getConsumerManager() {
        return consumerManager;
    }

    public void setConsumerManager(JmsConsumerManager consumerManager) {
        this.consumerManager = consumerManager;
    }
    
    
    
}
