package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 4/1/15.
 */
@JsonObject
public class OutletsForBusinessRequest extends OutletsRequest {
    @JsonField(name = "bid")
    public int businessID;

    public OutletsForBusinessRequest() {
    }

    public OutletsForBusinessRequest(int brandID) {
        this.businessID = brandID;
    }
}
