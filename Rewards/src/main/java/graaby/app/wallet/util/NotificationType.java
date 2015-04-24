package graaby.app.wallet.util;

/**
 * Created by Akash.
 */
public enum NotificationType {
    NONE(0), SHARE_POINTS(5), TRANSACTION(6), NEW_VOUCHER(7), NEW_FEED(8), THANK_CONTACT(10), CHECKIN(11);
    private final int value;

    NotificationType(int value) {
        this.value = value;
    }

    public static NotificationType getType(int value) {
        NotificationType[] types = NotificationType.values();
        for (int i = 0; i < types.length; i++) {
            if (types[i].value == value)
                return types[i];
        }
        return NONE;
    }
}
