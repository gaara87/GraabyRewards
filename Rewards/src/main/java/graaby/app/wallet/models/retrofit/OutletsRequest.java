package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 4/1/15.
 */
@JsonObject
public class OutletsRequest {
    @JsonField(name = "lt")
    public double latitude;
    @JsonField(name = "lg")
    public double longitude;
}
