package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 3/30/15.
 */
@JsonObject
public class MarketForOutletRequest extends MarketRequest {
    @JsonField(name = "oid")
    public int outletID;

    public MarketForOutletRequest() {
    }

    public MarketForOutletRequest(int outletID) {
        this.outletID = outletID;
    }
}
