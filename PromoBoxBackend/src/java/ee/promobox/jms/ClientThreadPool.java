package ee.promobox.jms;

import java.util.HashMap;
import java.util.Map;

public class ClientThreadPool {
	
	private static final int THREAD_POOL_SIZE = 3;
	
	private Map<Integer, ThreadPool> clientThreadMap = new HashMap<>();
	
	public ThreadPool getClientThreadPool(int clientId) {
		synchronized (clientThreadMap) {
			ThreadPool threadPool = clientThreadMap.get(clientId);
			
			if (threadPool == null) {
				threadPool = new ThreadPool(THREAD_POOL_SIZE);
				
				clientThreadMap.put(clientId, threadPool);
			}
			
			return threadPool;
		}
		
	}
}
