package graaby.app.wallet.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import graaby.app.wallet.model.GraabySearchSuggestionsProvider;

public class SearchResultsActivity extends ActionBarActivity implements Response.Listener<JSONObject>, Response.ErrorListener, AdapterView.OnItemClickListener {

    final public static boolean SEARCH_COLLAPSE = true;

    private SwipeRefreshLayout mPullToRefreshLayout;
    private String mQueryString;
    private CustomRequest searchRequest;
    private BusinessesAdapter mAdapter;
    private boolean collapseFlag;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Intent intent = getIntent();
        mQueryString = intent.getStringExtra(SearchManager.QUERY);
        mQueryString = mQueryString == null ? "" : mQueryString;

        collapseFlag = intent.getBooleanExtra(Helper.KEY_TYPE, false);

        mPullToRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mPullToRefreshLayout.setEnabled(Boolean.FALSE);
        mPullToRefreshLayout.setColorSchemeResources(R.color.midnightblue, R.color.wetasphalt, R.color.asbestos, R.color.concrete);
        ListView mListView = (ListView) findViewById(android.R.id.list);
        mAdapter = new BusinessesAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQueryString = intent.getStringExtra(SearchManager.QUERY);
            if (mSearchView != null) {
                mSearchView.clearFocus();
                mSearchView.setQuery(mQueryString, false);
            }
            sendRequest();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView view = (SearchView) MenuItemCompat.getActionView(searchItem);
            mSearchView = view;
            if (view != null) {
                view.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                view.setIconified(false);
                view.setQueryRefinementEnabled(true);
                view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        view.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        mQueryString = s;
                        return true;
                    }
                });
                view.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        finish();
                        return false;
                    }
                });
                if (!TextUtils.isEmpty(mQueryString)) {
                    view.setQuery(mQueryString, false);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, 0);
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
            intent = new Intent(this, BusinessDetailsActivity.class);
        }
        intent.putExtra(Helper.INTENT_CONTAINER_INFO, mAdapter.getItem(i).toString());
        startActivity(intent);
    }
}
