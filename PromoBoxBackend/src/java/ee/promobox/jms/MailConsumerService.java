/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.jms;

import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 *
 * @author vitalispiridonov
 */
public class MailConsumerService  extends MessageListenerAdapter {
    
    public static Log log = LogFactory.getLog(MailConsumerService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void handleMessage(final MailDto mailDto) {	
        try {
            final MimeMessagePreparator preparator = new MimeMessagePreparator() {

                public void prepare(MimeMessage mimeMessage) throws Exception {

                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage);

                    message.setTo(mailDto.getTo());
                    message.setFrom(mailDto.getFrom());
                    message.setSubject(mailDto.getSubject());
                    message.setReplyTo(mailDto.getReplyTo());

                    message.setText(mailDto.getText(), mailDto.isHtml());

                }

            };
            
            mailSender.send(preparator);
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
