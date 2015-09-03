package graaby.app.wallet.ui.fragments;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.GraabyApplication;
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
    @Bind(R.id.timestamp)
    TextView timestamp;
    @Bind(R.id.name)
    TextView nameTextView;
    @Bind(R.id.points_textView)
    TextView pointsTextView;
    @Bind(R.id.ratingBar)
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
    void setupInjections() {
        GraabyApplication.getApplication().getApiComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_point_received_from_tx);
        ButterKnife.bind(this, rootView);

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
                        .subscribe(new CacheSubscriber<BaseResponse>(getActivity(), mSwipeRefresh) {
                            @Override
                            public void onSuccess(BaseResponse result) {
                                if (result.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success))) {
                                    Toast.makeText(getActivity(), "Rating saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
