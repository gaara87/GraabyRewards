package graaby.app.wallet.ui.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bumptech.glide.Glide;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.events.ToolbarEvents;
import graaby.app.wallet.models.retrofit.OutletDetail;
import graaby.app.wallet.models.retrofit.OutletDetailsRequest;
import graaby.app.wallet.network.services.BusinessService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.Helper;

/**
 * Created by gaara on 8/4/14.
 */
public class BusinessDetailFragment extends BaseFragment {

    @Bind(R.id.item_businessPicImageView)
    ImageView itemBusinessPicImageView;
    @Bind(R.id.item_businessAddressTextView)
    TextView itemBusinessAddressTextView;
    @Bind(R.id.business_points)
    TextView businessPoints;
    @Bind(R.id.business_followers)
    TextView businessFollowers;
    @Bind(R.id.business_checkins)
    TextView businessCheckins;
    @Bind(R.id.points_earned_textView)
    TextView pointsEarnedTextView;
    @Bind(R.id.profile_total_savings_textView)
    TextView profileTotalSavingsTextView;
    @Bind(R.id.profile_checkins_textview)
    TextView profileCheckinsTextview;
    @Bind(R.id.punch_container)
    LinearLayout mPunchLayout;
    @Bind(android.R.id.empty)
    View empty;
    @Bind(R.id.business_reward_discount_value)
    TextView mDiscountTextView;
    @Bind(R.id.business_call)
    Button mCall;
    @Bind(R.id.business_directions)
    Button mDirections;
    @Bind(R.id.business_site)
    Button mWebsite;
    @Inject
    BusinessService mBusinessService;
    private OutletDetail originalOutletDetail;

    public BusinessDetailFragment() {

    }

    public static BusinessDetailFragment newInstance(OutletDetail outletData) throws IOException {
        BusinessDetailFragment fragment = new BusinessDetailFragment();
        Bundle args = new Bundle();
        args.putString(Helper.INTENT_CONTAINER_INFO, LoganSquare.serialize(outletData));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        if (getArguments() != null) {
            try {
                originalOutletDetail = LoganSquare.parse(getArguments().getString(
                        Helper.INTENT_CONTAINER_INFO), OutletDetail.class);
                EventBus.getDefault().post(new ToolbarEvents.SetTitle(originalOutletDetail.businessName));
            } catch (IOException e) {
                e.printStackTrace();
                getActivity().finish();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_business_detail);
        ButterKnife.bind(this, getActivity().findViewById(R.id.main_content));
        setSwipeRefreshColors(R.color.midnightblue, R.color.wetasphalt, R.color.asbestos, R.color.concrete);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sendRequest();
    }

    @Override
    protected void sendRequest() {
        mCompositeSubscriptions.add(
                mBusinessService.getOutletDetails(new OutletDetailsRequest(originalOutletDetail.outletID))
                        .compose(this.<OutletDetail>applySchedulers())
                        .subscribe(new CacheSubscriber<OutletDetail>(getActivity()) {
                                       @Override
                                       public void onSuccess(OutletDetail result) {

                                           Glide.with(getActivity())
                                                   .load(result.pictureURL)
                                                   .crossFade()
                                                   .placeholder(R.drawable.default_business_profile_image)
                                                   .into(itemBusinessPicImageView);

                                           OutletDetail.Stats statsObject = result.outletStatistics;
                                           businessPoints.setText(statsObject.pointsGivenOut);

                                           businessFollowers.setText(statsObject.numberOfUsersFollowing);

                                           businessCheckins.setText(statsObject.totalCheckins);

                                           pointsEarnedTextView.setText(statsObject.totalPointsEarnedInOutlet);

                                           profileTotalSavingsTextView.setText(statsObject.totalSavingsInOutlet);

                                           profileCheckinsTextview.setText(statsObject.totalCheckinsInOutlet);
                                           if (!TextUtils.isEmpty(result.outletName)) {
                                               EventBus.getDefault().post(new ToolbarEvents.SetTitle(result.outletName));
                                           }
                                           if (!TextUtils.isEmpty(result.businessName)) {
                                               EventBus.getDefault().post(new ToolbarEvents.SetTitle(result.businessName));
                                           }

                                           itemBusinessAddressTextView.setText(result.outletAddress);

                                           originalOutletDetail.phoneNumber = result.phoneNumber;
                                           originalOutletDetail.websiteURL = result.websiteURL;
                                           mDirections.setVisibility((originalOutletDetail.latitude != 0) ? View.VISIBLE : View.INVISIBLE);
                                           mCall.setVisibility((!TextUtils.isEmpty(originalOutletDetail.phoneNumber)) ? View.VISIBLE : View.INVISIBLE);
                                           mWebsite.setVisibility((!TextUtils.isEmpty(originalOutletDetail.websiteURL)) ? View.VISIBLE : View.INVISIBLE);
                                           mDiscountTextView.setText(String.valueOf(result.flatGraabyDiscountPercentage) + "%");

                                           if (result.punchards == null || result.punchards.punchCardRewards == null || result.punchards.punchCardRewards.size() == 0) {
                                               empty.setVisibility(View.VISIBLE);
                                           } else {
                                               LayoutInflater inflater = LayoutInflater.from(getContext());
                                               for (OutletDetail.Reward reward : result.punchards.punchCardRewards) {
                                                   View punchRewardLayout = inflater.inflate(R.layout.item_list_reward_info, null);
                                                   ViewHolder holder = new ViewHolder(punchRewardLayout);
                                                   holder.text1.setText(reward.rewardDetail);
                                                   holder.text2.setText(reward.onVisitCount);
                                                   mPunchLayout.addView(punchRewardLayout);
                                               }
                                           }
                                       }
                                   }
                        ));
    }

    @Override
    void setupInjections() {
        GraabyApplication.getApplication().getApiComponent().inject(this);
    }

    @OnClick(R.id.business_call)
    public void onBusinessCall() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + originalOutletDetail.phoneNumber));
        startActivity(callIntent);
    }

    @OnClick(R.id.business_directions)
    public void onBusinessDirections() {
        String geoUri = null;
        geoUri = "http://maps.google.com/maps?f=d&daddr=" + originalOutletDetail.latitude + "," + originalOutletDetail.longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(geoUri));
        intent.setComponent(new ComponentName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"));
        startActivity(intent);
    }

    @OnClick(R.id.business_site)
    public void onBusinessSite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + originalOutletDetail.websiteURL));
        startActivity(browserIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    class ViewHolder {
        @Bind(android.R.id.text1)
        TextView text1;
        @Bind(android.R.id.text2)
        TextView text2;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
