package graaby.app.wallet.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import graaby.app.wallet.CustomRequest;
import graaby.app.wallet.Helper;
import graaby.app.wallet.R;
import graaby.app.wallet.adapter.BusinessesAdapter;
import graaby.app.wallet.fragments.MarketFragment;
import graaby.app.wallet.model.GraabySearchSuggestionsProvider;

public class SearchResultsActivity extends ActionBarActivity implements Response.Listener<JSONObject>, Response.ErrorListener, AdapterView.OnItemClickListener {

    private SwipeRefreshLayout mPullToRefreshLayout;
    private String mQueryString;
    private CustomRequest searchRequest;
    private BusinessesAdapter mAdapter;
    private boolean collapseFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQueryString = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle('"' + mQueryString + '"');
            try {
                String type = intent.getStringExtra(Helper.KEY_TYPE);
                if (type.equals(MarketFragment.class.toString()))
                    collapseFlag = true;
                else {
                    collapseFlag = false;
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                collapseFlag = false;
            }
            mPullToRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
            mPullToRefreshLayout.setEnabled(Boolean.FALSE);
            mPullToRefreshLayout.setColorSchemeResources(R.color.midnightblue, R.color.wetasphalt, R.color.asbestos, R.color.concrete);
            ListView mListView = (ListView) findViewById(android.R.id.list);
            mAdapter = new BusinessesAdapter(this);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
            sendRequest();
        }
    }

    private void sendRequest() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(getString(R.string.field_search_query), mQueryString);
        if (collapseFlag) {
            params.put(getString(R.string.field_collapse), true);
        }
        try {
            searchRequest = new CustomRequest("search", params,
                    this, this);
            searchRequest.setShouldCache(Boolean.FALSE);
            Helper.getRQ().add(searchRequest);
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
        mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
        try {
            JSONArray placesArray = response.getJSONArray(getString(R.string.field_business_places));
            if (placesArray.length() != 0) {
                mAdapter.clear();
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        GraabySearchSuggestionsProvider.AUTHORITY, GraabySearchSuggestionsProvider.MODE);
                suggestions.saveRecentQuery(mQueryString, null);
            }
            for (int i = 0; i < placesArray.length(); i++) {
                JSONObject place = placesArray.optJSONObject(i);
                mAdapter.add(place);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mPullToRefreshLayout.setRefreshing(Boolean.FALSE);
        Helper.handleVolleyError(error, this);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent;
        if (collapseFlag) {
            intent = new Intent(this, MarketActivity.class);

        } else {
            intent = new Intent(this, BusinessDetailsctivity.class);
        }
        intent.putExtra(Helper.INTENT_CONTAINER_INFO, mAdapter.getItem(i).toString());
        startActivity(intent);
    }
}
