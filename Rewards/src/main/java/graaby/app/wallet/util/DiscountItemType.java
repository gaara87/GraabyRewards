package graaby.app.wallet.util;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

/**
 * Created by Akash on 3/25/15.
 */
public enum DiscountItemType {
    COUPONS("c"), VOUCHERS("v"), PUNCH("p");
    private final String value;

    DiscountItemType(final String newValue) {
        value = newValue;
    }

    public String getValue() {
        return value;
    }

    public static class DiscountItemTypeConverter extends StringBasedTypeConverter<DiscountItemType> {

        @Override
        public DiscountItemType getFromString(String type) {
            switch (type) {
                case "c":
                    return COUPONS;
                case "v":
                    return VOUCHERS;
                case "p":
                    return PUNCH;
                default:
                    return DiscountItemType.valueOf(type);
            }
        }

        @Override
        public String convertToString(DiscountItemType object) {
            return object.getValue();
        }
    }

}
