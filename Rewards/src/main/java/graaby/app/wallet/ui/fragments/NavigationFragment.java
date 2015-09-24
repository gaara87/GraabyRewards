package graaby.app.wallet.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.hdodenhof.circleimageview.CircleImageView;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.database.ORMService;
import graaby.app.wallet.events.AuthEvents;
import graaby.app.wallet.events.ProfileEvents;
import graaby.app.wallet.models.realm.ProfileDAO;
import graaby.app.wallet.models.retrofit.ProfileNavResponse;
import graaby.app.wallet.network.services.ProfileService;
import graaby.app.wallet.ui.activities.FeedActivity;
import graaby.app.wallet.ui.activities.MarketActivity;
import graaby.app.wallet.ui.activities.ProfileActivity;
import graaby.app.wallet.ui.activities.SettingsActivity;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.DiscountItemType;
import graaby.app.wallet.util.Helper;
import rx.Subscription;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class NavigationFragment extends BaseFragment implements NavigationView.OnNavigationItemSelectedListener {
    @Bind(R.id.navigation_drawer_profile_photo)
    CircleImageView navigationDrawerProfilePhoto;
    @Bind(R.id.navigation_drawer_name)
    TextView navigationDrawerName;
    @Bind(R.id.navigation_drawer_email)
    TextView navigationDrawerEmail;
    @Bind(R.id.navigation_drawer_points)
    TextView navigationDrawerPoints;

    @Inject
    ProfileService mProfileService;
    @Inject
    ORMService mOrmService;

    public NavigationFragment() {
        // Required empty public constructor
    }

    public static NavigationFragment newInstance() {
        return new NavigationFragment();
    }

    @Override
    protected void sendRequest() {
        Subscription subscriber = mProfileService.getProfileNavInfo()
                .compose(this.<ProfileNavResponse>applySchedulers())
                .subscribe(new CacheSubscriber<ProfileNavResponse>(getActivity(), mSwipeRefresh) {
                    @Override
                    public void onSuccess(ProfileNavResponse result) {
                        mOrmService.updateProfileInfo(result.userFullName, result.profilePictureURL, result.currentPoints);
                        ProfileDAO profile = mOrmService.getProfileInfo();
                        if (profile != null) {
                            navigationDrawerName.setText(profile.getFullName());
                            navigationDrawerEmail.setText(profile.getEmail());
                            navigationDrawerPoints.setText(profile.getCurrentPoints());
                            Glide.with(navigationDrawerProfilePhoto.getContext())
                                    .load(profile.getPictureURL())
                                    .crossFade()
                                    .into(navigationDrawerProfilePhoto);
                        }
                    }
                });

        mCompositeSubscriptions.add(subscriber);
    }

    @Override
    void setupInjections() {
        GraabyApplication.getApplication().getApiComponent().inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    public void attachNavigationView(NavigationView view) {
        // Inflate the layout for this fragment
        ButterKnife.bind(this, view);
        ProfileDAO profile = mOrmService.getProfileInfo();
        if (profile != null) {
            navigationDrawerName.setText(profile.getFullName());
            navigationDrawerEmail.setText(profile.getEmail());
            navigationDrawerPoints.setText(profile.getCurrentPoints());
            Glide.with(navigationDrawerProfilePhoto.getContext())
                    .load(profile.getPictureURL())
                    .crossFade()
                    .into(navigationDrawerProfilePhoto);
        }
        view.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        ButterKnife.unbind(this);
    }

    @Subscribe(sticky = true)
    public void handle(ProfileEvents.NameUpdatedEvent event) {
        ProfileDAO profile = mOrmService.getProfileInfo();
        if (profile != null) {
            navigationDrawerName.setText(profile.getFullName());
            navigationDrawerEmail.setText(profile.getEmail());
            navigationDrawerPoints.setText(profile.getCurrentPoints());
        }
    }

    @Subscribe(sticky = true)
    public void handle(ProfileEvents.PictureUpdatedEvent event) {
        Glide.with(navigationDrawerProfilePhoto.getContext())
                .load(event.getImageURL())
                .crossFade()
                .into(navigationDrawerProfilePhoto);
    }

    @Subscribe(sticky = true)
    public void handle(AuthEvents.SessionAuthenticatedEvent event) {
        sendRequest();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.drawer_profile:
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                getActivity().startActivity(intent);
                return false;
            case R.id.drawer_my_vouchers:
                intent = new Intent(getActivity(), MarketActivity.class);
                intent.putExtra(Helper.KEY_TYPE,
                        DiscountItemType.VOUCHERS);
                getActivity().startActivity(intent);
                return false;
            case R.id.drawer_feeds:
                intent = new Intent(getActivity(), FeedActivity.class);
                getActivity().startActivity(intent);
                return false;
            case R.id.drawer_feedback:
                Helper.sendFeedback(getActivity());
                return false;
            case R.id.drawer_settings:
                intent = new Intent(getActivity(), SettingsActivity.class);
                getActivity().startActivityForResult(intent, 10);
                return true;
        }
        return true;
    }
}
