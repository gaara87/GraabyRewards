package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.activities.SearchResultsActivity;
import graaby.app.wallet.events.LocationEvents;
import graaby.app.wallet.models.android.BusinessMarker;
import graaby.app.wallet.models.android.BusinessMarkerRenderer;
import graaby.app.wallet.models.realm.OutletDAO;
import graaby.app.wallet.models.retrofit.OutletDetail;
import graaby.app.wallet.models.retrofit.OutletsForBusinessRequest;
import graaby.app.wallet.models.retrofit.OutletsRequest;
import graaby.app.wallet.models.retrofit.OutletsResponse;
import graaby.app.wallet.network.services.BusinessService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.Helper;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class BusinessesFragment extends BaseFragment
        implements ClusterManager.OnClusterItemInfoWindowClickListener<BusinessMarker>,
        GoogleMap.OnMapLoadedCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final int REQUEST_CHECK_SETTINGS = 4013;
    private static final String TAG = BusinessesFragment.class.toString();
    GoogleMap mMap;
    @Inject
    BusinessService mBusinessService;
    @InjectView(R.id.map)
    MapView mapView;

    private ClusterManager<BusinessMarker> mClusterManager;
    private int mBrandId = Helper.DEFAULT_NON_BRAND_RELATED;
    private LatLng mLatLng;
    private LatLngBounds mLatLngBounds;

    private Set<Integer> outletsSet;
    private GoogleApiClient mGoogleAPIClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity.getClass() == MainActivity.class)
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(Helper.ARG_SECTION_NUMBER));
        mBrandId = getArguments().getInt(Helper.BRAND_ID_BUNDLE_KEY, Helper.DEFAULT_NON_BRAND_RELATED);
        setupLocationEnabler(activity);
    }

    private void setupLocationEnabler(Activity activity) {
        if (Helper.checkPlayServices(activity) && mBrandId == Helper.DEFAULT_NON_BRAND_RELATED) {
            mGoogleAPIClient = new GoogleApiClient.Builder(activity).addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleAPIClient.connect();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_business);
        ButterKnife.inject(this, inflatedView);

        mSwipeRefresh.setEnabled(false);
        setSwipeRefreshColors(R.color.wisteria, R.color.amethyst, R.color.holo_darkpurple, R.color.holo_lightpurple);

        mapView.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        initializeMap();

        return inflatedView;
    }

    @Override
    public void onMapLoaded() {
        loadMarkersFromDB(mBrandId);
    }

    private void loadMarkersFromDB(int brandID) {
        outletsSet = new HashSet<>();
        ListIterator<OutletDAO> iterator;
        if (brandID == Helper.DEFAULT_NON_BRAND_RELATED)
            iterator = GraabyApplication.getORMDbService().getAllOutlets().listIterator();
        else
            iterator = GraabyApplication.getORMDbService().getAllOutletsForBrand(brandID).listIterator();
        ArrayList<OutletDetail> details = new ArrayList<>();
        while (iterator.hasNext()) {
            OutletDAO outlet = iterator.next();
            details.add(new OutletDetail(outlet));
        }
        if (details.size() != 0)
            addOutletCollection(details);
    }

    @Override
    protected void sendRequest() {
        OutletsRequest request;
        if (mBrandId != Helper.DEFAULT_NON_BRAND_RELATED)
            request = new OutletsForBusinessRequest(mBrandId);
        else
            request = new OutletsRequest();

        if (mLatLng == null)
            return;

        request.latitude = mLatLng.latitude;
        request.longitude = mLatLng.longitude;

        mCompositeSubscriptions.add(mBusinessService.getOutletsAroundLocation(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CacheSubscriber<OutletsResponse>(getActivity()) {
                    @Override
                    public void onFail(Throwable e) {

                    }

                    @Override
                    public void onSuccess(OutletsResponse result) {
                        GraabyApplication.getORMDbService().addOutletsCollection(result.outlets);

                        addOutletCollection(result.outlets);
                    }
                }));
    }

    private void addOutletCollection(@NotNull List<OutletDetail> list) {
        LatLngBounds.Builder builder = LatLngBounds.builder();

        ArrayList<BusinessMarker> markers = new ArrayList<>();
        for (OutletDetail outlet : list) {
            if (outletsSet.contains(outlet.outletID))
                continue;
            else
                outletsSet.add(outlet.outletID);

            LatLng point = new LatLng(outlet.latitude, outlet.longitude);
            builder.include(point);
            BusinessMarker marker = new BusinessMarker(point, outlet);
            markers.add(marker);
        }

        if (mMap != null && markers.size() != 0) {
            mClusterManager.addItems(markers);
            mClusterManager.cluster();
            mLatLngBounds = builder.build();
        }
        moveMapCamera();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_business, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mBrandId != Helper.DEFAULT_NON_BRAND_RELATED) {
            menu.findItem(R.id.action_search).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_menu_item_refresh:
                prepareLocationRequest();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initializeMap() {
        if (mMap == null) {
            mMap = mapView.getMap();
            MapsInitializer.initialize(getActivity());
            mClusterManager = new ClusterManager<>(getActivity(), mMap);
            if (mMap != null) {
                mMap.setMyLocationEnabled(Boolean.TRUE);
                mMap.setOnCameraChangeListener(mClusterManager);
                mMap.setOnMarkerClickListener(mClusterManager);
                mMap.setOnInfoWindowClickListener(mClusterManager);
                mMap.setOnMapLoadedCallback(this);
                mClusterManager.setOnClusterItemInfoWindowClickListener(this);
                mClusterManager.setRenderer(new BusinessMarkerRenderer(getActivity(), mMap, mClusterManager));
            }
        } else {
            Toast.makeText(getActivity(), "Unable to load map", Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, TAG, "Unable to load map");
        }
    }

    private void moveMapCamera() {
        CameraUpdate cameraUpdate = null;
        if (mLatLng != null)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatLng, 13);
        else if (mLatLngBounds != null)
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(mLatLngBounds, 100);
        try {
            if (cameraUpdate != null)
                mMap.moveCamera(cameraUpdate);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setToolbarColors(R.color.wisteria, R.color.wisteria_darker);
        if (mapView != null)
            mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null)
            mapView.onDestroy();

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
//        locationManager.removeUpdates(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onClusterItemInfoWindowClick(BusinessMarker businessMarker) {
        OutletDetail place = businessMarker.getPlace();
        Helper.openBusiness(getActivity(), place);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        prepareLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleAPIClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Crashlytics.log(Log.ERROR, TAG, "Failed to connect to google api client, connection result error code:- " + connectionResult.getErrorCode());
    }

    private void prepareLocationRequest() {
        LocationRequest locationRequest = getLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        checkLocationSettings(builder);
    }

    private LocationRequest getLocationRequest() {
        return new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setNumUpdates(1);
    }

    private void checkLocationSettings(LocationSettingsRequest.Builder builder) {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleAPIClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
//                final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        getLastKnownWithRequest();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    getActivity(),
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Crashlytics.log("Location Settings N/A! WTH?");
                        break;
                }
            }
        });
    }

    private void getLastKnownWithRequest() {
        Location lastKnown = LocationServices.FusedLocationApi.getLastLocation(mGoogleAPIClient);
        if (lastKnown != null) {
            mLatLng = new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude());
            sendRequest();
            Log.d(TAG, "Last location:-" + mLatLng.latitude + "," + mLatLng.longitude);
        } else {
            Toast.makeText(getActivity(), "Finding location", Toast.LENGTH_LONG).show();
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleAPIClient
                            , getLocationRequest()
                            , new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            moveMapCamera();
                            sendRequest();
                            Log.d(TAG, "New location:-" + mLatLng.latitude + "," + mLatLng.longitude);
                        }
                    });
        }

    }

    public void onEvent(LocationEvents.LocationEnabled event) {
        getLastKnownWithRequest();
    }
}

