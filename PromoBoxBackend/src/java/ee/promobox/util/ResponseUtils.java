/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.util;

import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maxim
 */
public class ResponseUtils {

    public final static String ERROR = "ERROR";
    public final static String OK = "OK";
    public final static String RESULT = "result";
    public final static String REASON = "reason";
    public final static String WARN = "WARN";
    
    private final static Logger log = LoggerFactory.getLogger(ResponseUtils.class);

    public static JSONObject getErrorResponse() {

        JSONObject obj = new JSONObject();

        try {
            obj.put("response", ERROR);
        } catch (Exception ex) {

        }

        return obj;
    }
    
    public static void sendUnauthorized(HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

}
