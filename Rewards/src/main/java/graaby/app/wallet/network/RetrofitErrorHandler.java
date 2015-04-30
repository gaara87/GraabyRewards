package graaby.app.wallet.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.apache.http.HttpStatus;

import javax.inject.Inject;

import graaby.app.wallet.R;
import graaby.app.wallet.auth.UserLoginActivity;
import graaby.app.wallet.modules.ForApplication;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Created by Akash on 3/23/15.
 */
public class RetrofitErrorHandler implements ErrorHandler {
    @Inject
    @ForApplication
    Context application;

    @Override
    public Throwable handleError(RetrofitError cause) {
        int errorResourceString = -1, statusCode = 0;
        if (cause.getResponse() != null)
            statusCode = cause.getResponse().getStatus();
        switch (cause.getKind()) {
            case CONVERSION:
                errorResourceString = R.string.error_data_parsing;
                break;
            case HTTP:
                if (statusCode == HttpStatus.SC_UNAUTHORIZED ||
                        statusCode == HttpStatus.SC_FORBIDDEN || statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                        statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                    errorResourceString = R.string.error_unauthorized;
                    //Handle auth failure event
                    final AccountManager acm = AccountManager.get(application);
                    final Account[] accounts = acm
                            .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);
                    if (accounts.length != 0) {
                        acm.updateCredentials(accounts[0], UserLoginActivity.AUTHTOKEN_TYPE, null, (Activity) application, null, null);
                        ((Activity) application).finish();
                    }
                } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    errorResourceString = R.string.error_404;
                }
                break;
            case NETWORK:
                if (statusCode == HttpStatus.SC_REQUEST_TIMEOUT)
                    errorResourceString = R.string.error_timeout;
                else
                    errorResourceString = R.string.error_network;
                break;
            case UNEXPECTED:
                errorResourceString = R.string.error_unexpected;
                break;
        }
        if (errorResourceString != -1) {
            Toast.makeText(application, errorResourceString, Toast.LENGTH_SHORT).show();
        }
        return cause;
    }

}
