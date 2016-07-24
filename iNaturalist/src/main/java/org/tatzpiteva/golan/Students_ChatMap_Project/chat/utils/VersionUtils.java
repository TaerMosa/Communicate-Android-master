package org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;



import org.inaturalist.android.INaturalistApp;

public class VersionUtils {

    public static int getAppVersion() {
        return getAppPackageInfo().versionCode;
    }

    public static String getAppVersionName() {
        return getAppPackageInfo().versionName;
    }

    private static PackageInfo getAppPackageInfo() {
        Context context = INaturalistApp.getInstance();
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
