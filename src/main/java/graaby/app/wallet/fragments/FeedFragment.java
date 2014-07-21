package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import junit.framework.Assert;

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

public class FeedFragment extends ListFragment implements Listener<JSONObject>,
        ErrorListener, SwipeRefreshLayout.OnRefreshListener {

    private List<JSONObject> feedList = new ArrayList<JSONObject>();
    private FeedsAdapter mAdapter;
    private CustomRequest feedRequest;
    private Activity mActivity;
    private SwipeRefreshLayout mPullToRefreshLayout;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feeds, null);
        mPullToRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swiperefresh);
        mPullToRefreshLayout.setOnRefreshListener(this);
        mPullToRefreshLayout.setColorSchemeResources(R.color.alizarin, R.color.pomegranate, R.color.wisteria, R.color.peterriver);
        mAdapter = new FeedsAdapter(mActivity, feedList);
        setListAdapter(mAdapter);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sendRequest();
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("last_updated_tstamp", 123456778);
        try {
            feedRequest = new CustomRequest("feeds", params, this, this);
            Cache.Entry entry = Helper.getRQ().getCache().get(feedRequest.getCacheKey());
            if (entry != null) {
                JSONObject jsonCachedResponse = CustomRequest.getCachedResponse(entry);
                if (jsonCachedResponse != null) {
                    onResponse(jsonCachedResponse);
                }
            }
            Helper.getRQ().add(feedRequest);
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            } catch (NullPointerException npe) {

            }
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        Assert.assertNotNull(response);

        try {
            feedList.clear();
            JSONArray feeds = response.getJSONArray(mActivity
                    .getResources().getString(R.string.feed_feed));

            for (int i = 0; i < feeds.length(); i++) {
                feedList.add(feeds.optJSONObject(i));
            }
            mAdapter.notifyDataSetChanged();
        } catch (NotFoundException e) {
        } catch (JSONException e) {
        } finally {
            try {
                mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
            } catch (NullPointerException npe) {
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Helper.handleVolleyError(error, mActivity);
        feedList.clear();

        try {
            mPullToRefreshLayout.setRefreshing(Boolean.TRUE);
            onResponse(CustomRequest.getCachedResponse(feedRequest
                    .getCacheEntry()));
        } catch (Exception e) {
        } finally {
            mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.refresh_menu_item:
                sendRequest();
                break;*/

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        sendRequest();
    }

    private class FeedsAdapter extends ArrayAdapter<JSONObject> {

        private LayoutInflater inflater;
        private String feedContentViewField;
        private String feedIdentityNameField;

        public FeedsAdapter(Activity activity, List<JSONObject> feeds) {
            super(activity, R.layout.fragment_feeds, R.layout.item_list_feed, feeds);
            inflater = LayoutInflater.from(getContext());
            feedContentViewField = getContext().getResources().getString(
                    R.string.feed_said);
            feedIdentityNameField = getContext().getResources().getString(
                    R.string.feed_id_name);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_list_feed, null);
            JSONObject node = getItem(position);

            TextView tv;
            {
                tv = (TextView) convertView.findViewById(R.id.feed_contentTextView);
                try {
                    tv.setText(node.getString(feedContentViewField));
                } catch (JSONException e) {
                }
            }

            {
                tv = (TextView) convertView
                        .findViewById(R.id.feed_identityNameTextView);
                try {
                    tv.setText(node.getString(feedIdentityNameField));
                } catch (JSONException e) {
                }
            }

            return convertView;
        }
    }
}
