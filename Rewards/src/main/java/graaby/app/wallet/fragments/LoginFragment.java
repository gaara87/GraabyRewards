package graaby.app.wallet.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.GraabyNDEFCore;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.auth.UserLoginActivity;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.ForgotPasswordRequest;
import graaby.app.wallet.models.retrofit.UserCredentials;
import graaby.app.wallet.models.retrofit.UserCredentialsResponse;
import graaby.app.wallet.network.services.AuthService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.Helper;


/**
 * Created by gaara on 2/8/15.
 * Make some impeccable shyte
 */
public class LoginFragment extends BaseFragment {

    LoginInterface loginActivity;
    @InjectView(R.id.password)
    EditText mPasswordView;
    @InjectView(R.id.emails_spinner)
    AutoCompleteTextView mAutoCompleteSpinner;
    @InjectView(R.id.login_form)
    View mLoginFormView;
    @InjectView(R.id.login_status)
    View mLoginStatusView;
    @InjectView(R.id.verification_message)
    TextView mLoginStatusMessageView;
    @Inject
    AuthService mLoginService;
    private String mEmail;
    private AccountManager mAccountMgr;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container);
        ButterKnife.inject(this, view);
        ((TextView) view.findViewById(R.id.agreement)).setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loginActivity = (LoginInterface) activity;
        mAccountMgr = AccountManager.get(activity);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, UserLoginActivity.getAccountNames(getActivity()));
        mAutoCompleteSpinner.setAdapter(adapter);
        mPasswordView
                .setOnEditorActionListener((textView, id, keyEvent) -> {
                    if (id == 697 || id == EditorInfo.IME_NULL) {
                        sendRequest();
                        return true;
                    }
                    return false;
                });
    }

    @OnClick(R.id.sign_in_button)
    public void signInButtonClick() {
        Helper.closeKeyboard(getActivity(), mAutoCompleteSpinner);
        sendRequest();
    }

    @OnClick(R.id.forgot_password)
    public void forgotPasswordRequest() {
        mEmail = mAutoCompleteSpinner.getText().toString();
        if (TextUtils.isEmpty(mEmail) || !mEmail.contains("@")) {
            Toast.makeText(getActivity(), "Please use a valid email", Toast.LENGTH_SHORT).show();
        } else {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.email = mEmail;
            Toast.makeText(getActivity(), "Resetting password", Toast.LENGTH_SHORT).show();
            mCompositeSubscriptions.add(mLoginService.passwordReset(request)
                    .compose(this.<BaseResponse>applySchedulers())
                    .subscribe(new CacheSubscriber<BaseResponse>(getActivity()) {
                        @Override
                        public void onSuccess(BaseResponse result) {
                            if (result.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success))) {
                                Toast.makeText(getActivity(), "Check e-mail for instructions", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), result.message, Toast.LENGTH_LONG).show();
                            }

                        }
                    }));
        }
    }

    @OnClick(R.id.new_user_register)
    public void registerButtonClicked() {
        loginActivity.onNewUserRegisterRequest();
    }

    @Override
    protected void sendRequest() {
        // Reset errors.
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mAutoCompleteSpinner.getText().toString();
        String mPassword = mPasswordView.getText().toString();

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
            mAutoCompleteSpinner.setError(getString(R.string.error_field_required));
            focusView = mAutoCompleteSpinner;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mAutoCompleteSpinner.setError(getString(R.string.error_invalid_email));
            focusView = mAutoCompleteSpinner;
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
            UserCredentials creds = new UserCredentials();
            creds.emailID = mEmail;
            creds.password = mPassword;
            creds.universalDeviceRandomID = Installation.id(getActivity());

            NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
            creds.nfcCapable = (mNfcAdapter != null);
            Crashlytics.setBool(Helper.CRASHLYTICS_KEY_NFC, creds.nfcCapable);

            mCompositeSubscriptions.add(mLoginService.attemptLogin(creds)
                    .compose(this.<UserCredentialsResponse>applySchedulers())
                    .subscribe(new CacheSubscriber<UserCredentialsResponse>(getActivity()) {
                        @Override
                        public void onFail(Throwable e) {
                            super.onFail(e);
                            showProgress(false);
                        }

                        @Override
                        public void onSuccess(UserCredentialsResponse userCredentialsResponse) {
                            showProgress(false);
                            if (!TextUtils.isEmpty(userCredentialsResponse.message)) {
                                Toast.makeText(getActivity(), userCredentialsResponse.message, Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    Crashlytics.setUserEmail(mEmail.split("@")[0]);
                                } catch (Exception ignored) {

                                }
                                Account acc = new Account(mEmail, UserLoginActivity.ACCOUNT_TYPE);
                                Bundle b = new Bundle();
                                b.putString("url", userCredentialsResponse.url);
                                b.putString(UserLoginActivity.AUTHTOKEN_USERDATA_KEY, userCredentialsResponse.oauth);

                                GraabyApplication.getORMDbService().clearProfileInfo();
                                GraabyApplication.getORMDbService().addProfileInfo(mEmail);

                                mAccountMgr.addAccountExplicitly(acc, "welovegoogle", b);

                                GraabyNDEFCore.saveNfcData(getActivity(), userCredentialsResponse.core);

                                MainActivity.getGcmPreferences(getActivity()).edit().clear();

                                Intent intent = new Intent();
                                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mEmail);
                                intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE,
                                        UserLoginActivity.ACCOUNT_TYPE);
                                intent.putExtra(AccountManager.KEY_AUTHTOKEN, userCredentialsResponse.oauth);
                                intent.putExtra(UserLoginActivity.AUTHTOKEN_USERDATA_KEY, userCredentialsResponse.oauth);

                                loginActivity.onLoginSuccessful(intent);

                                Log.i("Graaby Account", "Account added successfully");
                            }
                        }
                    }));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        loginActivity = null;
    }

    public void loginAfterRegistering(String username, String password) {
        mAutoCompleteSpinner.setText(username);
        mPasswordView.setText(password);
        sendRequest();
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
                            if (mLoginStatusView != null)
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
                            if (mLoginStatusView != null)
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

    public interface LoginInterface {
        void onLoginSuccessful(Intent accountAuthenticatorResultIntent);

        void onNewUserRegisterRequest();
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
