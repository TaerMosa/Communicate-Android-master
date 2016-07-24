package org.tatzpiteva.golan.Students_ChatMap_Project.chat;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

//import vc908.stickerfactory.StickersManager;
//import vc908.stickerfactory.billing.Prices;
//import vc908.stickerfactory.utils.Utils;

public class    ApplicationSingleton extends Application {
    private static final String TAG = ApplicationSingleton.class.getSimpleName();

    public static final String APP_ID = "39066";
    public static final String AUTH_KEY = "dqeA8dXfTQhrcJj";
    public static final String AUTH_SECRET = "ksU4hePrOD6yQuH";
    public static final String ACCOUNT_KEY = "psVsUzXGAw17JrKsi4h8";

    public static final String STICKER_API_KEY = "847b82c49db21ecec88c510e377b452c";

    public static final String USER_LOGIN = "hannasal";
    public static final String USER_PASSWORD = "hannasal";

    private static ApplicationSingleton instance;
    public static ApplicationSingleton getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        instance = this;

        // Initialise QuickBlox SDK
        //
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);


        QBAuth.createSession(new QBEntityCallback<QBSession>() {

            @Override
            public void onSuccess(QBSession session, Bundle params) {
                // You have successfully created the session
                //
                Toast.makeText(getApplicationContext(),"uu", Toast.LENGTH_SHORT).show();

                // Now you can use QuickBlox API!
            }

            @Override
            public void onError(QBResponseException errors) {

            }
        });

        final QBUser user = new QBUser("Javck", "javckpassword");
        user.setExternalId("45345");
        user.setFacebookId("100233453457767");
        user.setTwitterId("182334635457");
        user.setEmail("Javck@mail.com");
        user.setFullName("Javck Bold");
        user.setPhone("+18904567812");
        StringifyArrayList<String> tags = new StringifyArrayList<String>();
        tags.add("car");
        tags.add("man");
        user.setTags(tags);
        user.setWebsite("www.mysite.com");

        QBUsers.signUp(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle args) {
                Toast.makeText(getApplicationContext(),"uu", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(QBResponseException errors) {

            }
        });
        // Initialise Stickers sdk
        //
     //  StickersManager.initialize(STICKER_API_KEY, this);

        // set current user id
        // now it device id, and it means,
        // that all purchases will be bound to current device
    //    StickersManager.setUserID(Utils.getDeviceId(this));
        // register our shop activity for inner currency payment
    //    StickersManager.setShopClass(ShopActivity.class);
        // set prices
    //    StickersManager.setPrices(new Prices()
           //     .setPricePointB("$0.99", 0.99f)
            //    .setPricePointC("$1.99", 1.99f));
    }

    public int getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
