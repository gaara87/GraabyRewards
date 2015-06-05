package graaby.app.wallet.models.android;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import graaby.app.wallet.models.retrofit.OutletDetail;

public class BusinessMarker implements ClusterItem {
    private final LatLng mPosition;
    private final OutletDetail mPlace;

    public BusinessMarker(LatLng point, OutletDetail place) {
        mPosition = point;
        mPlace = place;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public OutletDetail getPlace() {
        return mPlace;
    }

    public String getTitle() {
        return mPlace.businessName;
    }

    public String getArea() {
        return mPlace.areaName;
    }
}


