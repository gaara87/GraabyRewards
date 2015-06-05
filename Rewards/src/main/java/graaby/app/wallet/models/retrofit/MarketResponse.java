package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

/**
 * Created by Akash on 3/28/15.
 */
@JsonObject
public class MarketResponse {
    @JsonField(name = "count")
    public int count;
    @JsonField(name = "items")
    public List<DiscountItemDetailsResponse> items;
}
