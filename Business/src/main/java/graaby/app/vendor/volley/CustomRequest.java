package graaby.app.vendor.volley;

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

    private static String tabUID = "";
    private static String preferID = "";

    public CustomRequest(String endpoint, HashMap<String, Object> params,
                         Listener<JSONObject> listener, ErrorListener errorListener) {
        super(Method.POST, ServerURL.url + endpoint, parse(params), listener,
                errorListener);
        this.setRetryPolicy(new DefaultRetryPolicy(10000, 2, 1.5f));
    }

    public CustomRequest(String endpoint, HashMap<String, Object> params,
                         Listener<JSONObject> listener, ErrorListener errorListener, Boolean cacheFlag)
            throws JSONException {
        super(Method.POST, ServerURL.url + endpoint, parse(params), listener,
                errorListener);
        this.setRetryPolicy(new DefaultRetryPolicy(10000, 2, 1.5f));
        this.setShouldCache(cacheFlag);
    }

    public CustomRequest(String url, HashMap<String, Object> params,
                         Listener<JSONObject> listener, ErrorListener errorListener, String tabID) {
        super(Method.POST, ServerURL.url + url, parse(params, tabID), listener,
                errorListener);
    }

    private static JSONObject parse(HashMap<String, Object> params, String tabID) {
        JSONObject jobj = parse(params);
        try {
            jobj.put("tab-uid", tabID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobj;
    }

    public static JSONObject parse(HashMap<String, Object> params) {
        JSONObject jobj = new JSONObject();
        try {
            for (Entry<String, Object> parameter : params.entrySet()) {
                jobj.put(parameter.getKey(), parameter.getValue());
            }
            assert tabUID != "";
            jobj.put("tab-uid", tabUID);
        } catch (JSONException e) {
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

    public static void setPreferenceID(String preferenceID) {
        preferID = preferenceID;
    }

    public static String getPreferenceName() {
        return preferID;
    }

    public static String getTabletUID() {
        return tabUID;
    }

    public static void setTabletUID(String tabletUID) {
        tabUID = tabletUID;
    }
}
