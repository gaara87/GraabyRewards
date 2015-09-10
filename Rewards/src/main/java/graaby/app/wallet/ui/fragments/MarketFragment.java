package graaby.app.wallet.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.DiscountItemDetailsResponse;
import graaby.app.wallet.models.retrofit.MarketForBusinessRequest;
import graaby.app.wallet.models.retrofit.MarketForOutletRequest;
import graaby.app.wallet.models.retrofit.MarketRequest;
import graaby.app.wallet.models.retrofit.MarketResponse;
import graaby.app.wallet.network.services.MarketService;
import graaby.app.wallet.ui.activities.DiscountItemDetailsActivity;
import graaby.app.wallet.ui.activities.SearchResultsActivity;
import graaby.app.wallet.ui.adapters.MarketAdapter;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.DiscountItemType;
import graaby.app.wallet.util.EndlessRecyclerOnScrollListener;
import graaby.app.wallet.util.Helper;
import graaby.app.wallet.util.SimpleDividerItemDecoration;
import rx.Observable;
import rx.Subscription;

public class MarketFragment extends BaseFragment implements MarketAdapter.MarketItemClickListener {

    public final static String SEARCHABLE_PARAMETER = "searchable";
    public static final String TAG = MarketFragment.class.toString();
    @Bind(R.id.recycler)
    RecyclerView mGridRecyclerView;
    @Bind(android.R.id.empty)
    TextView mEmpty;
    @Bind(R.id.progressBar)
    ProgressBar mProgress;

    @Inject
    MarketService mMarketService;
    private DiscountItemType whatType;
    private MarketAdapter adapter;
    private boolean areTheseMyDiscountItems = false;
    private Integer mBrandID = Helper.DEFAULT_NON_BRAND_RELATED;
    private int mCurrentPage;
    private SimpleDividerItemDecoration decor;

    private EndlessRecyclerOnScrollListener onScrollListener = new EndlessRecyclerOnScrollListener() {
        @Override
        public void onLoadMore(int current_page) {
            Log.d("Load More", "Page : " + current_page);
            mCurrentPage = current_page;
            sendRequest();
        }
    };

    public static MarketFragment newInstance(Boolean myFlag, int outletID, Boolean searchable) {
        MarketFragment fragment = new MarketFragment();
        Bundle args = new Bundle();
        args.putInt(Helper.BRAND_ID_BUNDLE_KEY, outletID);
        args.putInt(Helper.OUTLET_ID_BUNDLE_KEY, outletID);
        args.putBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG, myFlag);
        args.putBoolean(SEARCHABLE_PARAMETER, searchable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().get(
                    Helper.KEY_TYPE) != null)
                whatType = (DiscountItemType) getArguments().get(Helper.KEY_TYPE);
            areTheseMyDiscountItems = getArguments().getBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG, Boolean.FALSE);
            mBrandID = getArguments().getInt(Helper.BRAND_ID_BUNDLE_KEY, Helper.DEFAULT_NON_BRAND_RELATED);
        }
        adapter = new MarketAdapter(getActivity(), areTheseMyDiscountItems, this);
        if (getArguments() != null && getArguments().getBoolean(SEARCHABLE_PARAMETER, Boolean.FALSE)) {
            this.setHasOptionsMenu(Boolean.TRUE);
        }
        sendRequest();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_marketplace);
        ButterKnife.bind(this, v);
        setSwipeRefreshColors(R.color.sunflower, R.color.nephritis, R.color.peterriver, R.color.pumpkin);
        mSwipeRefresh.setEnabled(false);
        decor = new SimpleDividerItemDecoration(mGridRecyclerView.getContext());
        mGridRecyclerView.setLayoutManager(new LinearLayoutManager(mGridRecyclerView.getContext()));
        mGridRecyclerView.setHasFixedSize(true);
        mGridRecyclerView.addItemDecoration(decor);
        mGridRecyclerView.setAdapter(adapter);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        onScrollListener.setLinearLayoutManager((LinearLayoutManager) mGridRecyclerView.getLayoutManager());
        mGridRecyclerView.addOnScrollListener(onScrollListener);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_market, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_toggle_grid);
        if (adapter != null && item != null) {
            int resourceDrawableID = adapter.isItAList() ? R.drawable.ic_grid_on_white_24dp : R.drawable.ic_view_list_white_24dp;
            item.setIcon(resourceDrawableID);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
                intent.putExtra(Helper.KEY_TYPE, SearchResultsActivity.SEARCH_COLLAPSE);
                startActivity(intent);
                return true;
            case R.id.action_toggle_grid:
                if (adapter.isItAList()) {
                    adapter.setAsGridAdapter();
                    mGridRecyclerView.setLayoutManager(new GridLayoutManager(mGridRecyclerView.getContext(), getResources().getInteger(R.integer.grid_columns)));
                    mGridRecyclerView.removeItemDecoration(decor);
                    onScrollListener.setGridLayoutManager((GridLayoutManager) mGridRecyclerView.getLayoutManager());

                } else {
                    adapter.setAsListAdapter();
                    mGridRecyclerView.setLayoutManager(new LinearLayoutManager(mGridRecyclerView.getContext()));
                    mGridRecyclerView.addItemDecoration(decor);
                    onScrollListener.setLinearLayoutManager((LinearLayoutManager) mGridRecyclerView.getLayoutManager());
                }
                getActivity().supportInvalidateOptionsMenu();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    protected void sendRequest() {
        MarketRequest request = new MarketRequest();
        if (mBrandID != Helper.DEFAULT_NON_BRAND_RELATED) {
            if (getArguments().getInt(Helper.OUTLET_ID_BUNDLE_KEY, -1) != -1) {
                request = new MarketForOutletRequest(getArguments().getInt(Helper.OUTLET_ID_BUNDLE_KEY));
            } else {
                request = new MarketForBusinessRequest(mBrandID);
            }
        }
        request.page = mCurrentPage;
        //TODO: convert PAGE_SIZE to configuration parameter from GTM
        request.size = Helper.PAGE_SIZE;

        Observable<MarketResponse> tempObs = mMarketService.getMarketDiscountItems(request);
        if (areTheseMyDiscountItems) {
            // viewing user coupons or vouchers
            switch (whatType) {
                case COUPONS:
                    tempObs = mMarketService.getUserCoupons(request);
                    break;
                case VOUCHERS:
                    tempObs = mMarketService.getUserVouchers(request);
                    break;
            }
        }
        Subscription subscriber = tempObs.compose(this.<MarketResponse>applySchedulers())
                .subscribe(new CacheSubscriber<MarketResponse>(getActivity(), mSwipeRefresh, true) {
                    @Override
                    public void onSuccess(MarketResponse result) {
                        if (result.items.size() > 0) {
                            if (mCurrentPage++ == 0) {
                                adapter.clear();
                            }
                            adapter.addAll(result.items);
                        } else if (mCurrentPage == 0) {
                            mEmpty.setVisibility(View.VISIBLE);
                            mGridRecyclerView.setVisibility(View.GONE);
                            mProgress.setVisibility(View.GONE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        mCompositeSubscriptions.add(subscriber);
        Log.d("Loading", "Page - " + mCurrentPage);
    }

    @Override
    void setupInjections() {
        GraabyApplication.getApplication().getApiComponent().inject(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 66 && resultCode == Activity.RESULT_OK) {
            sendRequest();
        }
    }

    @Override
    public void onMarketItemClick(int position) {
        DiscountItemDetailsResponse marketDiscountItem = adapter.getItem(position);
        Intent intent = new Intent();
        try {
            intent.putExtra(Helper.INTENT_CONTAINER_INFO, LoganSquare.serialize(marketDiscountItem));

            if (whatType != null)
                intent.putExtra(Helper.KEY_TYPE, whatType);
            intent.putExtra(Helper.MY_DISCOUNT_ITEMS_FLAG, areTheseMyDiscountItems);
            intent.setClass(getActivity(), DiscountItemDetailsActivity.class);
            startActivityForResult(intent, 66);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
