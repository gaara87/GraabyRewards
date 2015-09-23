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

import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.GraabyNDEFCore;
import graaby.app.wallet.R;
import graaby.app.wallet.nearby.NearbyPublish;
import graaby.app.wallet.nearby.NearbySubscribe;


/**
 * Created by Akash.
 */
public class NearbyFragment extends BaseFragment {

    private static final String TAG = NearbyFragment.class.toString();
    @Bind(android.R.id.progress)
    ProgressBar mProgress;
    @Bind(R.id.status)
    TextView mStatusTextView;
    private NearbyPublish mNearbyPublisherStep1;
    private NearbySubscribe mNearbySubscriberStep2;
    private NearbyPublish mNearbyPublisherStep3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_nearby);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            GraabyNDEFCore.getGraabyUserID(getContext());
            mNearbyPublisherStep1 = new NearbyPublish(getActivity(), GraabyNDEFCore.getGraabyUserAsBytes(getContext()), () -> {
                setStatus("Scanning for Graaby devices");
            });
            mNearbySubscriberStep2 = new NearbySubscribe(getActivity(), new MessageListener() {
                @Override
                public void onFound(Message message) {
                    mNearbyPublisherStep1.onStop();
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
                                            mProgress.setVisibility(View.GONE);
                                            new Timer().schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    getActivity().finish();
                                                }
                                            }, 1000);

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
        if (mNearbyPublisherStep1 != null)
            mNearbyPublisherStep1.onStop();
        if (mNearbySubscriberStep2 != null)
            mNearbySubscriberStep2.onStop();
        if (mNearbyPublisherStep3 != null)
            mNearbyPublisherStep3.onStop();
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
