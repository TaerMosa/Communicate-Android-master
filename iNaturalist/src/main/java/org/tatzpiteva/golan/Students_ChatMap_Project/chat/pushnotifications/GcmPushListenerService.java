package org.tatzpiteva.golan.Students_ChatMap_Project.chat.pushnotifications;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.inaturalist.android.INaturalistApp;
import org.inaturalist.android.R;
import org.tatzpiteva.golan.LaunchScreenActivity;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities.DialogsActivity;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities.SplashActivity;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.NotificationUtils;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.ResourceUtils;


public class GcmPushListenerService extends CoreGcmPushListenerService {
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void showNotification(String message) {
        NotificationUtils.showNotification(this,LaunchScreenActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.drawable.ic_launcher, NOTIFICATION_ID);
    }

    @Override
    protected void sendPushMessageBroadcast(String message) {
        Intent gcmBroadcastIntent = new Intent(GcmConsts.ACTION_NEW_GCM_EVENT);
        gcmBroadcastIntent.putExtra(GcmConsts.EXTRA_GCM_MESSAGE, message);

        LocalBroadcastManager.getInstance(this).sendBroadcast(gcmBroadcastIntent);
    }
}