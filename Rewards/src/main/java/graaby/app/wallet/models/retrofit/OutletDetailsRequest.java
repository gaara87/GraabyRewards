package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 4/1/15.
 */
@JsonObject
public class OutletDetailsRequest {
    @JsonField(name = "oid")
    public int outletID;

    public OutletDetailsRequest() {
    }

    public OutletDetailsRequest(int outletID) {
        this.outletID = outletID;
    }
}
