package graaby.app.wallet.fragments;

import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class FeedFragment extends ListFragment implements Listener<JSONObject>,
        ErrorListener {

    private List<JSONObject> feedList = new ArrayList<JSONObject>();
    private FeedsAdapter mAdapter;
    private CustomRequest feedRequest;
    private Activity mActivity;
    private PullToRefreshLayout mPullToRefreshLayout;

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
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // This is the View which is created by ListFragment
        ViewGroup viewGroup = (ViewGroup) view;

        // We need to create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                .insertLayoutInto(viewGroup)
                .theseChildrenArePullable(getListView(), getListView().getEmptyView())
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        sendRequest();
                    }
                })
                .options(Options.create()
                        .scrollDistance(.5f).build())
                .setup(mPullToRefreshLayout);

        sendRequest();
        mAdapter = new FeedsAdapter(mActivity, feedList);
        setListAdapter(mAdapter);
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("last_updated_tstamp", 123456778);
        try {
            feedRequest = new CustomRequest("feeds", params, this, this);
            Helper.getRQ().add(feedRequest);
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
                mPullToRefreshLayout.setRefreshComplete();
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
            mPullToRefreshLayout.setRefreshComplete();

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
