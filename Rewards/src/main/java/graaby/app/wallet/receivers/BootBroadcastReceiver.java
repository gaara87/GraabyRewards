package graaby.app.wallet.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import graaby.app.wallet.auth.UserAuthenticationHandler;
import graaby.app.wallet.services.GraabyOutletDiscoveryService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Inject
    UserAuthenticationHandler authHandler;

    public BootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            if (authHandler != null && authHandler.isAuthenticated()) {
                GraabyOutletDiscoveryService.setupLocationService(context);
            }
        }

    }
}
