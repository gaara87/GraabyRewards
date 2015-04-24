package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 4/8/15.
 */
@JsonObject
public class RegistrationRequest {
    @JsonField(name = "email")
    public String email;
    @JsonField(name = "pwd")
    public String password;
    @JsonField(name = "firstname")
    public String firstName;
    @JsonField(name = "lastname")
    public String lastName;
    @JsonField(name = "token")
    public String socialAuthToken;
    @JsonField(name = "provider")
    public String socialIntegrationProvider;
    @JsonField(name = "id")
    public String googleAuthTokenID;
    @JsonField(name = "phone")
    public String phoneNumber;
    @JsonField(name = "ph_verified")
    public boolean isPhoneNumberVerified;
}
