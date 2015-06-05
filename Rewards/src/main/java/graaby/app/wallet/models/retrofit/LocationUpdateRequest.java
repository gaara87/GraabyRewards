package graaby.app.wallet.models.retrofit;

import android.location.Location;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash.
 */
@JsonObject
public class LocationUpdateRequest {
    @JsonField(name = "lt")
    public double latitude;
    @JsonField(name = "lg")
    public double longitude;
    @JsonField(name = "tstamp")
    public long timestamp;

    public LocationUpdateRequest(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        timestamp = System.currentTimeMillis();
    }

    public LocationUpdateRequest() {

    }
}
