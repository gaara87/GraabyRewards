package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash.
 */
@JsonObject
public class ForgotPasswordRequest {
    @JsonField(name = "email")
    public String email;
}
