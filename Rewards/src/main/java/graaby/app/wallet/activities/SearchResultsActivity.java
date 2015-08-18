package graaby.app.wallet.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import javax.inject.Inject;

import graaby.app.wallet.R;
import graaby.app.wallet.adapters.SearchResultsAdapter;
import graaby.app.wallet.models.android.GraabySearchSuggestionsProvider;
import graaby.app.wallet.models.retrofit.SearchRequest;
import graaby.app.wallet.models.retrofit.SearchResponse;
import graaby.app.wallet.network.services.SearchService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.Helper;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchResultsActivity extends BaseAppCompatActivity
        implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {

    final public static boolean SEARCH_COLLAPSE = true;
    @Inject
    SearchService mSearchService;
    private String mQueryString;
    private SearchResultsAdapter mAdapter;
    private boolean collapseFlag;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        mQueryString = intent.getStringExtra(SearchManager.QUERY);
        mQueryString = mQueryString == null ? "" : mQueryString;

        collapseFlag = intent.getBooleanExtra(Helper.KEY_TYPE, false);

        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setEnabled(Boolean.FALSE);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.midnightblue, R.color.wetasphalt, R.color.asbestos, R.color.concrete);
        ListView mListView = (ListView) findViewById(android.R.id.list);
        mAdapter = new SearchResultsAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setEmptyView(findViewById(android.R.id.empty));

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
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        mSearchView.setLayoutParams(new ActionMenuView.LayoutParams(ActionMenuView.LayoutParams.MATCH_PARENT, ActionMenuView.LayoutParams.WRAP_CONTENT));
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setFocusable(true);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.requestFocusFromTouch();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            mSearchView.setIconified(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, 0);
        }
    }

    private void sendRequest() {
        mCompositeSubscriptions.add(mSearchService.searchBusinesses(new SearchRequest(mQueryString, collapseFlag))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CacheSubscriber<SearchResponse>(this) {
                    @Override
                    public void onSuccess(SearchResponse result) {
                        mAdapter.clear();
                        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchResultsActivity.this,
                                GraabySearchSuggestionsProvider.AUTHORITY, GraabySearchSuggestionsProvider.MODE);
                        suggestions.saveRecentQuery(mQueryString, null);
                        mAdapter.addAll(result.outlets);
                    }
                }));

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent;
        if (collapseFlag) {
            intent = new Intent(this, MarketActivity.class);
        } else {
            intent = new Intent(this, BusinessDetailsActivity.class);
        }
        try {
            intent.putExtra(Helper.INTENT_CONTAINER_INFO, LoganSquare.serialize(mAdapter.getItem(i)));
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mQueryString = s;
        sendRequest();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }
}
