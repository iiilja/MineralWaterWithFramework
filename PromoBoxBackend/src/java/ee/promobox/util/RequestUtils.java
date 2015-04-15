/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.util;

import java.io.Writer;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Maxim
 */
public class RequestUtils {

    public final static String ERROR = "ERROR";
    public final static String OK = "OK";
    public final static String RESULT = "result";
    public final static String REASON = "reason";
    
    private final static Logger log = LoggerFactory.getLogger(
            RequestUtils.class);

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
