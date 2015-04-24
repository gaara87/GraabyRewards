package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

/**
 * Created by Akash on 4/1/15.
 */
@JsonObject
public class OutletsResponse {
    @JsonField(name = "places")
    public List<OutletDetail> outlets;

}
