package graaby.app.wallet.util;

import retrofit.RetrofitError;

/**
 * Created by Akash.
 */
public class CustomRetrofitErrorThrowable extends Throwable {
    public CustomRetrofitErrorThrowable(String message, RetrofitError cause) {
        super(message, cause);
    }
}
