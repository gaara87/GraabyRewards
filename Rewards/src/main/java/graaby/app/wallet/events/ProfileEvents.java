package graaby.app.wallet.events;

/**
 * Created by Akash on 2/25/15.
 */
public class ProfileEvents {
    public static class NameUpdatedEvent {
    }

    public static class PictureUpdatedEvent {
        private static String imageURL;

        public PictureUpdatedEvent(String url) {
            imageURL = url;
        }

        public String getImageURL() {
            return imageURL;
        }
    }

    public static class LoggedOutEvent {
    }
}
