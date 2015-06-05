package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 4/8/15.
 */
@JsonObject
public class RatingRequest {
    @JsonField(name = "tx_id")
    public String transactionID;
    @JsonField(name = "rating")
    public float transactionRating;

    public RatingRequest(String mActivityID, float rating) {
        this.transactionID = mActivityID;
        this.transactionRating = rating;
    }

    public RatingRequest() {
    }
}
