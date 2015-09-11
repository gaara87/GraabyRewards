package graaby.app.wallet.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import graaby.app.wallet.GraabyNDEFCore;
import graaby.app.wallet.nearby.NearbyPublish;


/**
 * Created by Akash.
 */
public class NearbyFragment extends BaseFragment {

    private static final String TAG = NearbyFragment.class.toString();
    private NearbyPublish mNearbyPublisher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mNearbyPublisher = new NearbyPublish(getActivity(), GraabyNDEFCore.getGraabyUserAsBytes(getContext()));
        } catch (IOException e) {
            Log.e(TAG, "No data available to publish");
            getActivity().finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mNearbyPublisher.onStart();
    }

    @Override
    public void onStop() {
        mNearbyPublisher.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mNearbyPublisher = null;
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mNearbyPublisher.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void sendRequest() {

    }

    @Override
    void setupInjections() {

    }


}
