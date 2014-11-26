/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.jms;

import java.io.Serializable;

/**
 *
 * @author vitalispiridonov
 */
public class MailDto implements Serializable {
    
    private String to;
    private String from;
    private String replyTo;
    private String subject;
    private String text;
    
    private boolean html = true;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}
