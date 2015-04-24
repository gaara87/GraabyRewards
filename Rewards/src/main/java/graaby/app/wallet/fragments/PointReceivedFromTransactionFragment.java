package graaby.app.wallet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.RatingRequest;
import graaby.app.wallet.network.services.BusinessService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.Helper;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by gaara on 9/1/14.
 */
public class PointReceivedFromTransactionFragment extends BaseFragment implements RatingBar.OnRatingBarChangeListener {
    @InjectView(R.id.timestamp)
    TextView timestamp;
    @InjectView(R.id.name)
    TextView nameTextView;
    @InjectView(R.id.points_textView)
    TextView pointsTextView;
    @InjectView(R.id.ratingBar)
    RatingBar ratingBar;
    @Inject
    BusinessService mBusinessService;
    private Long mTimeStamp;
    private int mAmount;
    private String mBusinessName, mActivityID;
    private boolean mCheckin = false;

    public PointReceivedFromTransactionFragment() {

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String info = getArguments().getString(
                Helper.INTENT_CONTAINER_INFO);
        try {
            JSONObject pointNode = new JSONObject(info);
            mBusinessName = pointNode.getString(getString(R.string.field_business_name));
            mAmount = pointNode.getInt(getString(R.string.contact_send_amount));
            mTimeStamp = pointNode.getLong(getString(R.string.timestamp));
            mActivityID = pointNode.getString(getString(R.string.field_gcm_txid));
            mCheckin = pointNode.getBoolean(getString(R.string.field_gcm_checkin));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void sendRequest() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_point_received_from_tx);
        ButterKnife.inject(this, rootView);

        mSwipeRefresh.setEnabled(false);

        pointsTextView.setText(String.valueOf(mAmount));
        nameTextView.setText(mBusinessName);
        if (mCheckin) {
            nameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checkin, 0);
        }

        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
        timestamp.setText(sdf.format(new Date(mTimeStamp)));


        ratingBar.setOnRatingBarChangeListener(this);
        return rootView;
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        mCompositeSubscriptions.add(
                mBusinessService.rateUserTransactionForBusiness(new RatingRequest(mActivityID, ratingBar.getRating()))
                        .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CacheSubscriber<BaseResponse>(getActivity()) {
                            @Override
                            public void onFail(Throwable e) {

                            }

                            @Override
                            public void onSuccess(BaseResponse result) {
                                if (result.responseSuccessCode == 1) {
                                    Toast.makeText(getActivity(), "Rating saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
