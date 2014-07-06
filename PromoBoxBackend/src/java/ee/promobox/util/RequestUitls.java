/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.util;

import org.json.JSONObject;

/**
 *
 * @author Maxim
 */
public class RequestUitls {

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

}
