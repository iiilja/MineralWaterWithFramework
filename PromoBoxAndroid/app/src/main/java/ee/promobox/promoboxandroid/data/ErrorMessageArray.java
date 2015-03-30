package ee.promobox.promoboxandroid.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ilja on 28.01.2015.
 */
public class ErrorMessageArray extends JSONArray {

    public void addError(ErrorMessage msg) throws JSONException {
        JSONObject json = new JSONObject();
            json.put("name", msg.getName() + "");
            json.put("message", msg.getMessage() + "");
            json.put("date", msg.getDate());
            json.put("stackTrace", msg.getStackTrace() + "");

        put(json);
    }
}
