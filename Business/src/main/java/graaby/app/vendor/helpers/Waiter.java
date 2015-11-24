package graaby.app.vendor.helpers;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * Created by gaara on 9/12/14.
 */
public class Waiter extends Thread {
    private static final String TAG = Waiter.class.getName();
    Handler mHandler;
    private long lastUsed;
    private long period;
    private boolean stop;

    public Waiter(long period, Handler handler) {
        this.period = period;
        stop = false;
        mHandler = handler;
    }

    public void run() {
        long idle;
        this.touch();
        do {
            idle = System.currentTimeMillis() - lastUsed;
            try {
                Thread.sleep(5000); //check every 5 seconds
            } catch (InterruptedException e) {
                Log.d(TAG, "Waiter interrupted!");
            }
            if (idle > period) {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                mHandler.sendMessage(msg);
                forceInterrupt();
            }
        }
        while (!stop);
        Log.d(TAG, "Finishing Waiter thread");
    }

    public synchronized void touch() {
        lastUsed = System.currentTimeMillis();
    }

    public synchronized void forceInterrupt() {
        stop = true;
        this.interrupt();
    }
}