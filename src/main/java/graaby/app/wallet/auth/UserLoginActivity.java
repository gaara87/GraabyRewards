package graaby.app.wallet.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.UUID;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.OkHttpStack;
import graaby.app.wallet.R;
import graaby.app.wallet.activities.OnboardingActivity;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class UserLoginActivity extends AccountAuthenticatorActivity implements
        ErrorListener, Listener<JSONObject>, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, View.OnClickListener {

    final public static String ACCOUNT_TYPE = "graaby.app.wallet";
    final public static String AUTHTOKEN_TYPE = "graaby.app.wallet";
    final private static String TAG = "UserLoginActivity";
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mPassword;
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    private AccountManager acm;
    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                showProgress(true);
                Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            String email = user.getProperty("email").toString();
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put(getString(R.string.username),
                                    email);

                            requestParams.put(getString(R.string.password),
                                    mPassword);
                            requestParams.put(getString(R.string.uuid),
                                    Installation.id(UserLoginActivity.this));

                            requestParams.put(getString(R.string.social_login), "f");
                            requestParams.put(getString(R.string.access_token), "needtogetaccesstoken");
                            try {

                                RequestQueue mRequestQ = Volley.newRequestQueue(UserLoginActivity.this,
                                        new OkHttpStack());

                                mRequestQ.add(new CustomRequest("login", requestParams, UserLoginActivity.this,
                                        UserLoginActivity.this));

                                if (Session.getActiveSession() != null) {
                                    Session.getActiveSession().closeAndClearTokenInformation();
                                }

                                Session.setActiveSession(null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).executeAsync();
            } else {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);

        socialLoginInitialization(savedInstanceState);

        setContentView(R.layout.activity_login);

        acm = AccountManager.get(this);
        Account[] accounts = acm
                .getAccountsByType(UserLoginActivity.ACCOUNT_TYPE);

        if (accounts.length != 0) {
            Toast.makeText(this, "Only 1 account allowed", Toast.LENGTH_LONG)
                    .show();
            finish();
        }

        findViewById(R.id.google_login).setOnClickListener(this);
        findViewById(R.id.fb_login).setOnClickListener(this);

        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                                                  KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                }
        );
    }

    private void socialLoginInitialization(Bundle savedInstanceState) {
        mPlusClient = new PlusClient.Builder(this, this, this)
                .setScopes(Scopes.PLUS_LOGIN)
                .build();

        mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage(getString(R.string.login_progress_signing_in));

        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setPermissions("email").setCallback(statusCallback));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        /*getMenuInflater().inflate(R.menu.activity_login, menu);*/
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     *
     * @throws org.json.JSONException
     */
    public void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(getString(R.string.username),
                    mEmail);
            requestParams.put(getString(R.string.password),
                    mPassword);
            requestParams.put(getString(R.string.uuid),
                    Installation.id(this));


            NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (mNfcAdapter != null) {
                requestParams.put(getString(R.string.nfc_flag), Boolean.TRUE);
            }
            try {

                RequestQueue mRequestQ = Volley.newRequestQueue(this);

                CustomRequest loginRequest = new CustomRequest("login", requestParams, this,
                        this);
                loginRequest.setShouldCache(Boolean.FALSE);
                mRequestQ.add(loginRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onResponse(JSONObject response) {
        showProgress(false);
        if (response.has(getString(R.string.response_msg))) {
            try {
                Toast.makeText(this, response.getString(getString(R.string.response_msg)), Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
            }
        } else {
            Account acc = new Account(mEmail, UserLoginActivity.ACCOUNT_TYPE);
            Bundle b = new Bundle();
            String urlField = getString(R.string.url), oauth = getResources()
                    .getString(R.string.oauth), token = "";
            try {
                b.putString(urlField, response.getString(urlField));
            } catch (JSONException e) {
            }

            try {
                token = response.getString(oauth);
                b.putString(oauth, token);
            } catch (JSONException e) {
            }

            acm.addAccountExplicitly(acc, "welovegoogle", b);

            if (response.has(getString(R.string.login_core))) {
                try {
                    JSONObject nfcCOreObject = response.getJSONObject(getString(R.string.login_core));
                    FileOutputStream fos = null;
                    try {
                        fos = UserLoginActivity.this.openFileOutput("beamer", MODE_PRIVATE);
                        DataOutputStream dos = new DataOutputStream(fos);
                        dos.writeUTF(nfcCOreObject.toString());
                        dos.close();
                        fos.close();
                    } catch (FileNotFoundException e) {
                    } catch (IOException e) {
                    }

                } catch (JSONException e) {
                }
            }

            Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mEmail);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE,
                    UserLoginActivity.ACCOUNT_TYPE);
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);
            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);

            finish();
            Log.i("Graaby Account", "Account added successfully");
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        showProgress(false);
        mPasswordView.setError(getString(R.string.error_incorrect_password));
        mPasswordView.requestFocus();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        showProgress(true);
        mConnectionProgressDialog.dismiss();
        String accountName = mPlusClient.getAccountName();

        String accessToken = null;
        try {
            accessToken = GoogleAuthUtil.getToken(this,
                    mPlusClient.getAccountName(),
                    "oauth2:" + Scopes.PLUS_LOGIN);
        } catch (IOException transientEx) {
            // network or server error, the call is expected to succeed if you try again later.
            // Don't attempt to call again immediately - the request is likely to
            // fail, you'll hit quotas or back-off.
            return;
        } catch (UserRecoverableAuthException e) {
            // Recover
            accessToken = null;
        } catch (GoogleAuthException authEx) {
            // Failure. The call is not expected to ever succeed so it should not be
            // retried.
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(getString(R.string.username),
                mPlusClient.getAccountName());

        requestParams.put(getString(R.string.password),
                mPassword);
        requestParams.put(getString(R.string.uuid),
                Installation.id(this));

        requestParams.put(getString(R.string.social_login), "g");
        requestParams.put(getString(R.string.access_token), accessToken);
        try {

            RequestQueue mRequestQ = Volley.newRequestQueue(this,
                    new OkHttpStack());
            CustomRequest request = new CustomRequest("login", requestParams, this,
                    this);
            request.setShouldCache(Boolean.FALSE);
            mRequestQ.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPlusClient.disconnect();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (mConnectionProgressDialog.isShowing()) {
            // The user clicked the sign-in button already. Start to resolve
            // connection errors. Wait until onConnected() to dismiss the
            // connection dialog.
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    mPlusClient.connect();
                    mConnectionResult = null;
                }
            }
        }
        // Save the result and resolve the connection failure upon a user click.
        mConnectionResult = connectionResult;
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();
        } else {
            Session.getActiveSession().onActivityResult(this, requestCode, responseCode, intent);
        }
        mConnectionProgressDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.google_login && !mPlusClient.isConnected()) {
            if (mConnectionResult == null) {
                mPlusClient.connect();
                mConnectionProgressDialog.show();
            } else {
                try {
                    mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    // Try connecting again.
                    mConnectionResult = null;
                    mPlusClient.connect();
                }
            }
        } else if (view.getId() == R.id.fb_login) {
            Session session = Session.getActiveSession();
            if (session == null) {
                session = new Session(this);
                Session.setActiveSession(session);
            }
            if (!session.isOpened() && !session.isClosed()) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            } else {
                Session.openActiveSession(this, true, statusCallback);
            }

        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }


    /**
     * This class helps in providing a unique id for the device to track installation
     */
    private static class Installation {
        private static final String INSTALLATION = "INSTALLATION";
        private static String sID = null;

        public synchronized static String id(Context context) {
            if (sID == null) {
                File installation = new File(context.getFilesDir(),
                        INSTALLATION);
                try {
                    if (!installation.exists())
                        writeInstallationFile(installation);
                    sID = readInstallationFile(installation);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return sID;
        }

        private static String readInstallationFile(File installation)
                throws IOException {
            RandomAccessFile f = new RandomAccessFile(installation, "r");
            byte[] bytes = new byte[(int) f.length()];
            f.readFully(bytes);
            f.close();
            return new String(bytes);
        }

        private static void writeInstallationFile(File installation)
                throws IOException {
            FileOutputStream out = new FileOutputStream(installation);
            String id = UUID.randomUUID().toString();
            out.write(id.getBytes());
            out.close();
        }
    }

}
