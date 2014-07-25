package graaby.app.wallet.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.adapter.BusinessesAdapter;
import graaby.app.wallet.model.BusinessMarker;
import graaby.app.wallet.model.BusinessMarkerRenderer;

public class BusinessesFragment extends Fragment implements
        OnItemClickListener, ErrorListener,
        Listener<JSONObject>, LocationListener, ClusterManager.OnClusterItemInfoWindowClickListener<BusinessMarker>, SwipeRefreshLayout.OnRefreshListener {
    MapView mapView;
    GoogleMap mMap;

    private Bundle mBundle;
    private String fieldNameLongitude, fieldNameLatitude, fieldNameBrandID;

    private ListView listView;
    private Button enableLocationButton;
    private SwipeRefreshLayout mPullToRefreshLayout;

    private CustomRequest businessesRequest;
    private BusinessesAdapter mAdapter;
    private Activity mActivity;
    private LocationManager locationManager;
    private Boolean isLocationEnabled = Boolean.FALSE;

    private ClusterManager<BusinessMarker> mClusterManager;

    private Integer mBrandId = -1;
    private LatLng mLatLng = new LatLng(0, 0);
    private LatLngBounds mLatLngBounds;
    ;

    private boolean isLocationEnabled() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isLocationEnabled = Boolean.TRUE;
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 20, this);
            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnown != null) {
                mLatLng = new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude());
            }
        } else {
            isLocationEnabled = Boolean.FALSE;
        }
        return isLocationEnabled;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        if (activity.getClass() == MainActivity.class)
            ((MainActivity) mActivity).onSectionAttached(
                    getArguments().getInt(Helper.ARG_SECTION_NUMBER));
        mBrandId = getArguments().getInt(Helper.BRAND_ID_BUNDLE_KEY, -1);
        locationManager = (LocationManager) mActivity
                .getSystemService(Context.LOCATION_SERVICE);

        isLocationEnabled();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        fieldNameLongitude = getString(
                R.string.field_business_longitude);
        fieldNameLatitude = getString(
                R.string.field_business_latitude);
        fieldNameBrandID = getString(R.string.field_business_id);
        mAdapter = new BusinessesAdapter(mActivity, new ArrayList<JSONObject>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_business, container,
                false);

        mPullToRefreshLayout = (SwipeRefreshLayout) inflatedView.findViewById(R.id.swiperefresh);
        mPullToRefreshLayout.setOnRefreshListener(this);
        mPullToRefreshLayout.setEnabled(false);
        mPullToRefreshLayout.setColorSchemeResources(R.color.wisteria, R.color.amethyst, R.color.holo_darkpurple, R.color.holo_lightpurple);
        listView = (ListView) inflatedView.findViewById(R.id.business_listview);
        listView.setOnItemClickListener(this);
        listView.setAdapter(mAdapter);
        if (!isLocationEnabled && mBrandId == -1) {
            enableLocationButton = (Button) inflatedView.findViewById(R.id.business_enable_location_btn);
            enableLocationButton.setVisibility(View.VISIBLE);
            enableLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
        }
        mapView = (MapView) inflatedView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        this.setHasOptionsMenu(isLocationEnabled);
        setUpMapIfNeeded();

        return inflatedView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendRequest();
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put(fieldNameLatitude, mLatLng.latitude);
        params.put(fieldNameLongitude, mLatLng.longitude);
        if (mBrandId != -1) {
            params.put(fieldNameBrandID, mBrandId);
        }
        try {
            businessesRequest = new CustomRequest("stores", params, this, this);
            if (mBrandId == -1) {
                Cache.Entry entry = Helper.getRQ().getCache().get(businessesRequest.getCacheKey());
                if (entry != null) {
                    JSONObject jsonCachedResponse = CustomRequest.getCachedResponse(entry);
                    if (jsonCachedResponse != null) {
                        onResponse(jsonCachedResponse);
                    }
                }
            }
            Helper.getRQ().add(businessesRequest);
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
        } catch (JSONException e) {

        } catch (NullPointerException npe) {
        } finally {
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            JSONArray placesArray = response.getJSONArray(getString(R.string.field_business_places));
            if (placesArray.length() != 0) {
                mAdapter.clear();
                if (mMap != null)
                    mMap.clear();
            }
            mClusterManager.clearItems();
            LatLngBounds.Builder builder = LatLngBounds.builder();
            for (int i = 0; i < placesArray.length(); i++) {
                JSONObject place = placesArray.optJSONObject(i);
                LatLng point = new LatLng(place.getDouble(fieldNameLatitude), place
                        .getDouble(fieldNameLongitude));
                if (mBrandId != -1) {
                    builder.include(point);
                }
                if (mMap != null) {
                    mClusterManager.addItem(new BusinessMarker(mActivity, point, place));
                }
                mAdapter.add(place);
            }
            mClusterManager.cluster();
            if (mBrandId != -1) {
                mLatLngBounds = builder.build();
                moveMapCamera();
            }
        } catch (JSONException e) {
        } catch (IllegalStateException ise) {
        } finally {
            mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Helper.handleVolleyError(error, mActivity);
        try {
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            onResponse(CustomRequest.getCachedResponse(businessesRequest
                    .getCacheEntry()));
        } catch (Exception e) {
        } finally {
            mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_business, menu);
        inflater.inflate(R.menu.menu_search, menu);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            SearchManager searchManager = (SearchManager) mActivity.getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                    .getActionView();
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(mActivity.getComponentName()));
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (listView != null && listView.getVisibility() == View.VISIBLE) {
            menu.getItem(0).setIcon(android.R.drawable.ic_menu_mapmode)
                    .setTitle("List");
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_item_view_toggle:
                if (item.getTitle().equals("List")) {
                    item.setTitle("Map");
                    item.setIcon(android.R.drawable.ic_menu_mapmode);
                    listView.setVisibility(View.VISIBLE);
                } else if (item.getTitle().equals("Map")) {
                    item.setTitle("List");
                    item.setIcon(R.drawable.ic_action_view_as_list);
                    listView.setVisibility(View.GONE);
                }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = mapView.getMap();
            MapsInitializer.initialize(mActivity);

            mClusterManager = new ClusterManager<BusinessMarker>(mActivity, mMap);
            if (mMap != null) {
                mMap.setMyLocationEnabled(Boolean.TRUE);
                mMap.setOnCameraChangeListener(mClusterManager);
                mMap.setOnMarkerClickListener(mClusterManager);
                mMap.setOnInfoWindowClickListener(mClusterManager);
                mClusterManager.setOnClusterItemInfoWindowClickListener(this);
                mClusterManager.setRenderer(new BusinessMarkerRenderer(mActivity, mMap, mClusterManager));
                moveMapCamera();
            }
        }
    }

    private void moveMapCamera() {
        CameraUpdate cameraUpdate = null;
        if (mBrandId == -1)
            cameraUpdate = CameraUpdateFactory.newLatLng(mLatLng);
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
        if (isLocationEnabled()) {
            this.setHasOptionsMenu(isLocationEnabled);
            if (enableLocationButton != null && enableLocationButton.getVisibility() == View.VISIBLE) {
                enableLocationButton.setVisibility(View.GONE);
            }
        }
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        locationManager.removeUpdates(this);
        businessesRequest.cancel();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View arg1,
                            int position, long arg3) {
        JSONObject node = mAdapter.getItem(position);
        Helper.openBusiness(mActivity, node);

    }

    @Override
    public void onLocationChanged(Location location) {
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        moveMapCamera();
        sendRequest();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(mActivity, "Status Changed" + provider + status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(mActivity, "Provider Enabled" + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(mActivity, "Provider Disabled" + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClusterItemInfoWindowClick(BusinessMarker businessMarker) {
        JSONObject place = businessMarker.getPlace();
        Helper.openBusiness(mActivity, place);
    }

    @Override
    public void onRefresh() {
        sendRequest();
    }
}

