package graaby.app.wallet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.adapters.FeedsAdapter;
import graaby.app.wallet.models.retrofit.FeedsResponse;
import graaby.app.wallet.network.services.FeedService;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.EndlessRecyclerOnScrollListener;
import graaby.app.wallet.util.Helper;

public class FeedFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = FeedFragment.class.toString();
    @Bind(R.id.recycler)
    RecyclerView mList;
    @Inject
    FeedService mFeedService;
    private FeedsAdapter mAdapter;
    private int mCurrentPage = 0;

    public static FeedFragment newInstance() {
        return new FeedFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GraabyApplication.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_feeds);
        ButterKnife.bind(this, v);
        setSwipeRefreshColors(R.color.alizarin, R.color.pomegranate, R.color.wisteria, R.color.peterriver);
        mList.setLayoutManager(new LinearLayoutManager(mList.getContext()));
        mAdapter = new FeedsAdapter();
        mList.setHasFixedSize(true);
        mList.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mList.addOnScrollListener(new EndlessRecyclerOnScrollListener((LinearLayoutManager) mList.getLayoutManager()) {
            @Override
            public void onLoadMore(int page) {
                Log.d("Load More", "Page : " + page);
                mCurrentPage = page;
                sendRequest();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sendRequest();
    }

    @Override
    protected void sendRequest() {
        mFeedService.getUserFeeds(mCurrentPage, Helper.PAGE_SIZE)
                .compose(this.<FeedsResponse>applySchedulers())
                .subscribe(new CacheSubscriber<FeedsResponse>(getActivity()) {
                    @Override
                    public void onSuccess(FeedsResponse result) {
                        if (mCurrentPage == 0)
                            mAdapter.clear();

                        if (result.feedsList.size() != 0) {
                            mCurrentPage++;
                            mAdapter.addAll(result.feedsList);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public void onRefresh() {
        mCurrentPage = 0;
        sendRequest();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
