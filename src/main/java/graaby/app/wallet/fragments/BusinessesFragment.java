package graaby.app.wallet.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;

public class BusinessesFragment extends Fragment implements
        OnInfoWindowClickListener, OnItemClickListener, ErrorListener,
        Listener<JSONObject>, LocationListener {
    private GoogleMap mMap;
    private Bundle mBundle;
    private String fieldNameLongitude;
    private String fieldNameLatitude;
    private ListView listView;

    private CustomRequest businessesRequest;
    private SparseArray<JSONObject> markerJSONmapping = new SparseArray<JSONObject>();
    private BusinessesAdapter adapter;
    private Activity mActivity;

    private LatLng latLng = new LatLng(0, 0);
    private Boolean isLocationEnabled = Boolean.FALSE;
    private LocationManager locationManager;
    private Button enableLocationButton;

    private boolean isLocationEnabled() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isLocationEnabled = Boolean.TRUE;
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 20, this);
            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnown != null)
                latLng = new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude());
        } else {
            isLocationEnabled = Boolean.FALSE;
        }
        return isLocationEnabled;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        ((MainActivity) mActivity).onSectionAttached(
                getArguments().getInt(Helper.ARG_SECTION_NUMBER));
        locationManager = (LocationManager) mActivity
                .getSystemService(Context.LOCATION_SERVICE);
        isLocationEnabled();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        fieldNameLongitude = getString(
                R.string.business_longitude);
        fieldNameLatitude = getString(
                R.string.business_latitude);

        adapter = new BusinessesAdapter(mActivity, new ArrayList<JSONObject>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_business, container,
                false);

        listView = (ListView) inflatedView.findViewById(R.id.business_listview);
        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter);
        if (!isLocationEnabled) {
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
        this.setHasOptionsMenu(isLocationEnabled);
        setUpMapIfNeeded();

        return inflatedView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sendRequest();
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put(fieldNameLatitude, latLng.latitude);
        params.put(fieldNameLongitude, latLng.longitude);
        try {
            businessesRequest = new CustomRequest("stores", params, this, this);
            Cache.Entry entry = Helper.getRQ().getCache().get(businessesRequest.getCacheKey());
            if (entry != null) {
                JSONObject jsonCachedResponse = CustomRequest.getCachedResponse(entry);
                if (jsonCachedResponse != null) {
                    onResponse(jsonCachedResponse);
                }
            }
            Helper.getRQ().add(businessesRequest);
            showProgress(Boolean.TRUE);
        } catch (JSONException e) {

        } catch (NullPointerException npe) {
        } finally {
            showProgress(Boolean.TRUE);
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        String store_title = getString(
                R.string.business_title);
        try {
            JSONArray placesArray = response.getJSONArray(getString(R.string.business_places));
            if (placesArray.length() != 0) {
                adapter.clear();
                if (mMap != null)
                    mMap.clear();
            }
            for (int i = 0; i < placesArray.length(); i++) {
                JSONObject place = placesArray.optJSONObject(i);
                Double lat = place.getDouble(fieldNameLatitude), lon = place
                        .getDouble(fieldNameLongitude);
                Integer tempHashcode = 0;
                if (mMap != null) {
                    Marker tempMarker = mMap
                            .addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .title(place.getString(store_title))
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.business_map_pointer)));

                    tempHashcode = tempMarker.hashCode();
                }
                adapter.add(place);
                markerJSONmapping.put(tempHashcode, place);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            showProgress(Boolean.FALSE);
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Helper.handleVolleyError(error, mActivity);
        try {
            showProgress(Boolean.TRUE);
            onResponse(CustomRequest.getCachedResponse(businessesRequest
                    .getCacheEntry()));
        } catch (Exception e) {
        } finally {
            showProgress(Boolean.FALSE);

        }
    }

    private void showProgress(Boolean done) {
        try {
            ((MainActivity) mActivity).showProgress(done);
        } catch (NullPointerException e) {
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
            case R.id.action_menu_item_refresh:
                sendRequest();
                break;
            case R.id.action_menu_item_view_toggle:
                if (item.getTitle().equals("List")) {
                    item.setTitle("Map");
                    item.setIcon(android.R.drawable.ic_menu_mapmode);
//                    mMap.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                } else if (item.getTitle().equals("Map")) {
                    item.setTitle("List");
                    item.setIcon(R.drawable.ic_action_view_as_list);
//                    mMapView.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) ((ActionBarActivity) mActivity).getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setOnInfoWindowClickListener(this);
        mMap.setMyLocationEnabled(Boolean.TRUE);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        try {
            mMap.moveCamera(cameraUpdate);
        } catch (NullPointerException npe) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0,
                    0)));
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationManager.removeUpdates(this);
        if (mMap != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).commit();
            mMap = null;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        JSONObject place = markerJSONmapping.get(marker.hashCode());
        Helper.openBusiness(mActivity, place);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View arg1,
                            int position, long arg3) {
        JSONObject node = adapter.getItem(position);
        Helper.openBusiness(mActivity, node);

    }

    @Override
    public void onLocationChanged(Location location) {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        setUpMap();
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

    class BusinessesAdapter extends ArrayAdapter<JSONObject> {

        private final LayoutInflater inflater;
        private String bName_field, bArea_field;

        public BusinessesAdapter(Context context, List<JSONObject> places) {
            super(context, R.layout.item_list_business, places);
            bName_field = context.getString(R.string.business_title);
            bArea_field = context.getString(R.string.business_area);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_list_business, null);
            JSONObject place = getItem(position);

            try {
                ((TextView) convertView.findViewById(R.id.item_businessNameTextView)).setText(place.getString(bName_field));
            } catch (JSONException e) {
            }

            try {
                ((TextView) convertView.findViewById(R.id.item_businessAddressTextView)).setText(place.getString(bArea_field));
            } catch (JSONException e) {
            }


            return convertView;
        }

    }

}
