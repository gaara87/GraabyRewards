/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package graaby.app.wallet.gcm;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import graaby.app.wallet.GraabyBroadcastReceiver;
import graaby.app.wallet.Helper;
import graaby.app.wallet.MainActivity;
import graaby.app.wallet.R;
import graaby.app.wallet.activities.PointReceivedActivity;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID_POINTS = 1;
    private static final int NOTIFICATION_ID_TX = 2;
    private static int NOTIFICATION_ID_NEW_MARKET = 3;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendNotification(extras.getString(getString(R.string.field_gcm_data)));
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(final String msg) {
        try {
            JSONObject object = new JSONObject(msg);
            String notificationTitle, smallContentText, smallContentInfo;
            int notificationImageResource = R.drawable.ic_gcm_point, notificationID;

            SharedPreferences pref = getSharedPreferences("pref_notification", Activity.MODE_PRIVATE);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSound(Uri.parse(pref.getString("notifications_new_message_ringtone", "")))
                            .setAutoCancel(Boolean.TRUE);

            if (pref.getBoolean("notifications_new_message_vibrate", true)) {
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            }

            Intent intent = new Intent(this, MainActivity.class);

            switch (object.getInt(getString(R.string.field_gcm_msg_type))) {
                case 5:
                    String sender = object.getString(getString(R.string.contact_send_from));
                    int amount = object.getInt(getString(R.string.contact_send_amount));
                    notificationTitle = getString(R.string.gcm_message_recieved_points);
                    smallContentText = String.format(getString(R.string.gcm_message_recieved_points_content), sender, amount);
                    smallContentInfo = String.valueOf(amount);
                    notificationImageResource = R.drawable.ic_gcm_point;
                    notificationID = NOTIFICATION_ID_POINTS;

                    intent.setClass(this, PointReceivedActivity.class);
                    intent.putExtra(Helper.INTENT_CONTAINER_INFO, msg);

                    Intent broadcastIntent = new Intent(this, GraabyBroadcastReceiver.class);
                    broadcastIntent.setAction(GraabyBroadcastReceiver.ACTION_THANK);
                    broadcastIntent.putExtra(Helper.INTENT_CONTAINER_INFO, msg);
                    broadcastIntent.putExtra(Helper.NOTIFICATIONID, notificationID);

                    PendingIntent pendingBroadcastIntent = PendingIntent.getBroadcast(this, 0, broadcastIntent, 0);

                    mBuilder.addAction(R.drawable.ic_action_accept, "Say thanks", pendingBroadcastIntent);
                    break;
                case 6:
                    amount = object.getInt(getString(R.string.contact_send_amount));
                    String outlet = object.getString(getString(R.string.field_business_name));
                    notificationTitle = getString(R.string.gcm_message_transaction);
                    smallContentText = String.format(getString(R.string.gcm_message_transaction_content), amount, outlet);
                    smallContentInfo = String.valueOf(amount);
                    notificationImageResource = R.drawable.ic_gcm_point;
                    notificationID = NOTIFICATION_ID_TX;
                    break;
                case 7:
                    outlet = object.getString(getString(R.string.field_business_name));
                    notificationTitle = getString(R.string.gcm_message_market);
                    smallContentText = String.format(getString(R.string.gcm_message_market_content), outlet);
                    smallContentInfo = "";
                    notificationImageResource = R.drawable.ic_gcm_discount;
                    notificationID = NOTIFICATION_ID_NEW_MARKET;
                    break;
//                case 8:
//                    //contact added
//                    break;
                default:
                    notificationTitle = "";
                    smallContentText = "";
                    smallContentInfo = "";
                    notificationID = 0;
            }

            NotificationManager mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);


            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), notificationImageResource))
                    .setSmallIcon(notificationImageResource)
                    .setContentTitle(notificationTitle)
                    .setContentText(smallContentText)
                    .setContentInfo(smallContentInfo)
                    .setContentIntent(contentIntent);

            mNotificationManager.notify(notificationID, mBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
