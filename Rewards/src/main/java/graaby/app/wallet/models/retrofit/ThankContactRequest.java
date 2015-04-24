package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 4/8/15.
 */
@JsonObject
public class ThankContactRequest {
    @JsonField(name = "uid_thank")
    public int activityIDToThank;

    public ThankContactRequest() {

    }

    public ThankContactRequest(int activityID) {
        this.activityIDToThank = activityID;
    }
}
