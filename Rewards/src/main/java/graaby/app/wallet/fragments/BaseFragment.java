package graaby.app.wallet.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.events.ToolbarEvents;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    protected final CompositeSubscription mCompositeSubscriptions;
    protected SwipeRefreshLayout mSwipeRefresh;
    protected Observable.Transformer<Object, Object> mTransformer;
    public BaseFragment() {
        mCompositeSubscriptions = new CompositeSubscription();
    }

    protected void setSwipeRefreshColors(int... colors) {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setColorSchemeResources(colors);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GraabyApplication.inject(this);
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, int layoutResource) {
        super.onCreateView(inflater, container, savedInstanceState);
        View inflatedView = inflater.inflate(layoutResource, null);
        mSwipeRefresh = (SwipeRefreshLayout) inflatedView.findViewById(R.id.swiperefresh);
        if (mSwipeRefresh != null)
            mSwipeRefresh.setOnRefreshListener(this);
        return inflatedView;
    }

    protected abstract void sendRequest();

    @Override
    public void onRefresh() {
        sendRequest();
    }

    @Override
    public void onDetach() {
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(false);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(false);
        mCompositeSubscriptions.unsubscribe();
    }

    protected void setToolbarColors(int toolbarColorResourceID, int statusBarColorResourceID) {
        EventBus.getDefault().postSticky(new ToolbarEvents(toolbarColorResourceID, statusBarColorResourceID));
    }

    protected <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
