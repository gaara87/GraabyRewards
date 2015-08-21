package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash.
 */
@JsonObject
public class ProfileNavResponse extends BaseResponse {
    @JsonField(name = "name")
    public String userFullName;
    @JsonField(name = "points")
    public String currentPoints;
    @JsonField(name = "discounts")
    public String totalCouponAndVouchers;
    @JsonField(name = "pic")
    public String profilePictureURL;
}
