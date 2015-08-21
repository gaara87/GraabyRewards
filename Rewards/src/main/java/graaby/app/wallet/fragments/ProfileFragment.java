package graaby.app.wallet.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.activities.MarketActivity;
import graaby.app.wallet.events.ProfileEvents;
import graaby.app.wallet.models.retrofit.ProfileResponse;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.util.ActivityType;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.DiscountItemType;
import graaby.app.wallet.util.Helper;
import rx.Subscription;

public class ProfileFragment extends BaseFragment {

    @Bind(R.id.buy_tag_card)
    Button mBuyTagCardView;
    @Bind(R.id.points_textView)
    TextView mPointsTextView;
    @Bind(R.id.profile_total_savings_textView)
    TextView mProfileTotalSavingsTextView;
    @Bind(R.id.profile_total_vouchers_textView)
    TextView mProfileTotalVouchersTextView;
    @Bind(R.id.profile_total_coupons_textView)
    TextView mProfileTotalCouponsTextView;
    @Bind(R.id.profile_checkins_textview)
    TextView mProfileCheckinsTextview;
    @Bind(R.id.profile_following_textview)
    TextView mProfileFollowingTextview;
    @Bind(R.id.profile_connections_textview)
    TextView mProfileConnectionsTextview;
    @Bind(R.id.profile_recents)
    LinearLayout mProfileRecents;
    @Inject
    ProfileService mProfileService;

    private ViewBusinessesListener mCallback;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (ViewBusinessesListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
        setToolbarColors(R.color.wetasphalt, R.color.wetasphalt_darker);
    }

    @Override
    protected void sendRequest() {
        Subscription subscriber = mProfileService.getProfileInfo()
                .compose(this.<ProfileResponse>applySchedulers())
                .subscribe(new CacheSubscriber<ProfileResponse>(getActivity(), mSwipeRefresh) {
                    @Override
                    public void onSuccess(ProfileResponse result) {
                        refreshDetails(result);
                    }
                });

        mCompositeSubscriptions.add(subscriber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_profile);
        ButterKnife.bind(this, v);
        setSwipeRefreshColors(R.color.emarald, R.color.peterriver, R.color.wisteria, R.color.sunflower);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.profile_coupons_viewall_card, R.id.profile_vouchers_viewall_card})
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), MarketActivity.class);
        switch (v.getId()) {
            case R.id.profile_coupons_viewall_card:
                intent.putExtra(Helper.KEY_TYPE,
                        DiscountItemType.COUPONS);
                break;
            case R.id.profile_vouchers_viewall_card:
                intent.putExtra(Helper.KEY_TYPE,
                        DiscountItemType.VOUCHERS);
                break;
        }
        startActivity(intent);
    }

    @OnClick(R.id.buy_tag_card)
    public void buyGraabyTag() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setTitle("Graaby Tag");
        dialog.setContentView(R.layout.dialog_buy_tag);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        dialog.findViewById(R.id.view_outlets).setOnClickListener(view -> {
            dialog.dismiss();
            mCallback.onViewBusinessesRequest();
        });
    }

    private void refreshDetails(ProfileResponse profile) {
        if (profile.userBiography != null) {
            GraabyApplication.getORMDbService().updateProfileInfo(profile.userBiography.name, profile.userBiography.profilePicURL, profile.pointStatistics.currentPointBalance);
            EventBus.getDefault().postSticky(new ProfileEvents.NameUpdatedEvent());
            EventBus.getDefault().postSticky(new ProfileEvents.PictureUpdatedEvent(profile.userBiography.profilePicURL));

            if (profile.userBiography.getIsTagAssociatedWithAccount()) {
                mBuyTagCardView.setVisibility(View.GONE);
            } else {
                mBuyTagCardView.setVisibility(View.VISIBLE);
            }
        }
        if (profile.pointStatistics != null) {
            mPointsTextView.setText(profile.pointStatistics.currentPointBalance);
            mProfileTotalSavingsTextView.setText(profile.pointStatistics.cumulativeSavingsTotal);
            mProfileConnectionsTextview.setText(profile.pointStatistics.totalNumberOfContacts);
            mProfileCheckinsTextview.setText(profile.pointStatistics.totalCheckinCount);
            mProfileFollowingTextview.setText(profile.pointStatistics.totalBusinessUserIsFollowingCount);
            mProfileTotalCouponsTextView.setText(profile.pointStatistics.totalValidCouponsCount);
            mProfileTotalVouchersTextView.setText(profile.pointStatistics.totalValidVouchersCount);
        }
        if (profile.recentActivities != null) {
            mProfileRecents.removeViews(1, mProfileRecents.getChildCount() - 1);

            for (int i = 0; i < profile.recentActivities.size(); i++) {
                View v = getActivity().getLayoutInflater().inflate(
                        R.layout.profile_recent_details, null);
                TextView tv = (TextView) v
                        .findViewById(R.id.profile_recent_transactions_item_text_view);
                ProfileResponse.RecentUserActivityDetails details = profile.recentActivities.get(i);
                tv.setText(details.detail);

                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        ActivityType.getDrawableResourceIDForActivity(details.type), 0);
                mProfileRecents.addView(v);
            }

        }
    }

    public interface ViewBusinessesListener {
        void onViewBusinessesRequest();
    }
}
