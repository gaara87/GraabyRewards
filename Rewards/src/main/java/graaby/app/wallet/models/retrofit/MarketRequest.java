package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 3/28/15.
 */
@JsonObject
public class MarketRequest {
    @JsonField
    public int page;
    @JsonField
    public int size;
}
