package org.tatzpiteva.golan.Students_ChatMap_Project.chat.pushnotifications;

import com.google.android.gms.iid.InstanceIDListenerService;

public abstract class CoreGcmPushInstanceIDService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        GooglePlayServicesHelper playServicesHelper = new GooglePlayServicesHelper();
        if (playServicesHelper.checkPlayServicesAvailable()) {
            playServicesHelper.registerForGcm(getSenderId());
        }
    }

    protected abstract String getSenderId();
}
