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

package graaby.app.wallet.services;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import graaby.app.wallet.R;
import graaby.app.wallet.activities.ExtraInfoActivity;
import graaby.app.wallet.activities.FeedActivity;
import graaby.app.wallet.activities.MarketActivity;
import graaby.app.wallet.activities.PointReceivedActivity;
import graaby.app.wallet.receivers.GcmBroadcastReceiver;
import graaby.app.wallet.receivers.GraabyBroadcastReceiver;
import graaby.app.wallet.util.Helper;
import graaby.app.wallet.util.NotificationType;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final String NOTIFICATION_ACTION_POINTS = "action_points_transfer";
    public static final String NOTIFICATION_ACTION_TX = "action_points_reward";
    public static final String NOTIFICATION_ACTION_INFO = "action_info";
    public static final String NOTIFICATION_ACTION_FEED = "action_feed";
    public static final String NOTIFICATION_ACTION_NEW_DISCOUNT = "action_new_discount";

    public static int NOTIFICATION_ID_NEW_MARKET = 3;
    public static int NOTIFICATION_ID_THANKED = 4;
    public static int NOTIFICATION_ID_CHECKIN = 5;
    public static int NOTIFICATION_ID_FEED = 6;
    public static int NOTIFICATION_ID_INFO = 7;

    private static Random random = new Random();

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
            PendingIntent pendingIntent = null;
            String notificationTitle, smallContentText, smallContentInfo = "";
            int notificationImageResource = R.drawable.ic_noty_point, notificationID,
                    uniquePendingId = (int) (System.currentTimeMillis() & 0xfffffff);

            SharedPreferences pref = getSharedPreferences("pref_notification", Activity.MODE_PRIVATE);

            Uri noty_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if (!TextUtils.isEmpty(pref.getString("notifications_new_message_ringtone", ""))) {
                noty_sound = Uri.parse(pref.getString("notifications_new_message_ringtone", ""));
            }

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSound(noty_sound)
                            .setLights(0xff2ECC71, 300, 1000)
                            .setAutoCancel(Boolean.TRUE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mBuilder.setCategory(Notification.CATEGORY_SOCIAL);

            if (pref.getBoolean("notifications_new_message_vibrate", true)) {
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            }

            Intent activityIntent = new Intent();
            switch (NotificationType.getType(object.getInt(getString(R.string.field_gcm_msg_type)))) {
                case SHARE_POINTS:
                    //user received points from contact
                    String sender = object.getString(getString(R.string.field_gcm_name));
                    int amount = object.getInt(getString(R.string.contact_send_amount));
                    notificationTitle = getString(R.string.gcm_message_recieved_points);
                    smallContentText = String.format(getString(R.string.gcm_message_recieved_points_content),
                            sender, amount);
                    smallContentInfo = String.valueOf(amount);
                    notificationImageResource = R.drawable.ic_noty_point;
                    notificationID = getRandomInt(0, 50);

                    mBuilder.setColor(getResources().getColor(R.color.alizarin));

                    activityIntent.setClass(this, PointReceivedActivity.class);
                    activityIntent.setAction(NOTIFICATION_ACTION_POINTS);
                    activityIntent.putExtra(Helper.INTENT_CONTAINER_INFO, msg);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(PointReceivedActivity.class);
                    stackBuilder.addNextIntent(activityIntent);

                    Intent broadcastIntent = new Intent(this, GraabyBroadcastReceiver.class);
                    broadcastIntent.setAction(GraabyBroadcastReceiver.ACTION_THANK);
                    broadcastIntent.putExtra(Helper.INTENT_CONTAINER_INFO, msg);
                    broadcastIntent.putExtra(Helper.NOTIFICATIONID, notificationID);

                    PendingIntent pendingBroadcastIntent = PendingIntent.getBroadcast(this,
                            uniquePendingId,
                            broadcastIntent,
                            0);

                    mBuilder.addAction(R.drawable.ic_action_accept, "Say thanks", pendingBroadcastIntent);

                    pendingIntent = stackBuilder.getPendingIntent(uniquePendingId, PendingIntent.FLAG_ONE_SHOT);
                    break;
                case TRANSACTION:
                    //user made a transaction
                    amount = object.getInt(getString(R.string.contact_send_amount));
                    String outlet = object.getString(getString(R.string.field_business_name));
                    notificationTitle = getString(R.string.gcm_message_transaction);
                    smallContentText = String.format(getString(R.string.gcm_message_transaction_content),
                            amount, outlet);
                    smallContentInfo = String.valueOf(amount);
                    notificationImageResource = R.drawable.ic_noty_point;
                    notificationID = getRandomInt(51, 100);

                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(smallContentText));
                    mBuilder.setColor(getResources().getColor(R.color.alizarin));


                    activityIntent.setClass(this, PointReceivedActivity.class);
                    activityIntent.setAction(NOTIFICATION_ACTION_TX);
                    activityIntent.putExtra(Helper.INTENT_CONTAINER_INFO, msg);
                    activityIntent.putExtra(Helper.NOTIFICATIONID, notificationID);

                    pendingIntent = PendingIntent.getActivity(this, 0, activityIntent,
                            PendingIntent.FLAG_ONE_SHOT);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        mBuilder.setCategory(Notification.CATEGORY_STATUS);
                    break;
                case NEW_VOUCHER:
                    //new marketplace voucher has appeared
                    outlet = object.getString(getString(R.string.field_business_name));
                    notificationTitle = getString(R.string.gcm_message_market);
                    if (object.has("msg"))
                        smallContentText = object.getString("msg") + " @ " + outlet;
                    else
                        smallContentText = String.format(getString(R.string.gcm_message_market_content), outlet);
                    smallContentInfo = "";
                    notificationImageResource = R.drawable.ic_gcm_discount;
                    notificationID = NOTIFICATION_ID_NEW_MARKET;

                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(getString(R.string.gcm_message_market_content), outlet)));
                    mBuilder.setColor(getResources().getColor(R.color.sunflower));

                    activityIntent.setClass(this, MarketActivity.class);
                    activityIntent.setAction(NOTIFICATION_ACTION_NEW_DISCOUNT);
                    activityIntent.putExtra(Helper.INTENT_CONTAINER_INFO, msg);
                    activityIntent.putExtra(Helper.NOTIFICATIONID, NOTIFICATION_ID_NEW_MARKET);
                    activityIntent.putExtra(Helper.MY_DISCOUNT_ITEMS_FLAG, false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        mBuilder.setCategory(Notification.CATEGORY_PROMO);
                    break;
                case NEW_FEED:
                    notificationTitle = "New message";
                    smallContentText = object.getString("msg");
                    notificationImageResource = R.drawable.ic_noty_announcement;
                    notificationID = NOTIFICATION_ID_FEED;

                    activityIntent.setClass(this, FeedActivity.class);
                    activityIntent.putExtra(Helper.NOTIFICATIONID, NOTIFICATION_ID_FEED);
                    activityIntent.setAction(NOTIFICATION_ACTION_FEED);

                    mBuilder.setColor(getResources().getColor(R.color.alizarin));

                    pendingIntent = TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(activityIntent)
                            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    break;
                case THANK_CONTACT:
                    //contact thanks you for sending points
                    String thanksString = object.getString(getString(R.string.field_gcm_name));
                    notificationTitle = getString(R.string.gcm_message_thanked);
                    smallContentText = String.format(getString(R.string.gcm_message_thanked_small_content), thanksString);
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(smallContentText));
                    notificationImageResource = R.drawable.ic_noty_thank;
                    notificationID = NOTIFICATION_ID_THANKED;
                    mBuilder.setColor(getResources().getColor(R.color.belizehole));

                    break;
                case CHECKIN:
                    //checkin notification
                    outlet = object.getString(getString(R.string.field_gcm_name));
                    notificationTitle = getString(R.string.gcm_message_checkin_title);
                    smallContentText = String.format(getString(R.string.gcm_message_checkin_small_content), outlet);
                    notificationImageResource = R.drawable.ic_gcm_checkin;
                    notificationID = NOTIFICATION_ID_CHECKIN;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        mBuilder.setCategory(Notification.CATEGORY_STATUS);
                    mBuilder.setColor(getResources().getColor(R.color.wisteria));

                    break;
                case INFO_NEEDED:
                    notificationTitle = getString(R.string.gcm_message_meta_info_title);
                    smallContentText = getString(R.string.gcm_message_meta_info_title);
                    notificationImageResource = R.drawable.ic_noty_information;
                    notificationID = NOTIFICATION_ID_INFO;

                    mBuilder.setColor(getResources().getColor(R.color.emarald));

                    activityIntent.setClass(this, ExtraInfoActivity.class);
                    activityIntent.setAction(NOTIFICATION_ACTION_INFO);
                    activityIntent.putExtra(Helper.INTENT_CONTAINER_INFO, msg);
                    activityIntent.putExtra(Helper.NOTIFICATIONID, notificationID);

                    pendingIntent = PendingIntent.getActivity(this, 0, activityIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    break;
                case NONE:
                default:
                    notificationTitle = "";
                    smallContentText = "";
                    smallContentInfo = "";
                    notificationID = 0;
            }

            if (pendingIntent == null)
                pendingIntent = PendingIntent.getActivity(this, 0,
                        activityIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationManager mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), notificationImageResource))
                    .setSmallIcon(R.drawable.ic_noty_graaby)
                    .setContentTitle(notificationTitle)
                    .setContentText(smallContentText)
                    .setContentInfo(smallContentInfo)
                    .setContentIntent(pendingIntent);

            mNotificationManager.notify(notificationID, mBuilder.build());
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private int getRandomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

}
