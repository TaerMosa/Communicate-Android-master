package org.inaturalist.android;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import io.fabric.sdk.android.Fabric;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.inaturalist.android.INaturalistService.LoginType;
import org.tatzpiteva.golan.ConfigurationManager;
import org.tatzpiteva.golan.MyProjectsManager;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities.DialogsActivity;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities.SplashActivity;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.SharedPreferencesUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class INaturalistApp extends Application {
    private final static String TAG = "INAT: Application";
    private SharedPreferences mPrefs;
    private NotificationManager mNotificationManager;
    private boolean mIsSyncing = false;
    public static Integer VERSION = 1;
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a z");
    public static SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("d MMM yyyy");
    public static SimpleDateFormat SHORT_TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static Integer SYNC_NOTIFICATION = 3;
    private static Context context;
    private Locale locale = null;
    private Locale deviceLocale = null;
    private OnDownloadFileProgress mDownloadCallback;
    private boolean mOnboardingShownBefore = false;
    // chat students project applecation senglton
    private static INaturalistApp instance;
    public static final String APP_ID = "39066";
    public static final String AUTH_KEY = "dqeA8dXfTQhrcJj";
    public static final String AUTH_SECRET = "ksU4hePrOD6yQuH";
    public static final String ACCOUNT_KEY = "psVsUzXGAw17JrKsi4h8";

    public static final String STICKER_API_KEY = "847b82c49db21ecec88c510e377b452c";

    public static String USER_LOGIN = "";
    public static String USER_PASSWORD = "";

    private String userH ;
    private boolean foreground = false;
    public static INaturalistApp getInstance() {
        return instance;
    }

    public boolean shownOnboarding() {
        return mOnboardingShownBefore;
    }

    public void setShownOnboarding(boolean value) {
        mOnboardingShownBefore = true;
    }

    public interface INotificationCallback {
        public void onNotification(String title, String content);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FacebookSdk.sdkInitialize(getApplicationContext());
        ConfigurationManager.getInstance(getApplicationContext());
        MyProjectsManager.getInstance(getApplicationContext());

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        INaturalistApp.context = getApplicationContext();
        deviceLocale = getResources().getConfiguration().locale;
        applyLocaleSettings();

        // Create the root offline guides directory, if needed
        GuideXML.createOfflineGuidesDirectory(this);

        SharedPreferences pref = getSharedPreferences("iNaturalistPreferences", MODE_PRIVATE);
        String username = pref.getString("username", null);
        if (username != null) {
            setShownOnboarding(true);
            // initialize Quick Blox user
            initQBuser(username);
        }
    }


    //------------------init quickblox user ------------------------------------------
    public void initQBuser(final String Inatusername){
        // Login to REST API
        // Initialise QuickBlox SDK
        //
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
        instance = this;


// create QB session
        QBAuth.createSession(new QBEntityCallback<QBSession>() {

            @Override
            public void onSuccess(QBSession session, Bundle params) {
                // You have successfully created the session
                //
                // Now you can use QuickBlox API!

                final QBUser user = new QBUser(Inatusername,"password");


                user.setFullName(Inatusername);
                StringifyArrayList<String> tags = new StringifyArrayList<String>();
                tags.add("communiUser");
                user.setTags(tags);

                QBUsers.signUp(user, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser user, Bundle args) {

                    }

                    @Override
                    public void onError(QBResponseException errors) {

                    }
                });

                USER_LOGIN = user.getLogin();
                USER_PASSWORD = user.getPassword();

                ChatService.getInstance().login(user, new QBEntityCallback<Void>() {


                    @Override
                    public void onSuccess(Void result, Bundle bundle) {
                        // Go to Dialogs screen
                        //
                        //   Intent intent = new Intent(INaturalistApp.this, DialogsActivity.class);
                        //  startActivity(intent);

                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(INaturalistApp.this);
                        dialog.setMessage("chat login errors: " + errors).create().show();
                    }
                });


            }

            @Override
            public void onError(QBResponseException errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(INaturalistApp.this);
                dialog.setMessage("1 get dialogs errors: " + errors).create().show();
            }
        });
    }

    public String getUserId(){
        return userH;
    }
    /* Used for accessing iNat service results - since passing large amounts of intent data
     * is impossible (for example, returning a huge list of projects/guides won't work via intents)
     */
    private Map<String, Serializable> mServiceResults = new HashMap<String, Serializable>();
    private INotificationCallback mNotificationCallback;

    public void setServiceResult(String key, Serializable value) {
        mServiceResults.put(key,  value);
    }

    public Serializable getServiceResult(String key) {
        return mServiceResults.get(key);
    }


    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public static String getUserCountry(Context context) {
        ActivityHelper helper;
        helper = new ActivityHelper(context);
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception e) { }

        return null;
    }


    /** Returns the set inat network member */
    public String getInaturalistNetworkMember() {
        SharedPreferences settings = getPrefs();
        return settings.getString("pref_network_member", getINatNetworks()[0]);
    }


    /** Set the inat network member */
    public void setInaturalistNetworkMember(String memberNetwork) {
        SharedPreferences settings = getPrefs();
        Editor settingsEditor = settings.edit();

        settingsEditor.putString("pref_network_member", memberNetwork);
        settingsEditor.apply();
    }

    public void detectUserCountryAndUpdateNetwork(Context context) {
        // Don't ask the user again to switch to another network (if he's been asked before)
        if (getInaturalistNetworkMember() != null) return;

        ActivityHelper helper;
        helper = new ActivityHelper(context);

        Resources res = getBaseContext().getResources();

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View titleBarView = inflater.inflate(R.layout.change_network_title_bar, null);
        ImageView titleBarLogo = (ImageView) titleBarView.findViewById(R.id.title_bar_logo);

        String country = getUserCountry(context);
        Log.d(TAG, "Detected country: " + country);

        final String[] inatNetworks = getINatNetworks();

        if (country == null) {
            // Couldn't detect country - set default iNat network
            setInaturalistNetworkMember(inatNetworks[0]);
            return;
        }


        String detectedNetwork = inatNetworks[0]; // Select default iNaturalist network
        for (int i = 0; i < inatNetworks.length; i++) {
            if (country.equalsIgnoreCase(getStringResourceByName("inat_country_" + inatNetworks[i]))) {
                detectedNetwork = inatNetworks[i];
                break;
            }
        }

        // Don't ask the user again to switch if it's the default iNat network
        if (!detectedNetwork.equals(inatNetworks[0])) {
            // Set the logo in the title bar according to network type
            String logoName = getStringResourceByName("inat_logo_" + detectedNetwork);
            String packageName = getPackageName();
            int resId = getResources().getIdentifier(logoName, "drawable", packageName);
            titleBarLogo.setImageResource(resId);

            final String selectedNetwork = detectedNetwork;
            helper.confirm(
                    titleBarView,
                    getStringResourceByName("alert_message_use_" + detectedNetwork),
                    new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setInaturalistNetworkMember(selectedNetwork);
                            restart();
                        }
                    },
                    new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Set default iNaturalist network
                            setInaturalistNetworkMember(inatNetworks[0]);
                        }
                    });
        } else {
            // Set default iNaturalist network
            setInaturalistNetworkMember(inatNetworks[0]);
        }

    }


    public String getFormattedDeviceLocale(){
        if(deviceLocale!=null){
            return deviceLocale.getDisplayLanguage();
        }
        return "";
    }

    public String[] getINatNetworks() {
        Resources res = getResources();
        return res.getStringArray(R.array.inat_networks);
    }

    public String[] getStringArrayResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "array", packageName);
        if (resId == 0) {
            return new String[] { aString };
        } else {
            return getResources().getStringArray(resId);
        }
    }


    public String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        if (resId == 0) {
            return aString;
        } else {
            return getString(resId);
        }
    }

    public void applyLocaleSettings(){
        SharedPreferences settings = getPrefs();

        Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = settings.getString("pref_locale", "");
        if (! "".equals(lang) && ! config.locale.getLanguage().equals(lang))
        {
            String parts[] = lang.split("-r");
            if (parts.length > 1) {
                // Language + country code
                locale = new Locale(parts[0], parts[1]);
            } else {
                // Just the language code
                locale = new Locale(lang);
            }
        }else{
            locale = deviceLocale;
        }
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void restart(){
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Configuration config = new Configuration(newConfig);
        if (locale != null)
        {
            config.locale = locale;
            Locale.setDefault(locale);
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    public static Context getAppContext() {
        return INaturalistApp.context;
    }

    public void setIsSyncing(boolean isSyncing) {
        mIsSyncing  = isSyncing;
    }
    public boolean getIsSyncing() {
        return mIsSyncing;
    }

    public void checkSyncNeeded() {
        Cursor oCursor = getContentResolver().query(Observation.CONTENT_URI, Observation.PROJECTION,
                "_synced_at IS NULL OR (_updated_at > _synced_at)", null, Observation.DEFAULT_SORT_ORDER);
        Cursor opCursor = getContentResolver().query(ObservationPhoto.CONTENT_URI, ObservationPhoto.PROJECTION,
                "_synced_at IS NULL OR (_updated_at > _synced_at)", null, ObservationPhoto.DEFAULT_SORT_ORDER);
        if (!mIsSyncing) {
            mNotificationManager.cancel(SYNC_NOTIFICATION);
        } else {
            Resources res = getResources();
            serviceNotify(SYNC_NOTIFICATION,
                    res.getString(R.string.sync_required),
                    String.format(res.getString(R.string.sync_required_message), oCursor.getCount(), opCursor.getCount()),
                    null,
                    new Intent(INaturalistService.ACTION_SYNC, null, this, INaturalistService.class));
        }
    }

    public boolean loggedIn() {
        return getPrefs().contains("credentials");
    }

    public LoginType getLoginType() {
        return LoginType.valueOf(getPrefs().getString("login_type", LoginType.PASSWORD.toString()));
    }


    public String currentUserLogin() {
        return getPrefs().getString("username", null);
    }

    public SharedPreferences getPrefs() {
        if (mPrefs == null) {
            mPrefs = getSharedPreferences("iNaturalistPreferences", MODE_PRIVATE);
        }
        return mPrefs;
    }

    public void notify(Integer id, String title, String content) {
        notify(id, title, content, null);
    }

    public void notify(Integer id, String title, String content, String ticker) {
        Intent intent = new Intent(this, ObservationListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notify(id, title, content, ticker, intent);
    }

    public void sweepingNotify(Integer id, String title, String content, String ticker, Intent intent) {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
        notify(id, title, content, ticker, intent);
    }

    public void sweepingNotify(Integer id, String title, String content, String ticker) {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
        notify(id, title, content, ticker);
    }

    public void notify(Integer id, String title, String content, String ticker, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notify(id, title, content, ticker, pendingIntent);
    }

    public void serviceNotify(Integer id, String title, String content, String ticker, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        notify(id, title, content, ticker, pendingIntent);
    }

    public void notify(Integer id, String title, String content, String ticker, PendingIntent pendingIntent) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Notification notification = new Notification(R.drawable.ic_stat_inaturalist, ticker, System.currentTimeMillis());
        notification.setLatestEventInfo(getApplicationContext(), title, content, pendingIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        //mNotificationManager.notify(id, notification);
        if (mNotificationCallback != null) {
            mNotificationCallback.onNotification(title, content);
        }
    }

    public void setNotificationCallback(INotificationCallback callback) {
        mNotificationCallback = callback;
    }

    public String formatDate(Timestamp date) { return DATE_FORMAT.format(date); }
    public String formatDatetime(Timestamp date) { return DATETIME_FORMAT.format(date); }
    public String shortFormatDate(Timestamp date) {
        SimpleDateFormat f;
        if (Locale.getDefault().getCountry().equals("US")) {
            f = new SimpleDateFormat("MMM d, yyyy");
        } else {
            f = SHORT_DATE_FORMAT;
        }
        return f.format(date);
    }
    public String shortFormatTime(Timestamp date) { return SHORT_TIME_FORMAT.format(date); }


    public interface OnDownloadFileProgress {
        public boolean onDownloadProgress(long downloaded, long total, String downloadedFilename);
        public void onDownloadError();
    }

    public void setDownloadCallback(OnDownloadFileProgress callback) {
        mDownloadCallback = callback;
    }

    public void downloadFile(final String downloadUrl, final OnDownloadFileProgress callback) {
        mDownloadCallback = callback;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(downloadUrl);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    final int fileSize = connection.getContentLength();

                    // Download the file
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    // Output stream (temp file)
                    File outputFile = File.createTempFile(UUID.randomUUID().toString(), null, getCacheDir());
                    String outputFilename = outputFile.getAbsolutePath();
                    OutputStream output = new FileOutputStream(outputFile);

                    byte data[] = new byte[1024];

                    long total = 0;
                    int count = 0;

                    // Write output data, chunk by chunk
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);

                        mDownloadCallback.onDownloadProgress(total, fileSize, outputFilename);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();

                } catch (IOException exc) {
                    exc.printStackTrace();
                    mDownloadCallback.onDownloadError();
                }

            }
        }).start();
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

    public boolean isForeground() {
        return foreground;
    }

    public boolean isBackground() {
        return !foreground;
    }
}