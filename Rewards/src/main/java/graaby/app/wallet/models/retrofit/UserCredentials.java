package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 3/23/15.
 */
@JsonObject
public class UserCredentials {

    @JsonField(name = "uname")
    public String emailID;

    @JsonField(name = "pwd")
    public String password;

    @JsonField(name = "uuid")
    public String universalDeviceRandomID;

    @JsonField(name = "android_nfc")
    public boolean nfcCapable;
}
