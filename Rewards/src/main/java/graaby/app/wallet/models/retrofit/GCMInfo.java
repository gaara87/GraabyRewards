package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 3/25/15.
 */
@JsonObject
public class GCMInfo {
    @JsonField(name = "reg_id")
    public String registrationId;

    public GCMInfo() {
        this.registrationId = "";
    }

    public GCMInfo(String regid) {
        this.registrationId = regid;
    }
}
