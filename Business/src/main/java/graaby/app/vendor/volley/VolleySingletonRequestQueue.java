package graaby.app.vendor.volley;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by gaara on 9/9/14.
 */
public class VolleySingletonRequestQueue {
    private static VolleySingletonRequestQueue mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private RequestQueue mJobRequestQueue;

    private VolleySingletonRequestQueue(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new DiskLruImageCache(context, "imageCache", 1024 * 1024 * 10, Bitmap.CompressFormat.PNG, 100));

    }

    public static synchronized VolleySingletonRequestQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingletonRequestQueue(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), new OkHttpStack());
        }
        return mRequestQueue;
    }

    public RequestQueue getJobRequestQueue() {
        if (mJobRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mJobRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), new OkHttpStack());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}