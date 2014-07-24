package graaby.app.wallet.model;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONException;
import org.json.JSONObject;

import graaby.app.wallet.R;

public class BusinessMarker implements ClusterItem {
    private final LatLng mPosition;
    private final JSONObject mPlace;
    private final String mFieldTitleName;
    private final String mFieldArea;

    public BusinessMarker(Context ctxt, LatLng point, JSONObject place) {
        mPosition = point;
        mPlace = place;
        mFieldTitleName = ctxt.getString(R.string.field_business_title);
        mFieldArea = ctxt.getString(R.string.business_area);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public JSONObject getPlace() {
        return mPlace;
    }

    public String getTitle() {
        try {
            return mPlace.getString(mFieldTitleName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getArea() {
        try {
            return mPlace.getString(mFieldArea);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}


