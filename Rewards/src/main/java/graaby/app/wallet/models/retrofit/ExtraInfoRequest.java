package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash.
 */
@JsonObject
public class ExtraInfoRequest {
    @JsonField(name = "dob")
    public String birthday;
    @JsonField(name = "gender")
    public String gender;

    public ExtraInfoRequest() {
    }

    public ExtraInfoRequest(String dob, String gender) {
        this.birthday = dob;
        this.gender = gender;
    }
}
