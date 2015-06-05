package graaby.app.wallet.util;

import com.bluelinelabs.logansquare.typeconverters.IntBasedTypeConverter;

import graaby.app.wallet.R;

/**
 * Created by Akash on 3/25/15.
 */
public enum ActivityType {
    TRANSACTION(1), BUY_COUPON(2), BUY_VOUCHER(3), CHECK_IN(4), SHARE_POINTS(5), RECEIEVE_POINTS(6);
    private final int value;

    ActivityType(final int newValue) {
        value = newValue;
    }

    public static int getDrawableResourceIDForActivity(ActivityType type) {
        switch (type) {
            case TRANSACTION:
                return R.drawable.ic_transaction_24;
            case SHARE_POINTS:
                return R.drawable.ic_sendpoint_24;
            case RECEIEVE_POINTS:
                return R.drawable.ic_receivepoint;
            case CHECK_IN:
                return R.drawable.ic_checkin;
            case BUY_COUPON:
                return R.drawable.coupon_withpadding;
            case BUY_VOUCHER:
                return R.drawable.voucher_withpadding;
        }
        return R.drawable.ic_transaction_24;
    }

    public int getValue() {
        return value;
    }

    public static class ActivityTypeConverter extends IntBasedTypeConverter<ActivityType> {
        @Override
        public ActivityType getFromInt(int i) {
            return ActivityType.values()[i - 1];
        }

        public int convertToInt(ActivityType object) {
            return 1;
        }
    }
}


