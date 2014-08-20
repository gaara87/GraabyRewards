package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.R;

/**
 * Created by gaara on 8/4/14.
 */
public class BusinessDetailFragment extends Fragment implements
        Response.ErrorListener, Response.Listener<JSONObject>, SwipeRefreshLayout.OnRefreshListener {

    private int businessId;
    private JSONObject placeNode;
    private SwipeRefreshLayout mPullToRefreshLayout;
    private Activity mActivity;
    private BusinessDetailFragmentCallback mCallback;

    public static BusinessDetailFragment newInstance(String jsonData) {
        BusinessDetailFragment fragment = new BusinessDetailFragment();
        Bundle args = new Bundle();
        args.putString(Helper.INTENT_CONTAINER_INFO, jsonData);
        fragment.setArguments(args);
        return fragment;
    }

    public BusinessDetailFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mCallback = (BusinessDetailFragmentCallback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        if (getArguments() != null) {
            String info = getArguments().getString(
                    Helper.INTENT_CONTAINER_INFO);

            try {
                placeNode = new JSONObject(info);
            } catch (JSONException e) {
                placeNode = new JSONObject();
            }


            try {
                businessId = placeNode.getInt(getString(R.string.field_business_outlet_id));
                ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(placeNode.getString(getString(
                        R.string.field_business_title)));
            } catch (JSONException e) {
                businessId = 0;
            } catch (NullPointerException npe) {

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_business_detail, null);
        mPullToRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swiperefresh);
        mPullToRefreshLayout.setOnRefreshListener(this);
        mPullToRefreshLayout.setColorSchemeResources(R.color.midnightblue, R.color.wetasphalt, R.color.asbestos, R.color.concrete);
        sendRequest();
        return v;
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();

        try {
            params.put(getString(R.string.field_business_outlet_id), businessId);
            Helper.getRQ().add(new CustomRequest("store", params, this, this));
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
        } catch (Resources.NotFoundException e1) {
        } catch (JSONException e1) {
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (placeNode.has(getString(R.string.field_business_latitude)) && placeNode.has(getString(R.string.field_business_longitude)))
            mActivity.getMenuInflater().inflate(R.menu.menu_business_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_item_directions:

                String geoUri = null;
                try {
                    geoUri = "http://maps.google.com/maps?f=d&daddr=" + placeNode.getDouble(getString(R.string.field_business_latitude)) + "," + placeNode.getDouble(getString(R.string.field_business_longitude));
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(geoUri));
                    intent.setComponent(new ComponentName("com.google.android.apps.maps",
                            "com.google.android.maps.MapsActivity"));
                    startActivity(intent);
                } catch (JSONException jse) {
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponse(JSONObject response) {
        mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
        ImageView iv = (ImageView) mActivity.findViewById(R.id.item_businessPicImageView);
        try {
            Helper.getImageLoader().get(response.getString(mActivity.getString(
                    R.string.pic_url)), ImageLoader.getImageListener(iv, R.drawable.default_business_profile_image, R.drawable.default_business_profile_image));
        } catch (Resources.NotFoundException e) {
            iv.setImageResource(R.drawable.default_business_profile_image);
        } catch (JSONException e) {
            iv.setImageResource(R.drawable.default_business_profile_image);
        }

        {
            try {
                JSONObject statsObject = response.getJSONObject(mActivity.getString(R.string.field_business_stats));
                TextView tv = (TextView) mActivity.findViewById(R.id.business_points);
                tv.setText(statsObject.getString(mActivity.getString(R.string.field_business_given)));

                tv = (TextView) mActivity.findViewById(R.id.business_followers);
                tv.setText(statsObject.getString(mActivity.getString(R.string.profile_following)));

                tv = (TextView) mActivity.findViewById(R.id.business_checkins);
                tv.setText(statsObject.getString(mActivity.getString(R.string.profile_checkins)));

                tv = (TextView) mActivity.findViewById(R.id.points_earned_textView);
                tv.setText(statsObject.getString(mActivity.getString(R.string.profile_total_points)));

                tv = (TextView) mActivity.findViewById(R.id.profile_total_savings_textView);
                tv.setText(statsObject.getString(mActivity.getString(R.string.profile_total_savings)));

                tv = (TextView) mActivity.findViewById(R.id.profile_checkins_textview);
                tv.setText(statsObject.getString(mActivity.getString(R.string.profile_my_checkins)));
            } catch (JSONException e) {
            }
        }

        try {
            ((ActionBarActivity) mActivity).getSupportActionBar().setTitle(response.getString(getResources().getString(
                    R.string.field_business_name)));

        } catch (Resources.NotFoundException e) {
        } catch (JSONException e) {
        }

        TextView tv = (TextView) mActivity.findViewById(R.id.item_businessAddressTextView);
        try {
            tv.setText(response.getString(getResources().getString(
                    R.string.field_)));
        } catch (Resources.NotFoundException e) {
        } catch (JSONException e) {
        }

        tv = (TextView) mActivity.findViewById(R.id.item_businessPhoneTextView);
        try {
            tv.setText(response.getString(getResources().getString(
                    R.string.business_phone)));
        } catch (Resources.NotFoundException e) {
        } catch (JSONException e) {
        }

        tv = (TextView) mActivity.findViewById(R.id.item_businessSiteTextView);
        try {
            tv.setText(response.getString(getResources().getString(
                    R.string.business_site)));
        } catch (Resources.NotFoundException e) {
        } catch (JSONException e) {
        }

        try {
            JSONObject punchcards = response.getJSONObject(mActivity.getString(R.string.field_business_punchcard));
            Integer discountObject = 0;
            String field = mActivity.getString(R.string.field_business_discount_value);
            if (response.has(field))
                discountObject = response.getInt(field);
            mCallback.onRewardDetailsLoaded(discountObject, punchcards.getJSONArray(mActivity.getString(R.string.field_punch_rewards)));
        } catch (JSONException e) {
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Helper.handleVolleyError(error, mActivity);
        mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
    }

    @Override
    public void onRefresh() {
        sendRequest();
    }

    public static interface BusinessDetailFragmentCallback {
        /**
         * Called when fragment has loaded all the punchcards.
         */
        void onRewardDetailsLoaded(Integer discount, JSONArray punches);
    }

    public static class RewardDetailsFragment extends Fragment {

        private ListView mListView;
        private TextView mDiscountTextView;

        public static RewardDetailsFragment newInstance() {
            RewardDetailsFragment fragment = new RewardDetailsFragment();
            return fragment;
        }

        public RewardDetailsFragment() {

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

        public void setPunchCards(Context context, Integer discount, List<JSONObject> punches) {
            mDiscountTextView.setText(String.valueOf(discount) + "%");
            mListView.setAdapter(new PunchAdapter(context, punches));
        }

        private class PunchAdapter extends ArrayAdapter<JSONObject> {

            private LayoutInflater inflater;
            private String rewardField;
            private String rewardVisitCountField;

            public PunchAdapter(Context context, List<JSONObject> punches) {
                super(context, R.layout.fragment_punchcards, android.R.layout.simple_list_item_2, punches);
                inflater = LayoutInflater.from(getContext());
                rewardField = context.getResources().getString(
                        R.string.field_punch_reward);
                rewardVisitCountField = context.getResources().getString(
                        R.string.field_punch_reward_visit);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);
                JSONObject node = getItem(position);

                TextView tv;
                {
                    tv = (TextView) convertView.findViewById(android.R.id.text1);
                    try {
                        tv.setText(node.getString(rewardField));
                    } catch (JSONException e) {
                    }
                }

                {
                    tv = (TextView) convertView.findViewById(android.R.id.text2);
                    try {
                        tv.setText(String.format(getString(R.string.punchcard_visit_value), node.getString(rewardVisitCountField)));
                    } catch (JSONException e) {
                    }
                }

                return convertView;
            }
        }
    }
}
