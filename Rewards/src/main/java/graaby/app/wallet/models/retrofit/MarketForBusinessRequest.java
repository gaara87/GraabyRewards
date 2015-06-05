package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 3/30/15.
 */
@JsonObject
public class MarketForBusinessRequest extends MarketRequest {
    @JsonField(name = "bid")
    public int brandId;

    public MarketForBusinessRequest() {
    }

    public MarketForBusinessRequest(int brandID) {
        this.brandId = brandID;
    }
}
