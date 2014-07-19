/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.util;

import java.io.Writer;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Maxim
 */
public class RequestUtils {

    public final static String ERROR = "ERROR";
    public final static String OK = "OK";

    public static JSONObject getErrorResponse() {

        JSONObject obj = new JSONObject();

        try {
            obj.put("response", ERROR);
        } catch (Exception ex) {

        }

        return obj;
    }

    public static ModelAndView printResult(String text, HttpServletResponse response) throws Exception {

        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Writer writer = response.getWriter();

        writer.write(text);

        writer.flush();

        return null;
    }

}
