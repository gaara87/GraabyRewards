package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;
import graaby.app.wallet.R;
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
    @InjectView(R.id.item_businessNameTextView)
    TextView itemBusinessNameTextView;
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
                        .subscribe(new CacheSubscriber<OutletDetail>(getActivity()) {
                                       @Override
                                       public void onFail(Throwable e) {
                                           mSwipeRefresh.setRefreshing(false);
                                       }

                                       @Override
                                       public void onSuccess(OutletDetail result) {
                                           mSwipeRefresh.setRefreshing(false);

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
                                           if (!TextUtils.isEmpty(result.outletName))
                                               itemBusinessNameTextView.setText(result.outletName);
                                           if (!TextUtils.isEmpty(result.businessName))
                                               itemBusinessNameTextView.setText(result.businessName);

                                           itemBusinessAddressTextView.setText(result.outletAddress);

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
            menu.findItem(R.id.action_menu_item_directions).setEnabled(originalOutletDetail.latitude != 0);
            menu.findItem(R.id.action_menu_item_call).setEnabled(!TextUtils.isEmpty(originalOutletDetail.phoneNumber));
            menu.findItem(R.id.action_menu_item_open_browser).setEnabled(!TextUtils.isEmpty(originalOutletDetail.websiteURL));
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

    public static class RewardDetailsFragment extends Fragment {

        private ListView mListView;
        private TextView mDiscountTextView;

        public RewardDetailsFragment() {

        }

        public static RewardDetailsFragment newInstance() {
            return new RewardDetailsFragment();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_reward_info, null);
            mListView = (ListView) v.findViewById(android.R.id.list);
            mListView.setEmptyView(v.findViewById(android.R.id.empty));
            mDiscountTextView = (TextView) v.findViewById(R.id.business_reward_discount_value);
            return v;
        }

        public void setPunchCards(Context context, Integer discount, List<OutletDetail.Reward> punches) {
            mDiscountTextView.setText(String.valueOf(discount) + "%");
            mListView.setAdapter(new PunchAdapter(context, punches));
        }

        private class PunchAdapter extends ArrayAdapter<OutletDetail.Reward> {

            private LayoutInflater inflater;

            public PunchAdapter(Context context, List<OutletDetail.Reward> punches) {
                super(context, R.layout.fragment_punchcards, punches);
                inflater = LayoutInflater.from(getContext());
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = inflater.inflate(R.layout.item_list_reward_info, null);
                OutletDetail.Reward node = getItem(position);

                TextView tv;
                tv = (TextView) convertView.findViewById(android.R.id.text1);
                tv.setText(node.rewardDetail);
                tv = (TextView) convertView.findViewById(android.R.id.text2);
                tv.setText(node.onVisitCount);

                return convertView;
            }
        }
    }
}
