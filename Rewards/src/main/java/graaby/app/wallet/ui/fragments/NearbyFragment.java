package graaby.app.wallet.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.GraabyNDEFCore;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.UserCredentialsResponse;
import graaby.app.wallet.nearby.NearbyPublish;
import graaby.app.wallet.nearby.NearbySubscribe;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.util.CacheSubscriber;
import rx.schedulers.Schedulers;


/**
 * Created by Akash.
 */
public class NearbyFragment extends BaseFragment {

    private static final String TAG = NearbyFragment.class.toString();
    @Bind(android.R.id.progress)
    ProgressBar mProgress;
    @Bind(R.id.status)
    TextView mStatusTextView;
    @Bind(R.id.nearby_scan)
    ToggleButton mNearbyBtn;
    @Inject
    Lazy<ProfileService> mCoreDataService;
    private NearbyPublish mNearbyPublisherStep1;
    private NearbySubscribe mNearbySubscriberStep2;
    private NearbyPublish mNearbyPublisherStep3;

    public static NearbyFragment newInstance() {
        return new NearbyFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_nearby);
        ButterKnife.bind(this, v);
        mNearbyBtn.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) startNearbySearch();
            else stopSearch();
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!GraabyNDEFCore.isCoreDataAvailable(getContext())) {
            mCompositeSubscriptions.add(
                    mCoreDataService.get().getNFCInfo().observeOn(Schedulers.newThread())
                            .subscribeOn(Schedulers.newThread())
                            .subscribe(new CacheSubscriber<UserCredentialsResponse.NFCData>(getActivity()) {
                                @Override
                                public void onSuccess(UserCredentialsResponse.NFCData result) {
                                    GraabyNDEFCore.saveNfcData(getActivity(), result);
                                    mNearbyBtn.setEnabled(true);
                                }
                            })
            );
        } else {
            mNearbyBtn.setEnabled(true);
        }
    }

    private void startNearbySearch() {
        try {
            GraabyNDEFCore.getGraabyUserID(getContext());
            mNearbyPublisherStep1 = new NearbyPublish(getActivity(), GraabyNDEFCore.getGraabyUserAsBytes(getContext()), () -> {
                setStatus("Scanning for Graaby devices");
                mProgress.setVisibility(View.VISIBLE);
            });
            mNearbySubscriberStep2 = new NearbySubscribe(getActivity(), new MessageListener() {
                @Override
                public void onFound(Message message) {
                    mNearbyPublisherStep1.onStop();
                    mProgress.setVisibility(View.VISIBLE);
                    setStatus("Device found, waiting to verify identity");
                    final String graabyUserID = new String(message.getContent(), Charset.forName("UTF8"));
                    try {
                        if (GraabyNDEFCore.getGraabyUserID(getContext()).equals(graabyUserID)) {
                            View parentView = getActivity().findViewById(R.id.container);
                            if (parentView != null) {
                                int textColor;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    textColor = getResources().getColor(R.color.emarald, getActivity().getTheme());
                                } else {
                                    textColor = getResources().getColor(R.color.emarald);
                                }
                                Snackbar.make(parentView, "Verify yourself by unlocking ->", Snackbar.LENGTH_INDEFINITE)
                                        .setActionTextColor(textColor)
                                        .setAction("Unlock", view -> {
                                            mNearbyPublisherStep3 = new NearbyPublish(getActivity(), graabyUserID, 3, 5);
                                            mNearbyPublisherStep3.onStart();
                                            mNearbyPublisherStep1.onStop();
                                            mNearbyPublisherStep3.onStop();
                                            setStatus("User verified, continue with checkout on the device");
                                            mProgress.setVisibility(View.INVISIBLE);

                                        }).show();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 2, null);
        } catch (IOException e) {
            Log.e(TAG, "No data available to publish");
            getActivity().finish();
        }
    }

    private void stopSearch() {
        if (mNearbyPublisherStep1 != null)
            mNearbyPublisherStep1.onStop();
        if (mNearbySubscriberStep2 != null)
            mNearbySubscriberStep2.onStop();
        if (mNearbyPublisherStep3 != null)
            mNearbyPublisherStep3.onStop();

        mProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mNearbyPublisherStep1 != null)
            mNearbyPublisherStep1.onStart();
        if (mNearbySubscriberStep2 != null)
            mNearbySubscriberStep2.onStart();
        if (mNearbyPublisherStep3 != null)
            mNearbyPublisherStep3.onStart();
    }

    @Override
    public void onStop() {
        stopSearch();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mNearbyPublisherStep1 = null;
        mNearbySubscriberStep2 = null;
        mNearbyPublisherStep3 = null;
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mNearbyPublisherStep1.onActivityResult(requestCode, resultCode, data);
        if (mNearbyPublisherStep3 != null)
            mNearbyPublisherStep3.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void sendRequest() {

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

    private void setStatus(String statusText) {
        getActivity().runOnUiThread(() -> mStatusTextView.setText(statusText));
    }
}
