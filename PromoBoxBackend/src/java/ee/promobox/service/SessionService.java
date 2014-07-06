/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.service;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 *
 * @author Maxim
 */
@Service
public class SessionService {
    private ConcurrentHashMap<String,Session> sessions = new ConcurrentHashMap<String,Session>();
    
    public void addSession(Session session) {
        sessions.put(session.getUuid(), session);
    }
    
    public Session findSession(String uuid) {
        return sessions.get(uuid);
    }
}
