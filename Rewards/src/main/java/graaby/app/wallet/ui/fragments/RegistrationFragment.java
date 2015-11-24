package graaby.app.wallet.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Min;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Pattern;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.auth.UserLoginActivity;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.RegistrationRequest;
import graaby.app.wallet.network.services.AuthService;
import graaby.app.wallet.ui.activities.OnboardingActivity;
import graaby.app.wallet.util.CacheSubscriber;

public class RegistrationFragment extends BaseFragment implements Validator.ValidationListener, CompoundButton.OnCheckedChangeListener {

    @Bind(R.id.email)
    AppCompatSpinner mSpinner;

    @Password(scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS, message = "Minimum 6 characters")
    @Bind(R.id.password)
    EditText mPasswordView;

    @Min(value = 3, message = "Minimum 3 characters")
    @Bind(R.id.login_first_name)
    EditText mFirstNameView;

    @Min(value = 3, message = "Minimum 3 characters")
    @Bind(R.id.login_last_name)
    EditText mLastNameView;

    @Bind(R.id.phone)
    @Pattern(regex = "(\\d{10}|\\d{0})", message = "10 digit number only")
    EditText mPhoneNumber;

    @Bind(R.id.password_switch)
    SwitchCompat mPasswordSwitch;

    @Bind(R.id.verification_message)
    TextView mVerificationMessage;
    @Bind(R.id.register_status)
    View mRegisterStatusView;
    @Bind(R.id.register_form)
    View mRegisterFormView;
    @Inject
    AuthService mAuthService;
    private Validator mValidator;
    private OnRegistrationListener mCallback;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GraabyApplication.getApplication().getApiComponent().inject(this);
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
    }

    @Override
    protected void sendRequest() {
        RegistrationRequest request = new RegistrationRequest();
        request.email = mSpinner.getSelectedItem().toString();
        request.password = mPasswordView.getText().toString();
        request.firstName = mFirstNameView.getText().toString();
        request.lastName = mLastNameView.getText().toString();
        request.phoneNumber = mPhoneNumber.getText().toString();
        request.isPhoneNumberVerified = false;

        //TODO: get google server side authtoken

        mCompositeSubscriptions.add(
                mAuthService.attemptRegister(request)
                        .compose(this.<BaseResponse>applySchedulers())
                        .subscribe(new CacheSubscriber<BaseResponse>(getActivity()) {

                            @Override
                            public void onFail(Throwable e) {
                                super.onFail(e);
                                showProgress(false);
                            }

                            @Override
                            public void onSuccess(BaseResponse baseResponse) {
                                if (baseResponse.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success))) {
                                    show("Registered successfully!");
                                    showProgress(false);
                                    mCallback.onRegistrationComplete(mSpinner.getSelectedItem().toString(), mPasswordView.getText().toString());
                                } else {
                                    show(baseResponse.message);
                                    showProgress(false);
                                    Toast.makeText(getActivity(), baseResponse.message, Toast.LENGTH_LONG).show();
                                }
                            }
                        }));
        show("Attempting to register");
        showProgress(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_registration, container, false);
        ButterKnife.bind(this, rootView);
        final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, UserLoginActivity.getAccountNames(getActivity()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.requestFocus();
        mPasswordSwitch.setOnCheckedChangeListener(this);
        return rootView;
    }

    @OnClick(R.id.about_graaby)
    public void onAboutGraabyClick() {
        startActivity(new Intent(getActivity(), OnboardingActivity.class));
    }

    @OnClick(R.id.register_button)
    public void onRegisterClick() {
        mValidator.validate();
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mCallback = (OnRegistrationListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    void setupInjections() {
        GraabyApplication.getApplication().getApiComponent().inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mRegisterStatusView.setVisibility(View.VISIBLE);
            mRegisterStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mRegisterStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mRegisterFormView.setVisibility(View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mRegisterFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void show(final String message) {
        getActivity().runOnUiThread(() -> mVerificationMessage.setText(message));
    }

    @Override
    public void onValidationSucceeded() {
        sendRequest();
    }

    @Override
    public void onValidationFailed(List<ValidationError> validationErrors) {
        for (ValidationError error : validationErrors) {
            ((EditText) error.getView()).setError(error.getCollatedErrorMessage(getActivity()));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            mPasswordView.setTransformationMethod(null);
            mPasswordSwitch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_visibility, 0, 0, 0);
        } else {
            mPasswordView.setTransformationMethod(new PasswordTransformationMethod());
            mPasswordSwitch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_visibility_off, 0, 0, 0);
        }
    }

    public interface OnRegistrationListener {
        void onRegistrationComplete(String email, String password);
    }


}
