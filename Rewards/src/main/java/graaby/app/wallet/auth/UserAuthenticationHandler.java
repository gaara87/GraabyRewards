package graaby.app.wallet.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import graaby.app.wallet.events.AuthEvents;


/**
 * Created by Akash on 3/25/15.
 */
public class UserAuthenticationHandler {

    public String uid;
    public String oAuth;

    public void login(Context context) {
        AccountManager acm = AccountManager.get(context);
        Account[] accounts = acm
                .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            login(null, acm, accounts[0]);
        }
    }

    private void login(final OnUserAuthentication callback, final AccountManager acm, final Account account) {

        acm.getAuthToken(account, UserLoginActivity.AUTHTOKEN_TYPE, null,
                null, future -> {
                    if (future.isDone()) {
                        try {
                            initFromFuture(future);
                            if (callback != null) {
                                callback.onSuccessfulAuthentication(true);
                                EventBus.getDefault().postSticky(new AuthEvents.SessionAuthenticatedEvent());
                            }
                        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                            if (callback != null)
                                callback.onFailureAuthentication();
                        }
                    } else if (future.isCancelled()) {
                        if (callback != null)
                            callback.onFailureAuthentication();
                    }
                }, null);
    }


    public void loginOrAddAccount(Activity activity, final OnUserAuthentication callback) {
        if (TextUtils.isEmpty(oAuth)) {
            final AccountManager acm = AccountManager.get(activity);
            Account[] accounts = acm
                    .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);
            if (accounts.length != 0) {
                login(callback, acm, accounts[0]);
            } else {
                acm.addAccount(UserLoginActivity.ACCOUNT_TYPE, UserLoginActivity.AUTHTOKEN_TYPE, null, null, activity,
                        future -> {
                            if (future.isDone()) {
                                try {
                                    String oauth = future.getResult().getString(UserLoginActivity.AUTHTOKEN_USERDATA_KEY);
                                    future.getResult().putString(AccountManager.KEY_AUTHTOKEN, oauth);
                                    initFromFuture(future);
                                    callback.onSuccessfulAuthentication(false);
                                    EventBus.getDefault().postSticky(new AuthEvents.SessionAuthenticatedEvent());
                                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                                    callback.onFailureAuthentication();
                                }
                            } else if (future.isCancelled()) {
                                callback.onFailureAuthentication();
                            }
                        }, null);
            }
        } else {
            callback.onSuccessfulAuthentication(true);
            EventBus.getDefault().postSticky(new AuthEvents.SessionAuthenticatedEvent());
        }
    }

    public void logout(Activity activity) {
        this.oAuth = "";
        this.uid = "";
        final AccountManager acm = AccountManager.get(activity);
        Account[] accounts = acm
                .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                acm.removeAccount(accounts[0], activity, null, null);
            } else {
                acm.removeAccount(accounts[0], null, null);
            }
        }
    }

    public void initFromFuture(AccountManagerFuture<Bundle> future) throws AuthenticatorException, OperationCanceledException, IOException {
        this.oAuth = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
        this.uid = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);
    }

    public boolean isAuthenticated() {
        return !TextUtils.isEmpty(oAuth);
    }

    public String getAccountEmail() {
        return this.uid;
    }

    public interface OnUserAuthentication {
        void onSuccessfulAuthentication(boolean shouldStartFlag);

        void onFailureAuthentication();
    }
}
