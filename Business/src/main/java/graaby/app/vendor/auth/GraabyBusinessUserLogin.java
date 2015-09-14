package graaby.app.vendor.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;

import junit.framework.Assert;

import java.io.IOException;

final public class GraabyBusinessUserLogin {

    protected static String USER_DATA_SAAVI = "saavi";

    public static void login(final Activity parentActivity, int index) {

        final GraabyBusinessUserLoginEvent listener = (GraabyBusinessUserLoginEvent) parentActivity;

        Assert.assertNotNull(listener);

        final AccountManager acm = AccountManager.get(parentActivity);
        final Account[] accounts = acm
                .getAccountsByType(GraabyBusinessUserAuthenticatorActivity.ACCOUNT_TYPE);

        if (accounts.length != 0) {
            if (accounts.length < index) {
                return;
            }
            acm.getAuthToken(accounts[index], GraabyBusinessUserAuthenticatorActivity.AUTHTOKEN_TYPE, null, parentActivity, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle b = future.getResult();
                        String tabletUID = b.getString(AccountManager.KEY_AUTHTOKEN);
                        String preferID = String.valueOf(b.getString(AccountManager.KEY_ACCOUNT_NAME).hashCode());
                        listener.onSuccessfulLogin(tabletUID, accounts.length, preferID);
                    } catch (OperationCanceledException e) {
                        listener.onFailureLogin();
                    } catch (AuthenticatorException e) {
                        listener.onFailureLogin();
                    } catch (IOException e) {
                        listener.onFailureLogin();
                    }
                }
            }, null);

        } else {

            acm.addAccount(
                    GraabyBusinessUserAuthenticatorActivity.ACCOUNT_TYPE, null,
                    null, null, parentActivity,
                    new AccountManagerCallback<Bundle>() {

                        @Override
                        public void run(AccountManagerFuture<Bundle> future) {
                            try {
                                if (future.getResult()
                                        .getString(AccountManager.KEY_USERDATA)
                                        .equals(GraabyBusinessUserAuthenticatorActivity.PARAM_SUCCESSFUL_ACCOUNT_ADD)) {
                                    login(parentActivity, accounts.length);
                                }

                            } catch (OperationCanceledException e) {
                                listener.onFailureLogin();
                            } catch (AuthenticatorException e) {
                                listener.onFailureLogin();
                            } catch (IOException e) {
                                listener.onFailureLogin();
                            }
                        }
                    }, null);
        }
    }

    public static Account[] getGraabyAccounts(AccountManager acm) {
        return acm.getAccountsByType(GraabyBusinessUserAuthenticatorActivity.ACCOUNT_TYPE);
    }


    public interface GraabyBusinessUserLoginEvent {

        void onSuccessfulLogin(String tabletUID, int numberOfAccounts, String preferID);

        void onFailureLogin();
    }

}
