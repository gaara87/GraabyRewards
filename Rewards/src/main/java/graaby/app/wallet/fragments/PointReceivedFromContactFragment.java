package graaby.app.wallet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.events.ToolbarEvents;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.ThankContactRequest;
import graaby.app.wallet.network.services.ContactService;
import graaby.app.wallet.util.Helper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class PointReceivedFromContactFragment extends BaseFragment {

    @Bind(R.id.contacts_userProfilePicImageView)
    ImageView contactsUserProfilePicImageView;
    @Bind(R.id.points_textView)
    TextView pointsTextView;
    @Bind(R.id.btn_thanks)
    Button btnThanks;
    @Inject
    ContactService mContactService;
    private int mActivityID, mAmount;
    private String pictureURL;

    public PointReceivedFromContactFragment() {
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
            EventBus.getDefault().post(new ToolbarEvents.SetTitle(pointNode.getString(getString(R.string.field_gcm_name))));
            mAmount = pointNode.getInt(getString(R.string.contact_send_amount));
            mActivityID = pointNode.getInt(getString(R.string.field_gcm_thank_id));
            pictureURL = pointNode.getString(getString(
                    R.string.pic_url));
        } catch (JSONException e) {
        }
    }

    @Override
    protected void sendRequest() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_point_received);
        ButterKnife.bind(this, rootView);
        mSwipeRefresh.setEnabled(Boolean.FALSE);

        Glide.with(getActivity())
                .load(pictureURL)
                .centerCrop()
                .placeholder(R.drawable.nav_profile_man)
                .crossFade()
                .into(contactsUserProfilePicImageView);


        pointsTextView.setText(String.valueOf(mAmount));


        return rootView;
    }

    @OnClick(R.id.btn_thanks)
    public void thankYouButtonClicked(final View buttonView) {
        buttonView.setEnabled(Boolean.FALSE);
        try {
            JSONObject params = new JSONObject();
            params.put(getResources().getString(R.string.field_gcm_thank_id), mActivityID);

            mContactService.thankContact(new ThankContactRequest(mActivityID), new Callback<BaseResponse>() {
                @Override
                public void success(BaseResponse baseResponse, Response response) {
                    if (baseResponse.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success))) {
                        ((Button) buttonView).setText("Sent");
                        Timer timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (getActivity() != null)
                                            getActivity().finish();
                                    }
                                }, 3000);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    buttonView.setEnabled(Boolean.TRUE);
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
