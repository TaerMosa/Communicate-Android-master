package org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;


import org.inaturalist.android.INaturalistApp;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ApplicationSingleton;

public class ResourceUtils {

    public static String getString(@StringRes int stringId) {
        return INaturalistApp.getInstance().getString(stringId);
    }

    public static Drawable getDrawable(@DrawableRes int drawableId) {
        return INaturalistApp.getInstance().getResources().getDrawable(drawableId);
    }

    public static int getColor(@ColorRes int colorId) {
        return INaturalistApp.getInstance().getResources().getColor(colorId);
    }

    public static int getDimen(@DimenRes int dimenId) {
        return (int) INaturalistApp.getInstance().getResources().getDimension(dimenId);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

}
