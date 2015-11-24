package graaby.app.vendor.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

final public class GraabyBusinessUserAuthenticator extends
        AbstractAccountAuthenticator {

    /**
     * The tag used to log to adb console. *
     */
    private static final String TAG = "UserAuthenticator";

    // Authentication Service context
    private final Context mContext;

    public GraabyBusinessUserAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType, String authTokenType,
                             String[] requiredFeatures, Bundle options) {
        Log.v(TAG, "addAccount()");
        Log.v(TAG, response.toString());
        final Intent intent = new Intent(mContext,
                GraabyBusinessUserAuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        Log.v(TAG, "End of add account");
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                     Account account, Bundle options) {
        Log.v(TAG, "confirmCredentials()");
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
                                 String accountType) {
        Log.v(TAG, "editProperties()");
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
                               Account account, String authTokenType, Bundle loginOptions)
            throws NetworkErrorException {
        if (!authTokenType
                .equals(GraabyBusinessUserAuthenticatorActivity.AUTHTOKEN_TYPE)) {
            return null;
        }
        final AccountManager am = AccountManager.get(mContext);
        final Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE,
                GraabyBusinessUserAuthenticatorActivity.ACCOUNT_TYPE);
        result.putString(
                GraabyBusinessUserAuthenticatorActivity.PARAM_AUTH_TOKEN,
                authTokenType);
        result.putString(AccountManager.KEY_AUTHTOKEN,
                am.getUserData(account, GraabyBusinessUserLogin.USER_DATA_SAAVI));
        return result;

    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // null means we don't support multiple authToken types
        Log.v(TAG, "getAuthTokenLabel()");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
                              Account account, String[] features) {
        // This call is used to query whether the Authenticator supports
        // specific features. We don't expect to get called, so we always
        // return false (no) for any queries.
        Log.v(TAG, "hasFeatures()");
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
                                    Account account, String authTokenType, Bundle loginOptions) {
        Log.v(TAG, "updateCredentials()");
        return null;
    }
}