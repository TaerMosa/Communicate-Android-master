package org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;



import org.inaturalist.android.INaturalistApp;

public class DeviceUtils {

    public static String getDeviceUid() {
        Context context = INaturalistApp.getInstance();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String uniqueDeviceId = telephonyManager.getDeviceId();

        if (TextUtils.isEmpty(uniqueDeviceId)) {
            // for tablets
            ContentResolver cr = context.getContentResolver();
            uniqueDeviceId = Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID);
        }

        return uniqueDeviceId;
    }

}
