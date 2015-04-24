package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 4/3/15.
 */
@JsonObject
public class AddContactRequest {
    @JsonField(name = "phone")
    public String contactPhone;

    public AddContactRequest() {
    }

    public AddContactRequest(String phoneNum) {
        this.contactPhone = phoneNum;
    }
}
