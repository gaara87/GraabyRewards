package graaby.app.vendor.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

final public class GraabyBusinessUserAuthenticationService extends Service {

    private GraabyBusinessUserAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new GraabyBusinessUserAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
