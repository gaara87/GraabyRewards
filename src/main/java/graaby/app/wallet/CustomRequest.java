package graaby.app.wallet;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map.Entry;

public class CustomRequest extends JsonObjectRequest {

    private final static String serverURL = "http://mapi.graaby.com/";
    private static String mAuthToken;
    private static String mUID;

    public CustomRequest(String url, HashMap<String, Object> params,
                         Listener<JSONObject> listener, ErrorListener errorListener)
            throws JSONException {
        super(Method.POST, serverURL + url, parse(params), listener,
                errorListener);
        this.setShouldCache(Boolean.TRUE);
        this.setRetryPolicy(new DefaultRetryPolicy(2500, 2, 1.5f));
    }

    private static JSONObject parse(HashMap<String, Object> params) {
        JSONObject jobj = new JSONObject();
        try {
            for (Entry<String, Object> parameter : params.entrySet()) {
                jobj.put(parameter.getKey(), parameter.getValue());
            }
            jobj.put("oauth", mAuthToken);
            jobj.put("uid", mUID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobj;
    }

    public static JSONObject getCachedResponse(Cache.Entry response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.responseHeaders));
            return new JSONObject(jsonString);
        } catch (UnsupportedEncodingException e) {
        } catch (JSONException je) {
        }
        return null;
    }

    public static void initialize(String auth, String emailID) {
        mAuthToken = auth;
        mUID = emailID;
    }

}
