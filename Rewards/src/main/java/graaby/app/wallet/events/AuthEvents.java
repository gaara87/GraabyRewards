package graaby.app.wallet.events;

/**
 * Created by Akash.
 */
public class AuthEvents {
    public static class SessionAuthenticatedEvent {
    }

    public static class LoginSuccessfulEvent {
    }

    public static class LoggedOutEvent {
        public final TYPE typeOfEvent;

        public LoggedOutEvent(TYPE eventType) {
            typeOfEvent = eventType;
        }

        public enum TYPE {
            UPDATE, REMOVE;
        }
    }
}
