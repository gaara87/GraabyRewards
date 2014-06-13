package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
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
        Listener<JSONObject> {
    private MapView mMapView;
    private GoogleMap mMap;
    private Bundle mBundle;
    private String fieldNameLongitude;
    private String fieldNameLatitude;
    private ListView listView;

    private Boolean playServiceAvailable = Boolean.TRUE;
    private CustomRequest businessesRequest;
    private SparseArray<JSONObject> markerJSONmapping = new SparseArray<JSONObject>();
    private BusinessesAdapter adapter;
    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        ((MainActivity) mActivity).onSectionAttached(
                getArguments().getInt(Helper.ARG_SECTION_NUMBER));
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

        MapsInitializer.initialize(mActivity);

        listView = (ListView) inflatedView.findViewById(R.id.business_listview);
        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter);

        mMapView = (MapView) inflatedView.findViewById(R.id.map);

        if (playServiceAvailable) {
            mMapView.onCreate(mBundle);
            setUpMapIfNeeded(inflatedView);
        } else {
            mMapView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }

        sendRequest();

        this.setHasOptionsMenu(Boolean.TRUE);

        return inflatedView;
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();

        LatLng ll = ((MainActivity) mActivity).getLocation();
        params.put(fieldNameLatitude, ll.latitude);
        params.put(fieldNameLongitude, ll.longitude);
        try {
            businessesRequest = new CustomRequest("stores", params, this, this);
            Helper.getRQ().add(businessesRequest);
            JSONObject jsonCachedResponse = CustomRequest.getCachedResponse(Helper.getRQ().getCache().get(businessesRequest.getCacheKey()));
            if (jsonCachedResponse != null) {
                onResponse(jsonCachedResponse);
            }
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
        if (playServiceAvailable) {
            inflater.inflate(R.menu.menu_fragment_business, menu);
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
                    mMapView.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                } else if (item.getTitle().equals("Map")) {
                    item.setTitle("List");
                    item.setIcon(R.drawable.ic_action_view_as_list);
                    mMapView.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded(View inflatedView) {
        if (mMap == null) {
            mMap = ((MapView) inflatedView.findViewById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setOnInfoWindowClickListener(this);
        mMap.setMyLocationEnabled(Boolean.TRUE);
        LatLng ll = ((MainActivity) mActivity).getLocation();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(ll);
        try {
            mMap.moveCamera(cameraUpdate);
        } catch (NullPointerException npe) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(13.060422,
                    80.249583)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
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
