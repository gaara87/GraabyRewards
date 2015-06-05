package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import graaby.app.wallet.util.DiscountItemType;

/**
 * Created by Akash on 3/30/15.
 */
@JsonObject
public class DiscountItemDetailsResponse {
    @JsonField(name = "id")
    public String discountItemID;

    @JsonField(name = "type", typeConverter = DiscountItemType.DiscountItemTypeConverter.class)
    public DiscountItemType typeOfDI;

    @JsonField(name = "cost")
    public String costOfDI;

    @JsonField(name = "val")
    public String discountValue;

    @JsonField(name = "bname")
    public String businessName;

    @JsonField(name = "count")
    public String leftOverCount;

    @JsonField(name = "pic")
    public String pictureURL;

    @JsonField(name = "bid")
    public int businessId;

    @JsonField(name = "redemption_point")
    public int redemptionCost;

    @JsonField(name = "exp")
    public String expiryDate;

    @JsonField(name = "desc")
    public String discountDetails;

    @JsonField(name = "terms")
    public String discountTermsAndConditions;

    @JsonField(name = "min")
    public int minValue;
}
