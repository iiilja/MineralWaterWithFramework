/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

/**
 *
 * @author vitalispiridonov
 */
public class FileConsumerService extends MessageListenerAdapter {
    
    public static Log log = LogFactory.getLog(FileConsumerService.class);
    
    public void handleMessage(FileDto fileDto) {
        log.info("Convert file here");
        
        log.info("File id: " + fileDto.getId());
    }
    
}
