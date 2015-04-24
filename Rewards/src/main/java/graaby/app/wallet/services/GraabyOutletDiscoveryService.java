package graaby.app.wallet.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.database.ORMService;
import graaby.app.wallet.events.LocationEvents;
import graaby.app.wallet.events.ProfileEvents;
import graaby.app.wallet.models.realm.OutletDAO;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.LocationUpdateRequest;
import graaby.app.wallet.network.services.SettingsService;
import graaby.app.wallet.receivers.UpdateLocationBroadcastReceiver;
import graaby.app.wallet.util.CacheSubscriber;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class GraabyOutletDiscoveryService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = GraabyOutletDiscoveryService.class.toString();
    @Inject
    SettingsService mService;
    ORMService realmDBService;
    private GoogleApiClient mGoogleAPIClient;

    public GraabyOutletDiscoveryService() {
    }

    public static void setupLocationService(Context context) {
        Observable.just(context)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Context>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Context context) {
                        context.startService(new Intent(context, GraabyOutletDiscoveryService.class));
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                UpdateLocationBroadcastReceiver.REQUEST_CODE,
                                new Intent(context, UpdateLocationBroadcastReceiver.class),
                                0);
                        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (5 * 1000),
                                GraabyApplication.getContainerHolder().getContainer().getLong(context.getString(R.string.gtm_update_interval)) * 60 * 1000,
                                pendingIntent);

                        Log.d(TAG, "locationService initialized successfully");
                    }
                });
    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(TAG, "Service onCreate()");
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleAPIClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleAPIClient.connect();
            EventBus.getDefault().register(this);
            GraabyApplication.inject(this);

            realmDBService = new ORMService(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void sendLocationUpdateRequest(Location location) {

        mService.updateUserLocation(new LocationUpdateRequest(location))
                .delaySubscription(new Random().nextInt(5), TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new CacheSubscriber<BaseResponse>(this) {
                    @Override
                    public void onFail(Throwable e) {
                        Log.e(TAG, "Location update failed");
                    }

                    @Override
                    public void onSuccess(BaseResponse result) {
                        if (result.responseSuccessCode == 1)
                            Log.d(TAG, "Location updated");
                        else
                            Log.e(TAG, "Location update failed");
                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient, getLocationRequest(), this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleAPIClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Crashlytics.log(Log.ERROR, TAG, "Failed to connect to google api client, connection result error code:- " + connectionResult.getErrorCode());
    }

    public void onEventBackgroundThread(LocationEvents.SendUpdate event) {
        Log.d(TAG, "Received event from broadcast receiver");

        if (mGoogleAPIClient != null && mGoogleAPIClient.isConnected()) {
            Location lastKnown = LocationServices.FusedLocationApi.getLastLocation(mGoogleAPIClient);
            if (lastKnown != null) {
                sendLocationUpdateRequest(lastKnown);
                Log.d(TAG, "Last location:-" + lastKnown.getLatitude() + "," + lastKnown.getLongitude());
            }
        }
    }

    public void onEvent(ProfileEvents.LoggedOutEvent event) {
        stopSelf();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, UpdateLocationBroadcastReceiver.REQUEST_CODE, new Intent(this, UpdateLocationBroadcastReceiver.class), 0);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getAccuracy() < GraabyApplication.getContainerHolder().getContainer().getDouble(getString(R.string.gtm_accuracy)))
            return;
        RealmResults<OutletDAO> outlets = realmDBService.getAllOutlets();
        if (outlets != null) {
            ListIterator<OutletDAO> iterator = outlets.listIterator();
            Location refLoc = new Location("");
            ArrayList<OutletDAO> outletDAOs = new ArrayList<>();
            long maxMetersFromUserThreshold = GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_max_distance_from_user));
            while (iterator.hasNext()) {
                OutletDAO outlet = iterator.next();
                refLoc.setLatitude(outlet.getLat());
                refLoc.setLongitude(outlet.getLon());
                if (location.distanceTo(refLoc) < maxMetersFromUserThreshold) {
                    outletDAOs.add(outlet);
                    Log.d(TAG, outlet.getName() + " found");
                }
            }
        }
    }

    private LocationRequest getLocationRequest() {
        return new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_fastest_interval)))
                .setSmallestDisplacement(GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_smallest_displacement)));
    }

}
