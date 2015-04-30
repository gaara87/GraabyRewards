package graaby.app.wallet.util;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import rx.Subscriber;

/**
 * @param <T> the data entity type that we're interested in
 */
public abstract class CacheSubscriber<T> extends Subscriber<T> {
    private WeakReference<Context> mContext;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public CacheSubscriber(@NotNull final Context context) {
        this.mContext = new WeakReference<>(context);
    }

    public CacheSubscriber(@NotNull final Context context, SwipeRefreshLayout swipeRefreshLayout) {
        this(context, swipeRefreshLayout, false);
    }

    public CacheSubscriber(@NotNull final Context context, SwipeRefreshLayout swipeRefreshLayout, boolean startRefreshingImmediatelyFlag) {
        this.mContext = new WeakReference<>(context);
        mSwipeRefreshLayout = swipeRefreshLayout;
        if (startRefreshingImmediatelyFlag)
            mSwipeRefreshLayout.setRefreshing(true);
    }


    @Override
    final public void onCompleted() {
        Log.d(CacheSubscriber.class.toString(), "Completed");
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    final public void onError(Throwable e) {
        Crashlytics.logException(e);
        if (mContext.get() != null) {
            onFail(e);
        }
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    final public void onNext(T result) {
        if (mContext.get() != null) {
            onSuccess(result);
        }
    }

    /**
     * Override an on error handler.
     *
     * @param e an exception that was thrown
     */
    public void onFail(Throwable e) {

    }

    /**
     * Implement a success handler.
     *
     * @param result the response object
     */
    public abstract void onSuccess(T result);
}
