package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;
import graaby.app.wallet.R;
import graaby.app.wallet.events.ToolbarEvents;
import graaby.app.wallet.models.retrofit.OutletDetail;
import graaby.app.wallet.models.retrofit.OutletDetailsRequest;
import graaby.app.wallet.network.services.BusinessService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.Helper;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by gaara on 8/4/14.
 */
public class BusinessDetailFragment extends BaseFragment {

    @InjectView(R.id.item_businessPicImageView)
    CircleImageView itemBusinessPicImageView;
    @InjectView(R.id.item_businessAddressTextView)
    TextView itemBusinessAddressTextView;
    @InjectView(R.id.business_points)
    TextView businessPoints;
    @InjectView(R.id.business_followers)
    TextView businessFollowers;
    @InjectView(R.id.business_checkins)
    TextView businessCheckins;
    @InjectView(R.id.points_earned_textView)
    TextView pointsEarnedTextView;
    @InjectView(R.id.profile_total_savings_textView)
    TextView profileTotalSavingsTextView;
    @InjectView(R.id.profile_checkins_textview)
    TextView profileCheckinsTextview;
    @InjectView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefresh;
    @Inject
    BusinessService mBusinessService;
    private BusinessDetailFragmentCallback mCallback;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (BusinessDetailFragmentCallback) activity;
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_business_detail);
        ButterKnife.inject(this, v);
        mSwipeRefresh.setColorSchemeResources(R.color.midnightblue, R.color.wetasphalt, R.color.asbestos, R.color.concrete);
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
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CacheSubscriber<OutletDetail>(getActivity(), mSwipeRefresh) {
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
                                           BusinessDetailFragment.this.getActivity().invalidateOptionsMenu();

                                           mCallback.onRewardDetailsLoaded(result.flatGraabyDiscountPercentage, result.punchards.punchCardRewards);
                                       }
                                   }
                        ));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_business_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (originalOutletDetail != null) {
            menu.findItem(R.id.action_menu_item_directions).setVisible(originalOutletDetail.latitude != 0);
            menu.findItem(R.id.action_menu_item_call).setVisible(!TextUtils.isEmpty(originalOutletDetail.phoneNumber));
            menu.findItem(R.id.action_menu_item_open_browser).setVisible(!TextUtils.isEmpty(originalOutletDetail.websiteURL));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_item_directions:

                String geoUri = null;
                geoUri = "http://maps.google.com/maps?f=d&daddr=" + originalOutletDetail.latitude + "," + originalOutletDetail.longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(geoUri));
                intent.setComponent(new ComponentName("com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity"));
                startActivity(intent);
                break;
            case R.id.action_menu_item_call:
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + originalOutletDetail.phoneNumber));
                startActivity(callIntent);
                break;
            case R.id.action_menu_item_open_browser:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + originalOutletDetail.websiteURL));
                startActivity(browserIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public interface BusinessDetailFragmentCallback {
        /**
         * Called when fragment has loaded all the punchcards.
         */
        void onRewardDetailsLoaded(Integer discount, List<OutletDetail.Reward> punches);
    }

}
