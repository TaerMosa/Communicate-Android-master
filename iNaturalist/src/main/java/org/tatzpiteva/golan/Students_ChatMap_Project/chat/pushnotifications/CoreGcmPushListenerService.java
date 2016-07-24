package org.tatzpiteva.golan.Students_ChatMap_Project.chat.pushnotifications;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;


import org.inaturalist.android.INaturalistApp;

public abstract class CoreGcmPushListenerService extends GcmListenerService {
    private static final String TAG = CoreGcmPushListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(GcmConsts.EXTRA_GCM_MESSAGE);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        if (INaturalistApp.getInstance().isBackground()) {
            showNotification(message);
        }

        sendPushMessageBroadcast(message);
    }

    protected abstract void showNotification(String message);

    protected abstract void sendPushMessageBroadcast(String message);
}