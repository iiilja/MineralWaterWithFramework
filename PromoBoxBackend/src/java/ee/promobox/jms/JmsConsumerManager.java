package ee.promobox.jms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.jms.Destination;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.listener.AbstractJmsListeningContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class JmsConsumerManager {
    private static final JmsConsumerManager instance = new JmsConsumerManager();
    private static final Log log = LogFactory.getLog(JmsConsumerManager.class);

    private final List<Thread> threads = new ArrayList<Thread>();
    private List<AbstractJmsListeningContainer> listeningContainers = new ArrayList<AbstractJmsListeningContainer>();
    
    public static JmsConsumerManager getInstance() {
        return instance;
    }

    @PreDestroy
    public void destory() {
        log.info("About to destroy");

        log.info("Stopping listening containers: " + listeningContainers.size());
        for (AbstractJmsListeningContainer cont : listeningContainers) {
            cont.stop();
        }
        
        interrupt();

        log.info("Shutting down listening containers: " + listeningContainers.size());
        for (AbstractJmsListeningContainer cont : listeningContainers) {
            cont.shutdown();
        }
    }

    public void interrupt() {
        synchronized (threads) {
            log.info("Interrupting consumer threads");

            for (Thread t : threads) {
                t.interrupt();
            }
        }
    }

    public void addThread() {
        addThread(Thread.currentThread());
    }
    
    public void removeThread() {
        removeThread(Thread.currentThread());
    }

    public void addThread(Thread thread) {
        synchronized (threads) {
            threads.add(thread);
        }
    }

    public void removeThread(Thread thread) {
        synchronized (threads) {
            threads.remove(thread);
        }
    }

    public List<AbstractJmsListeningContainer> getListeningContainers() {
        return listeningContainers;
    }

    public void setListeningContainers(List<AbstractJmsListeningContainer> listeningContainers) {
        this.listeningContainers = listeningContainers;
    }
    
    
}

