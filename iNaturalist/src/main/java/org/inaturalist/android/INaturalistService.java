package org.inaturalist.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class INaturalistService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // How many observations should we initially download for the user
    private static final int INITIAL_SYNC_OBSERVATION_COUNT = 100;

    private boolean mGetLocationForProjects = false; // if true -> we assume it's for near by guides


    public static final String IS_SHARED_ON_APP = "is_shared_on_app";

    public static final String USER = "user";
    public static final String IDENTIFICATION_ID = "identification_id";
    public static final String OBSERVATION_ID = "observation_id";
    public static final String OBSERVATION_RESULT = "observation_result";
    public static final String PROJECTS_RESULT = "projects_result";
    public static final String ADD_OBSERVATION_TO_PROJECT_RESULT = "add_observation_to_project_result";
    public static final String TAXON_ID = "taxon_id";
    public static final String COMMENT_BODY = "comment_body";
    public static final String IDENTIFICATION_BODY = "id_body";
    public static final String PROJECT_ID = "project_id";
    public static final String CHECK_LIST_ID = "check_list_id";
    public static final String ACTION_CHECK_LIST_RESULT = "action_check_list_result";
    public static final String CHECK_LIST_RESULT = "check_list_result";
    public static final String ACTION_GET_TAXON_RESULT = "action_get_taxon_result";
    public static final String TAXON_RESULT = "taxon_result";
    public static final String GUIDE_XML_RESULT = "guide_xml_result";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String LICENSE = "license";
    public static final String REGISTER_USER_ERROR = "error";
    public static final String REGISTER_USER_STATUS = "status";

    public static final int NEAR_BY_OBSERVATIONS_PER_PAGE = 25;

    public static String TAG = "INaturalistService";
    public static String HOST = "https://www.inaturalist.org";
    public static String USER_AGENT = "iNaturalist/" + INaturalistApp.VERSION + " (" +
            "Android " + System.getProperty("os.version") + " " + android.os.Build.VERSION.INCREMENTAL + "; " +
            "SDK " + android.os.Build.VERSION.SDK_INT + "; " +
            android.os.Build.DEVICE + " " +
            android.os.Build.MODEL + " " +
            android.os.Build.PRODUCT + ")";
    public static String ACTION_REGISTER_USER = "register_user";
    public static String ACTION_PASSIVE_SYNC = "passive_sync";
    public static String ACTION_ADD_IDENTIFICATION = "add_identification";
    public static String ACTION_GET_TAXON = "get_taxon";
    public static String ACTION_FIRST_SYNC = "first_sync";
    public static String ACTION_PULL_OBSERVATIONS = "pull_observations";
    public static String ACTION_GET_OBSERVATION = "get_observation";
    public static String ACTION_GET_CHECK_LIST = "get_check_list";
    public static String ACTION_JOIN_PROJECT = "join_project";
    public static String ACTION_LEAVE_PROJECT = "leave_project";
    public static String ACTION_GET_JOINED_PROJECTS = "get_joined_projects";
    public static String ACTION_GET_NEARBY_PROJECTS = "get_nearby_projects";
    public static String ACTION_GET_FEATURED_PROJECTS = "get_featured_projects";
    public static String ACTION_ADD_OBSERVATION_TO_PROJECT = "add_observation_to_project";
    public static String ACTION_REMOVE_OBSERVATION_FROM_PROJECT = "remove_observation_from_project";
    public static String ACTION_GET_ALL_GUIDES = "get_all_guides";
    public static String ACTION_GET_MY_GUIDES = "get_my_guides";
    public static String ACTION_GET_NEAR_BY_GUIDES = "get_near_by_guides";
    public static String ACTION_TAXA_FOR_GUIDE = "get_taxa_for_guide";
    public static String ACTION_GET_USER_DETAILS = "get_user_details";
    public static String ACTION_SYNC = "sync";
    public static String ACTION_NEARBY = "nearby";
    public static String ACTION_AGREE_ID = "agree_id";
    public static String ACTION_REMOVE_ID = "remove_id";
    public static String ACTION_GUIDE_ID = "guide_id";
    public static String ACTION_ADD_COMMENT = "add_comment";
    public static String ACTION_SYNC_COMPLETE = "sync_complete";
    public static String ACTION_OBSERVATION_RESULT = "observation_result";
    public static String ACTION_JOINED_PROJECTS_RESULT = "joined_projects_result";
    public static String ACTION_NEARBY_PROJECTS_RESULT = "nearby_projects_result";
    public static String ACTION_FEATURED_PROJECTS_RESULT = "featured_projects_result";
    public static String ACTION_ALL_GUIDES_RESULT = "all_guides_results";
    public static String ACTION_MY_GUIDES_RESULT = "my_guides_results";
    public static String ACTION_NEAR_BY_GUIDES_RESULT = "near_by_guides_results";
    public static String ACTION_TAXA_FOR_GUIDES_RESULT = "taxa_for_guides_results";
    public static String ACTION_GET_USER_DETAILS_RESULT = "get_user_details_result";
    public static String ACTION_GUIDE_XML_RESULT = "guide_xml_result";
    public static String ACTION_GUIDE_XML = "guide_xml";
    public static String ACTION_GET_PROJECT_DETAILS = "get_project_details";
    public static String ACTION_GET_PROJECT_DETAILS_RESULT = "get_project_details_result";
    public static String GUIDES_RESULT = "guides_result";
    public static String ACTION_REGISTER_USER_RESULT = "register_user_result";
    public static String TAXA_GUIDE_RESULT = "taxa_guide_result";
    public static Integer SYNC_OBSERVATIONS_NOTIFICATION = 1;
    public static Integer SYNC_PHOTOS_NOTIFICATION = 2;
    public static Integer AUTH_NOTIFICATION = 3;
    private String mLogin;
    private String mCredentials;
    private SharedPreferences mPreferences;
    private boolean mPassive;
    private INaturalistApp mApp;
    private LoginType mLoginType;

    private boolean mIsStopped = false;

    private boolean mIsSyncing;

    private Handler mHandler;

    private GoogleApiClient mLocationClient;

    private ArrayList<SerializableJSONArray> mProjectObservations;

    private Hashtable<Integer, Hashtable<Integer, ProjectFieldValue>> mProjectFieldValues;

    private Header[] mResponseHeaders = null;

    private JSONArray mResponseErrors;

    private String mNearByObservationsUrl;

    public enum LoginType {
        PASSWORD,
        GOOGLE,
        LoginType, FACEBOOK
    };


    public INaturalistService() {
        super("INaturalistService");

        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mPreferences = getSharedPreferences("iNaturalistPreferences", MODE_PRIVATE);
        mLogin = mPreferences.getString("username", null);
        mCredentials = mPreferences.getString("credentials", null);
        mLoginType = LoginType.valueOf(mPreferences.getString("login_type", LoginType.PASSWORD.toString()));
        mApp = (INaturalistApp) getApplicationContext();
        String action = intent.getAction();

        if (action == null) return;

        mPassive = action.equals(ACTION_PASSIVE_SYNC);


        Log.d(TAG, "Service: " + action);

        try {
            if (action.equals(ACTION_NEARBY)) {
                getNearbyObservations(intent);

            } else if (action.equals(ACTION_FIRST_SYNC)) {
                mIsSyncing = true;
                mApp.setIsSyncing(mIsSyncing);

                saveJoinedProjects();
                getUserObservations(INITIAL_SYNC_OBSERVATION_COUNT);
                syncObservationFields();
                postProjectObservations();

            } else if (action.equals(ACTION_AGREE_ID)) {
                int observationId = intent.getIntExtra(OBSERVATION_ID, 0);
                int taxonId = intent.getIntExtra(TAXON_ID, 0);
                JSONObject result = agreeIdentification(observationId, taxonId);

                if (result != null) {
                    // Reload the observation at the end (need to refresh comment/ID list)
                    Observation observation = getObservation(observationId);

                    Intent reply = new Intent(ACTION_OBSERVATION_RESULT);
                    reply.putExtra(OBSERVATION_RESULT, observation);
                    sendBroadcast(reply);
                }

            } else if (action.equals(ACTION_REMOVE_ID)) {
                int id = intent.getIntExtra(IDENTIFICATION_ID, 0);
                int observationId = intent.getIntExtra(OBSERVATION_ID, 0);
                JSONObject result = removeIdentification(id);

                // Reload the observation at the end (need to refresh comment/ID list)
                Observation observation = getObservation(observationId);

                Intent reply = new Intent(ACTION_OBSERVATION_RESULT);
                reply.putExtra(OBSERVATION_RESULT, observation);
                sendBroadcast(reply);


            } else if (action.equals(ACTION_ADD_IDENTIFICATION)) {
                int observationId = intent.getIntExtra(OBSERVATION_ID, 0);
                int taxonId = intent.getIntExtra(TAXON_ID, 0);
                String body = intent.getStringExtra(IDENTIFICATION_BODY);
                addIdentification(observationId, taxonId, body);

            } else if (action.equals(ACTION_REGISTER_USER)) {
                String email = intent.getStringExtra(EMAIL);
                String password = intent.getStringExtra(PASSWORD);
                String username = intent.getStringExtra(USERNAME);
                String license = intent.getStringExtra(LICENSE);

                String error = registerUser(email, password, username, license);

                Intent reply = new Intent(ACTION_REGISTER_USER_RESULT);
                reply.putExtra(REGISTER_USER_STATUS, error == null);
                reply.putExtra(REGISTER_USER_ERROR, error);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_GET_TAXON)) {
                int taxonId = intent.getIntExtra(TAXON_ID, 0);
                BetterJSONObject taxon = getTaxon(taxonId);

                Intent reply = new Intent(ACTION_GET_TAXON_RESULT);
                reply.putExtra(TAXON_RESULT, taxon);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_ADD_COMMENT)) {
                int observationId = intent.getIntExtra(OBSERVATION_ID, 0);
                String body = intent.getStringExtra(COMMENT_BODY);
                addComment(observationId, body);

            } else if (action.equals(ACTION_GUIDE_XML)) {
                int guideId = intent.getIntExtra(ACTION_GUIDE_ID, 0);
                String guideXMLFilename = getGuideXML(guideId);

                if (guideXMLFilename == null) {
                    // Failed to get the guide XML - try and find the offline version, if available
                    GuideXML guideXml = new GuideXML(this, String.valueOf(guideId));

                    if (guideXml.isGuideDownloaded()) {
                        guideXMLFilename = guideXml.getOfflineGuideXmlFilePath();
                    }
                }

                Intent reply = new Intent(ACTION_GUIDE_XML_RESULT);
                reply.putExtra(GUIDE_XML_RESULT, guideXMLFilename);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_GET_USER_DETAILS)) {
                BetterJSONObject user = getUserDetails();

                Intent reply = new Intent(ACTION_GET_USER_DETAILS_RESULT);
                reply.putExtra(USER, user);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_TAXA_FOR_GUIDE)) {
                int guideId = intent.getIntExtra(ACTION_GUIDE_ID, 0);
                SerializableJSONArray taxa = getTaxaForGuide(guideId);

                mApp.setServiceResult(ACTION_TAXA_FOR_GUIDES_RESULT, taxa);
                Intent reply = new Intent(ACTION_TAXA_FOR_GUIDES_RESULT);
                reply.putExtra(IS_SHARED_ON_APP, true);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_GET_ALL_GUIDES)) {
                SerializableJSONArray guides = getAllGuides();

                mApp.setServiceResult(ACTION_ALL_GUIDES_RESULT, guides);
                Intent reply = new Intent(ACTION_ALL_GUIDES_RESULT);
                reply.putExtra(IS_SHARED_ON_APP, true);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_GET_MY_GUIDES)) {
                SerializableJSONArray guides = null;
                guides = getMyGuides();

                Intent reply = new Intent(ACTION_MY_GUIDES_RESULT);
                reply.putExtra(GUIDES_RESULT, guides);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_GET_NEAR_BY_GUIDES)) {
                int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

                // If Google Play services is available
                if (ConnectionResult.SUCCESS == resultCode) {
                    // Use Google Location Services to determine location
                    mLocationClient =  new GoogleApiClient.Builder(getApplicationContext())
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                    mLocationClient.connect();

                    // Only once we're connected - we'll call getNearByGuides()
                    mGetLocationForProjects = false;

                } else {
                    // Use GPS for the location
                    SerializableJSONArray guides = getNearByGuides(false);

                    Intent reply = new Intent(ACTION_NEAR_BY_GUIDES_RESULT);
                    reply.putExtra(GUIDES_RESULT, guides);
                    sendBroadcast(reply);
                }

            } else if (action.equals(ACTION_GET_NEARBY_PROJECTS)) {
                int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

                // If Google Play services is available
                if (ConnectionResult.SUCCESS == resultCode) {
                    // Use Google Location Services to determine location
                    mLocationClient = new GoogleApiClient.Builder(getApplicationContext())
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                    mLocationClient.connect();

                    // Only once we're connected - we'll call getNearByProjects()
                    mGetLocationForProjects = true;

                } else {
                    // Use GPS for the location
                    SerializableJSONArray projects = getNearByProjects(false);

                    Intent reply = new Intent(ACTION_NEARBY_PROJECTS_RESULT);
                    reply.putExtra(PROJECTS_RESULT, projects);
                    sendBroadcast(reply);
                }

            } else if (action.equals(ACTION_GET_FEATURED_PROJECTS)) {
                SerializableJSONArray projects = getFeaturedProjects();

                Intent reply = new Intent(ACTION_FEATURED_PROJECTS_RESULT);
                reply.putExtra(PROJECTS_RESULT, projects);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_GET_JOINED_PROJECTS)) {
                SerializableJSONArray projects = null;
                if (mCredentials != null) {
                    projects = getJoinedProjectsOffline();
                }

                Intent reply = new Intent(ACTION_JOINED_PROJECTS_RESULT);
                reply.putExtra(PROJECTS_RESULT, projects);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_REMOVE_OBSERVATION_FROM_PROJECT)) {
                int observationId = intent.getExtras().getInt(OBSERVATION_ID);
                int projectId = intent.getExtras().getInt(PROJECT_ID);
                BetterJSONObject result = removeObservationFromProject(observationId, projectId);

            } else if (action.equals(ACTION_ADD_OBSERVATION_TO_PROJECT)) {
                int observationId = intent.getExtras().getInt(OBSERVATION_ID);
                int projectId = intent.getExtras().getInt(PROJECT_ID);
                BetterJSONObject result = addObservationToProject(observationId, projectId);

                Intent reply = new Intent(ADD_OBSERVATION_TO_PROJECT_RESULT);
                reply.putExtra(ADD_OBSERVATION_TO_PROJECT_RESULT, result);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_GET_CHECK_LIST)) {
                int id = intent.getExtras().getInt(CHECK_LIST_ID);
                SerializableJSONArray checkList = getCheckList(id);

                Intent reply = new Intent(ACTION_CHECK_LIST_RESULT);
                reply.putExtra(CHECK_LIST_RESULT, checkList);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_GET_OBSERVATION)) {
                int id = intent.getExtras().getInt(OBSERVATION_ID);
                Observation observation = getObservation(id);

                Intent reply = new Intent(ACTION_OBSERVATION_RESULT);
                reply.putExtra(OBSERVATION_RESULT, observation);
                sendBroadcast(reply);

            } else if (action.equals(ACTION_JOIN_PROJECT)) {
                int id = intent.getExtras().getInt(PROJECT_ID);
                joinProject(id);

            } else if (action.equals(ACTION_LEAVE_PROJECT)) {
                int id = intent.getExtras().getInt(PROJECT_ID);
                leaveProject(id);

            } else if (action.equals(ACTION_PULL_OBSERVATIONS)) {
                // Download observations without uploading any new ones
                mIsSyncing = true;
                mApp.setIsSyncing(mIsSyncing);

                getUserObservations(0);

                // Update last sync time
                long lastSync = System.currentTimeMillis();
                mPreferences.edit().putLong("last_sync_time", lastSync).commit();
            } else if (action.equals(ACTION_GET_PROJECT_DETAILS)) {

                SerializableJSONArray projects = getProjectDetails(intent.getExtras().getInt(PROJECT_ID));

                Intent reply = new Intent(ACTION_GET_PROJECT_DETAILS_RESULT);
                reply.putExtra(PROJECTS_RESULT, projects);
                reply.putExtra(PROJECT_ID, intent.getExtras().getInt(PROJECT_ID));
                sendBroadcast(reply);

            } else {
                mIsSyncing = true;
                mApp.setIsSyncing(mIsSyncing);
                syncObservations();

                // Update last sync time
                long lastSync = System.currentTimeMillis();
                mPreferences.edit().putLong("last_sync_time", lastSync).commit();

            }
        } catch (AuthenticationException e) {
            if (!mPassive) {
                requestCredentials();
            }
        } finally {
            if (mIsSyncing) {
                mIsSyncing = false;
                mApp.setIsSyncing(mIsSyncing);

                Log.i(TAG, "Sending ACTION_SYNC_COMPLETE");

                // Notify the rest of the app of the completion of the sync
                Intent reply = new Intent(ACTION_SYNC_COMPLETE);
                sendBroadcast(reply);
            }
        }
    }

    private void syncObservations() throws AuthenticationException {
        deleteObservations(); // Delete locally-removed observations
        saveJoinedProjects();
        getUserObservations(0); // First, download remote observations (new/updated)
        postObservations(); // Next, update local-to-remote observations
        syncObservationFields();
        postPhotos();
        postProjectObservations();


//        Toast.makeText(getApplicationContext(), "Observations synced", Toast.LENGTH_SHORT);
    }

    private BetterJSONObject getTaxon(int id) throws AuthenticationException {
        String url = String.format("%s/taxa/%d.json", HOST, id);

        JSONArray json = get(url);
        if (json == null || json.length() == 0) { return null; }

        JSONObject res;

        try {
            res = (JSONObject) json.get(0);
        } catch (JSONException e) {
            return null;
        }

        return new BetterJSONObject(res);
    }

    private void postProjectObservations() throws AuthenticationException {
        // First, delete any project-observations that were deleted by the user
        Cursor c = getContentResolver().query(ProjectObservation.CONTENT_URI,
                ProjectObservation.PROJECTION,
                "is_deleted = 1",
                null,
                ProjectObservation.DEFAULT_SORT_ORDER);

        c.moveToFirst();
        while (c.isAfterLast() == false) {
            ProjectObservation projectObservation = new ProjectObservation(c);
            removeObservationFromProject(projectObservation.observation_id, projectObservation.project_id);
            c.moveToNext();
        }

        // Now it's safe to delete all of the project-observations locally
        getContentResolver().delete(ProjectObservation.CONTENT_URI, "is_deleted = 1", null);


        // Next, add new project observations
        c = getContentResolver().query(ProjectObservation.CONTENT_URI,
                ProjectObservation.PROJECTION,
                "is_new = 1",
                null,
                ProjectObservation.DEFAULT_SORT_ORDER);

        c.moveToFirst();
        while (c.isAfterLast() == false) {
            ProjectObservation projectObservation = new ProjectObservation(c);
            BetterJSONObject result = addObservationToProject(projectObservation.observation_id, projectObservation.project_id);

            if (mResponseErrors != null) {
                SerializableJSONArray errors = new SerializableJSONArray(mResponseErrors);

                // Couldn't add the observation to the project (probably didn't pass validation)
                String error;
                try {
                    error = errors.getJSONArray().getString(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                    c.moveToNext();
                    continue;
                }

                Cursor c2 = getContentResolver().query(Observation.CONTENT_URI, Observation.PROJECTION, "id = '"+projectObservation.observation_id+"'", null, Observation.DEFAULT_SORT_ORDER);
                c2.moveToFirst();
                if (c2.getCount() == 0) {
                    break;
                }
                Observation observation = new Observation(c2);
                c2.close();

                c2 = getContentResolver().query(Project.CONTENT_URI, Project.PROJECTION, "id = '"+projectObservation.project_id+"'", null, Project.DEFAULT_SORT_ORDER);
                c2.moveToFirst();
                if (c2.getCount() == 0) {
                    break;
                }
                Project project = new Project(c2);
                c2.close();

                final String errorMessage = String.format(getString(R.string.failed_to_add_obs_to_project), observation.species_guess, project.title, error);

                // Notify user
                mApp.sweepingNotify(SYNC_OBSERVATIONS_NOTIFICATION,
                        getString(R.string.syncing_observations),
                        errorMessage,
                        getString(R.string.syncing));

                // Display toast in this main thread handler (since otherwise it won't get displayed)
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Toast doesn't support longer periods of display - this is a workaround
                        for (int i = 0; i < 3; i++) {
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else {
                // Unmark as new
                projectObservation.is_new = false;
                ContentValues cv = projectObservation.getContentValues();
                getContentResolver().update(projectObservation.getUri(), cv, null, null);
            }

            c.moveToNext();
        }

        // Finally, retrieve all project observations
        for (int j = 0; j < mProjectObservations.size(); j++) {
            JSONArray projectObservations = mProjectObservations.get(j).getJSONArray();

            for (int i = 0; i < projectObservations.length(); i++) {
                JSONObject jsonProjectObservation;
                try {
                    jsonProjectObservation = projectObservations.getJSONObject(i);
                    ProjectObservation projectObservation = new ProjectObservation(new BetterJSONObject(jsonProjectObservation));
                    ContentValues cv = projectObservation.getContentValues();
                    getContentResolver().insert(ProjectObservation.CONTENT_URI, cv);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void saveJoinedProjects() throws AuthenticationException {
        SerializableJSONArray projects = getJoinedProjects();

        if (projects != null) {
            JSONArray arr = projects.getJSONArray();

            try {
                // First, delete all joined projects
                getContentResolver().delete(Project.CONTENT_URI, null, null);
            } catch (Exception exc) {
                exc.printStackTrace();
                return;
            }

            // Next, add the new joined projects
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject jsonProject = arr.getJSONObject(i);
                    Project project = new Project(new BetterJSONObject(jsonProject));
                    ContentValues cv = project.getContentValues();
                    getContentResolver().insert(Project.CONTENT_URI, cv);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deleteObservations() throws AuthenticationException {
        // Remotely delete any locally-removed observations
        Cursor c = getContentResolver().query(Observation.CONTENT_URI,
                Observation.PROJECTION,
                "is_deleted = 1 AND user_login = '"+mLogin+"'",
                null,
                Observation.DEFAULT_SORT_ORDER);

        // for each observation DELETE to /observations/:id
        ArrayList<Integer> obsIds = new ArrayList<Integer>();
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            Observation observation = new Observation(c);
            delete(HOST + "/observations/" + observation.id + ".json", null);
            obsIds.add(observation.id);
            c.moveToNext();
        }

        // Now it's safe to delete all of the observations locally
        getContentResolver().delete(Observation.CONTENT_URI, "is_deleted = 1", null);
        // Delete associated project-fields and photos
        int count1 = getContentResolver().delete(ObservationPhoto.CONTENT_URI, "observation_id in (" + StringUtils.join(obsIds, ",") + ")", null);
        int count2 = getContentResolver().delete(ProjectObservation.CONTENT_URI, "observation_id in (" + StringUtils.join(obsIds, ",") + ")", null);
        int count3 = getContentResolver().delete(ProjectFieldValue.CONTENT_URI, "observation_id in (" + StringUtils.join(obsIds, ",") + ")", null);
    }

    private JSONObject agreeIdentification(int observationId, int taxonId) throws AuthenticationException {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("identification[observation_id]", new Integer(observationId).toString()));
        params.add(new BasicNameValuePair("identification[taxon_id]", new Integer(taxonId).toString()));

        JSONArray result = post(HOST + "/identifications.json", params);

        if (result != null) {
            try {
                return result.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private JSONObject removeIdentification(int identificationId) throws AuthenticationException {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        JSONArray result = delete(HOST + "/identifications/" + identificationId + ".json", null);

        if (result != null) {
            try {
                return result.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }


    private void addIdentification(int observationId, int taxonId, String body) throws AuthenticationException {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("identification[observation_id]", new Integer(observationId).toString()));
        params.add(new BasicNameValuePair("identification[taxon_id]", new Integer(taxonId).toString()));
        params.add(new BasicNameValuePair("identification[body]", body));

        JSONArray arrayResult = post(HOST + "/identifications.json", params);

        if (arrayResult != null) {
            BetterJSONObject result;
            try {
                result = new BetterJSONObject(arrayResult.getJSONObject(0));
                JSONObject jsonObservation = result.getJSONObject("observation");
                Observation remoteObservation = new Observation(new BetterJSONObject(jsonObservation));

                Cursor c = getContentResolver().query(Observation.CONTENT_URI,
                        Observation.PROJECTION,
                        "id = "+ remoteObservation.id, null, Observation.DEFAULT_SORT_ORDER);

                // update local observation
                c.moveToFirst();
                if (c.isAfterLast() == false) {
                    Observation observation = new Observation(c);
                    boolean isModified = observation.merge(remoteObservation);
                    ContentValues cv = observation.getContentValues();
                    if (observation._updated_at.before(remoteObservation.updated_at)) {
                        // Remote observation is newer (and thus has overwritten the local one) - update its
                        // sync at time so we won't update the remote servers later on (since we won't
                        // accidently consider this an updated record)
                        cv.put(Observation._SYNCED_AT, System.currentTimeMillis());
                    }
                    if (isModified) {
                        // Only update the DB if needed
                        getContentResolver().update(observation.getUri(), cv, null, null);
                    }
                }
                c.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Registers a user - returns an error message in case of an error (null if successful)
    private String registerUser(String email, String password, String username, String license) throws AuthenticationException {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user[email]", email));
        params.add(new BasicNameValuePair("user[login]", username));
        params.add(new BasicNameValuePair("user[password]", password));
        params.add(new BasicNameValuePair("user[password_confirmation]", password));
        String inatNetwork = mApp.getInaturalistNetworkMember();
        params.add(new BasicNameValuePair("user[site_id]", mApp.getStringResourceByName("inat_site_id_" + inatNetwork)));
        params.add(new BasicNameValuePair("user[preferred_observation_license]", license));
        params.add(new BasicNameValuePair("user[preferred_photo_license]", license));
        params.add(new BasicNameValuePair("user[preferred_sound_license]", license));
        Locale deviceLocale = getResources().getConfiguration().locale;
        String deviceLanguage =   deviceLocale.getLanguage();
        params.add(new BasicNameValuePair("user[locale]", deviceLanguage));

        post(HOST + "/users.json", params, false);
        if (mResponseErrors != null) {
            // Couldn't create user
            try {
                return mResponseErrors.getString(0);
            } catch (JSONException e) {
                e.printStackTrace();
                return "Unknown error";
            }
        } else {
            return null;
        }
    }


    private void addComment(int observationId, String body) throws AuthenticationException {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("comment[parent_id]", new Integer(observationId).toString()));
        params.add(new BasicNameValuePair("comment[parent_type]", "Observation"));
        params.add(new BasicNameValuePair("comment[body]", body));

        post(HOST + "/comments.json", params);
    }

    private void postObservations() throws AuthenticationException {
        Observation observation;
        // query observations where _updated_at > updated_at
        Cursor c = getContentResolver().query(Observation.CONTENT_URI,
                Observation.PROJECTION,
                "_updated_at > _synced_at AND _synced_at IS NOT NULL AND user_login = '"+mLogin+"'",
                null,
                Observation.SYNC_ORDER);
        int updatedCount = c.getCount();
        mApp.sweepingNotify(SYNC_OBSERVATIONS_NOTIFICATION,
                getString(R.string.syncing_observations),
                String.format(getString(R.string.syncing_x_observations), c.getCount()),
                getString(R.string.syncing));
        // for each observation PUT to /observations/:id
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            mApp.notify(SYNC_OBSERVATIONS_NOTIFICATION,
                    getString(R.string.updating_observations),
                    String.format(getString(R.string.updating_x_observations), (c.getPosition() + 1), c.getCount()),
                    getString(R.string.syncing));
            observation = new Observation(c);
            handleObservationResponse(
                    observation,
                    put(HOST + "/observations/" + observation.id + ".json?extra=observation_photos", paramsForObservation(observation, false))
            );
            c.moveToNext();
        }
        c.close();

        String inatNetwork = mApp.getInaturalistNetworkMember();
        String inatHost = mApp.getStringResourceByName("inat_host_" + inatNetwork);

        // query observations where _synced_at IS NULL
        c = getContentResolver().query(Observation.CONTENT_URI,
                Observation.PROJECTION,
                "id IS NULL", null, Observation.SYNC_ORDER);
        int createdCount = c.getCount();
        // for each observation POST to /observations/

        c.moveToFirst();
        while (c.isAfterLast() == false) {
            mApp.notify(SYNC_OBSERVATIONS_NOTIFICATION,
                    getString(R.string.posting_observations),
                    String.format(getString(R.string.posting_x_observations), (c.getPosition() + 1), c.getCount()),
                    getString(R.string.syncing));
            observation = new Observation(c);
            handleObservationResponse(
                    observation,
                    post("http://" + inatHost + "/observations.json?extra=observation_photos", paramsForObservation(observation, true))
            );
            c.moveToNext();
        }
        c.close();

        c = getContentResolver().query(Observation.CONTENT_URI,
                Observation.PROJECTION,
                "id IS NULL", null, Observation.SYNC_ORDER);
        int currentCreatedCount = c.getCount();
        c.close();
        c = getContentResolver().query(Observation.CONTENT_URI,
                Observation.PROJECTION,
                "_updated_at > _synced_at AND _synced_at IS NOT NULL AND user_login = '"+mLogin+"'",
                null,
                Observation.SYNC_ORDER);
        int currentUpdatedCount = c.getCount();
        c.close();


        if ((currentCreatedCount == 0) && (currentUpdatedCount == 0)) {
            // Sync completed successfully
            mApp.notify(SYNC_OBSERVATIONS_NOTIFICATION,
                    getString(R.string.observation_sync_complete),
                    String.format(getString(R.string.observation_sync_status), createdCount, updatedCount),
                    getString(R.string.sync_complete));
        } else {
            // There was a problem with the sync process
            mApp.notify(SYNC_OBSERVATIONS_NOTIFICATION,
                    getString(R.string.observation_sync_failed),
                    getString(R.string.not_all_observations_were_synced),
                    getString(R.string.sync_failed));
        }
    }


    private Observation getObservation(int id) throws AuthenticationException {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Locale deviceLocale = getResources().getConfiguration().locale;
        String deviceLanguage =   deviceLocale.getLanguage();

        String url = String.format("%s/observations/%d.json?locale=%s", HOST, id, deviceLanguage);

        JSONArray json = get(url);
        if (json == null || json.length() == 0) { return null; }

        JSONObject observation;

        try {
            observation = (JSONObject) json.get(0);
        } catch (JSONException e) {
            return null;
        }

        return new Observation(new BetterJSONObject(observation));
    }

    private void postPhotos() throws AuthenticationException {
        ObservationPhoto op;
        int createdCount = 0;
        // query observations where _updated_at > updated_at
        Cursor c = getContentResolver().query(ObservationPhoto.CONTENT_URI,
                ObservationPhoto.PROJECTION,
                "_synced_at IS NULL", null, ObservationPhoto.DEFAULT_SORT_ORDER);
        if (c.getCount() == 0) {
            return;
        }

        // for each observation PUT to /observations/:id
        ContentValues cv;
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            mApp.notify(SYNC_PHOTOS_NOTIFICATION,
                    getString(R.string.posting_photos),
                    String.format(getString(R.string.posting_x_photos), (c.getPosition() + 1), c.getCount()),
                    getString(R.string.syncing));
            op = new ObservationPhoto(c);
            ArrayList <NameValuePair> params = op.getParams();
            // http://stackoverflow.com/questions/2935946/sending-images-using-http-post
            Uri photoUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, op._photo_id);
            Cursor pc = getContentResolver().query(photoUri,
                    new String[] {MediaStore.MediaColumns._ID, MediaStore.Images.Media.DATA},
                    null,
                    null,
                    MediaStore.Images.Media.DEFAULT_SORT_ORDER);

            if (pc == null) {
                continue;
            } else if (pc.getCount() == 0) {
                // photo has been deleted, destroy the ObservationPhoto
                getContentResolver().delete(op.getUri(), null, null);
                continue;
            } else {
                pc.moveToFirst();
            }

            String imgFilePath = pc.getString(pc.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            params.add(new BasicNameValuePair("file", imgFilePath));

            String inatNetwork = mApp.getInaturalistNetworkMember();
            String inatHost = mApp.getStringResourceByName("inat_host_" + inatNetwork);
            params.add(new BasicNameValuePair("site_id", mApp.getStringResourceByName("inat_site_id_" + inatNetwork)));

            JSONArray response = post("http://" + inatHost + "/observation_photos.json", params);
            try {
                if (response == null || response.length() != 1) {
                    break;
                }
                JSONObject json = response.getJSONObject(0);
                BetterJSONObject j = new BetterJSONObject(json);
                ObservationPhoto jsonObservationPhoto = new ObservationPhoto(j);
                op.merge(jsonObservationPhoto);
                cv = op.getContentValues();
                cv.put(ObservationPhoto._SYNCED_AT, System.currentTimeMillis());
                getContentResolver().update(op.getUri(), cv, null, null);
                createdCount += 1;
            } catch (JSONException e) {
                Log.e(TAG, "JSONException: " + e.toString());
            }
            c.moveToNext();
        }
        c.close();

        c = getContentResolver().query(ObservationPhoto.CONTENT_URI,
                ObservationPhoto.PROJECTION,
                "_synced_at IS NULL", null, ObservationPhoto.DEFAULT_SORT_ORDER);
        int currentCount = c.getCount();
        c.close();

        if (currentCount == 0) {
            // Sync completed successfully
            mApp.notify(SYNC_PHOTOS_NOTIFICATION,
                    getString(R.string.photo_sync_complete),
                    String.format(getString(R.string.posted_new_x_photos), createdCount),
                    getString(R.string.sync_complete));
        } else {
            // Sync failed
            mApp.notify(SYNC_PHOTOS_NOTIFICATION,
                    getString(R.string.photo_sync_failed),
                    getString(R.string.not_all_photos_were_synced),
                    getString(R.string.sync_failed));
        }
    }

    private String getGuideXML(Integer guideId) throws AuthenticationException {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Locale deviceLocale = getResources().getConfiguration().locale;
        String deviceLanguage =   deviceLocale.getLanguage();

        String url = HOST + "/guides/" + guideId.toString() + ".xml?locale=" + deviceLanguage;

        try {
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = null;
            response = httpClient.execute(httpGet);

            InputStream buffer = new BufferedInputStream(response.getEntity().getContent());
            File outputFile = File.createTempFile(guideId.toString() + ".xml", null, getBaseContext().getCacheDir());
            OutputStream output = new FileOutputStream(outputFile);

            int count = 0;
            byte data[] = new byte[1024];
            while ((count = buffer.read(data)) != -1) {
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            buffer.close();

            // Return the downloaded full file name
            return outputFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private BetterJSONObject getUserDetails() throws AuthenticationException {
        String url = HOST + "/users/edit.json";
        JSONArray json = get(url, true);
        try {
            if (json == null) return null;
            if (json.length() == 0) return null;
            return new BetterJSONObject(json.getJSONObject(0));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private SerializableJSONArray getTaxaForGuide(Integer guideId) throws AuthenticationException {
        String url = HOST + "/guide_taxa.json?guide_id=" + guideId.toString();
        JSONArray json = get(url);
        try {
            return new SerializableJSONArray(json.getJSONObject(0).getJSONArray("guide_taxa"));
        } catch (JSONException e) {
            e.printStackTrace();
            return new SerializableJSONArray();
        }
    }


    private SerializableJSONArray getAllGuides() throws AuthenticationException {
        String inatNetwork = mApp.getInaturalistNetworkMember();
        String inatHost = mApp.getStringResourceByName("inat_host_" + inatNetwork);

        String url = "http://" + inatHost + "/guides.json?";

        url += "per_page=200&page=";

        JSONArray results = new JSONArray();

        // Results are paginated - make sure to retrieve them all

        int page = 1;
        JSONArray currentResults = get(url + page);

        while ((currentResults != null) && (currentResults.length() > 0)) {
            // Append current results
            for (int i = 0; i < currentResults.length(); i++) {
                try {
                    results.put(currentResults.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            page++;
            currentResults = get(url + page);
        }

        return new SerializableJSONArray(results);
    }

    private SerializableJSONArray getMyGuides() throws AuthenticationException {
        JSONArray json = null;
        String inatNetwork = mApp.getInaturalistNetworkMember();
        String inatHost = mApp.getStringResourceByName("inat_host_" + inatNetwork);
        String url = "http://" + inatHost + "/guides.json?by=you&per_page=200";

        if (mCredentials != null) {
            try {
                json = get(url, true);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        if (json == null) {
            json = new JSONArray();
        }

        // Build a list of result guide IDs
        int i = 0;
        List<String> guideIds = new ArrayList<String>();
        while (i < json.length()) {
            try {
                JSONObject guide = json.getJSONObject(i);
                guideIds.add(String.valueOf(guide.getInt("id")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }

        // Add any offline guides
        List<GuideXML> offlineGuides = GuideXML.getAllOfflineGuides(this);
        List<JSONObject> guidesJson = new ArrayList<JSONObject>();

        for (GuideXML guide: offlineGuides) {
            JSONObject guideJson = new JSONObject();
            if (guideIds.contains(guide.getID())) {
                // Guide already found in current guide results - no need to add it again
                continue;
            }

            try {
                guideJson.put("id", Integer.valueOf(guide.getID()));
                guideJson.put("title", guide.getTitle());
                guideJson.put("description", guide.getDescription());
                // TODO - no support for "icon_url" (not found in XML file)
            } catch (JSONException e) {
                e.printStackTrace();
            }

            json.put(guideJson);
        }

        return new SerializableJSONArray(json);
    }

    private SerializableJSONArray getNearByGuides(boolean useLocationServices) throws AuthenticationException {
        if (useLocationServices) {
            Location location;
            try {
                location =  LocationServices.FusedLocationApi.getLastLocation(
                        mLocationClient);
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
                return new SerializableJSONArray();
            }

            return getNearByGuides(location);
        } else {
            // Use GPS alone to determine location
            LocationManager locationManager = (LocationManager)mApp.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            return getNearByGuides(location);
        }
    }

    private SerializableJSONArray getNearByGuides(Location location) throws AuthenticationException {
        if (location == null) {
            // No location found - return an empty result
            Log.e(TAG, "Current location is null");
            return new SerializableJSONArray();
        }

        double lat  = location.getLatitude();
        double lon  = location.getLongitude();

        String inatNetwork = mApp.getInaturalistNetworkMember();
        String inatHost = mApp.getStringResourceByName("inat_host_" + inatNetwork);

        String url = "http://" + inatHost + String.format("/guides.json?latitude=%s&longitude=%s&per_page=200", lat, lon);
        Log.e(TAG, url);

        JSONArray json = get(url);

        return new SerializableJSONArray(json);
    }


    private SerializableJSONArray getNearByProjects(Location location) throws AuthenticationException {
        if (location == null) {
            // No location found - return an empty result
            Log.e(TAG, "Current location is null");
            return new SerializableJSONArray();
        }

        double lat  = location.getLatitude();
        double lon  = location.getLongitude();

        String inatNetwork = mApp.getInaturalistNetworkMember();
        String inatHost = mApp.getStringResourceByName("inat_host_" + inatNetwork);

        String url = "http://" + inatHost + String.format("/projects.json?latitude=%s&longitude=%s", lat, lon);

        Log.e(TAG, url);

        JSONArray json = get(url);

        if (json == null) {
            return new SerializableJSONArray();
        }

        // Determine which projects are already joined
        for (int i = 0; i < json.length(); i++) {
            Cursor c;
            try {
                c = getContentResolver().query(Project.CONTENT_URI, Project.PROJECTION, "id = '"+json.getJSONObject(i).getInt("id")+"'", null, Project.DEFAULT_SORT_ORDER);
                c.moveToFirst();
                if (c.getCount() > 0) {
                    json.getJSONObject(i).put("joined", true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

        }

        return new SerializableJSONArray(json);
    }

    private SerializableJSONArray getNearByProjects(boolean useLocationServices) throws AuthenticationException {

        if (useLocationServices) {
            Location location;
            try {
                location = LocationServices.FusedLocationApi.getLastLocation(
                        mLocationClient);
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
                return new SerializableJSONArray();
            }

            return getNearByProjects(location);
        } else {
            // Use GPS alone to determine location
            LocationManager locationManager = (LocationManager)mApp.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);

            return getNearByProjects(location);
        }
    }

    private SerializableJSONArray getProjectDetails(int projectId) throws AuthenticationException {
        String inatNetwork = mApp.getInaturalistNetworkMember();
        String inatHost = mApp.getStringResourceByName("inat_host_" + inatNetwork);
        String url = "http://" + inatHost + "/projects/" + Integer.toString(projectId) + ".json";

        JSONArray json = get(url);

        if (json == null) {
            return new SerializableJSONArray();
        }

        return new SerializableJSONArray(json);
    }

    private SerializableJSONArray getFeaturedProjects() throws AuthenticationException {
        String inatNetwork = mApp.getInaturalistNetworkMember();
        String inatHost = mApp.getStringResourceByName("inat_host_" + inatNetwork);
        String url = "http://" + inatHost + "/projects.json?featured=true";

        JSONArray json = get(url);

        if (json == null) {
            return new SerializableJSONArray();
        }


        // Determine which projects are already joined
        for (int i = 0; i < json.length(); i++) {
            Cursor c;
            try {
                c = getContentResolver().query(Project.CONTENT_URI, Project.PROJECTION, "id = '"+json.getJSONObject(i).getInt("id")+"'", null, Project.DEFAULT_SORT_ORDER);
                c.moveToFirst();
                if (c.getCount() > 0) {
                    json.getJSONObject(i).put("joined", true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

        }

        return new SerializableJSONArray(json);
    }

    private void addProjectFields(JSONArray jsonFields) {
        int projectId = -1;
        ArrayList<ProjectField> projectFields = new ArrayList<ProjectField>();

        for (int i = 0; i < jsonFields.length(); i++) {
            try {
                BetterJSONObject jsonField = new BetterJSONObject(jsonFields.getJSONObject(i));
                ProjectField field = new ProjectField(jsonField);
                projectId = field.project_id;
                projectFields.add(field);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (projectId != -1) {
            // First, delete all previous project fields (for that project)
            getContentResolver().delete(ProjectField.CONTENT_URI, "(project_id IS NOT NULL) and (project_id = "+projectId+")", null);

            // Next, re-add all project fields
            for (int i = 0; i < projectFields.size(); i++) {
                ProjectField field = projectFields.get(i);
                getContentResolver().insert(ProjectField.CONTENT_URI, field.getContentValues());
            }
        }
    }

    public void joinProject(int projectId) throws AuthenticationException {
        post(String.format("%s/projects/%d/join.json", HOST, projectId), null);

        try {
            JSONArray result = get(String.format("%s/projects/%d.json", HOST, projectId));
            BetterJSONObject jsonProject = new BetterJSONObject(result.getJSONObject(0));
            Project project = new Project(jsonProject);

            // Add joined project locally
            ContentValues cv = project.getContentValues();
            getContentResolver().insert(Project.CONTENT_URI, cv);

            // Save project fields
            addProjectFields(jsonProject.getJSONArray("project_observation_fields").getJSONArray());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent reply = new Intent(ACTION_JOINED_PROJECTS_RESULT);
        reply.putExtra(PROJECTS_RESULT, projectId);
        sendBroadcast(reply);
    }

    public void leaveProject(int projectId) throws AuthenticationException {
        delete(String.format("%s/projects/%d/leave.json", HOST, projectId), null);

        // Remove locally saved project (because we left it)
        getContentResolver().delete(Project.CONTENT_URI, "(id IS NOT NULL) and (id = "+projectId+")", null);
    }


    private BetterJSONObject removeObservationFromProject(int observationId, int projectId) throws AuthenticationException {
        if (ensureCredentials() == false) {
            return null;
        }

        String url = String.format("%s/projects/%d/remove.json?observation_id=%d", HOST, projectId, observationId);
        JSONArray json = delete(url, null);

        try {
            return new BetterJSONObject(json.getJSONObject(0));
        } catch (JSONException e) {
            e.printStackTrace();
            return new BetterJSONObject();
        }
    }


    private BetterJSONObject addObservationToProject(int observationId, int projectId) throws AuthenticationException {
        if (ensureCredentials() == false) {
            return null;
        }

        String url = HOST + "/project_observations.json";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("project_observation[observation_id]", String.valueOf(observationId)));
        params.add(new BasicNameValuePair("project_observation[project_id]", String.valueOf(projectId)));
        JSONArray json = post(url, params);

        if (json == null) {
            return new BetterJSONObject();
        }

        try {
            return new BetterJSONObject(json.getJSONObject(0));
        } catch (JSONException e) {
            e.printStackTrace();
            return new BetterJSONObject();
        }
    }


    private SerializableJSONArray getCheckList(int id) throws AuthenticationException {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Locale deviceLocale = getResources().getConfiguration().locale;
        String deviceLanguage =   deviceLocale.getLanguage();

        String url = String.format("%s/lists/%d.json?per_page=50&locale=%s", HOST, id, deviceLanguage);

        JSONArray json = get(url);

        if (json == null) {
            return null;
        }

        try {
            return new SerializableJSONArray(json.getJSONObject(0).getJSONArray("listed_taxa"));
        } catch (JSONException e) {
            e.printStackTrace();
            return new SerializableJSONArray();
        }
    }


    private SerializableJSONArray getJoinedProjectsOffline() {
        JSONArray projects = new JSONArray();

        Cursor c = getContentResolver().query(Project.CONTENT_URI, Project.PROJECTION, null, null, Project.DEFAULT_SORT_ORDER);

        c.moveToFirst();
        int index = 0;

        while (c.isAfterLast() == false) {
            Project project = new Project(c);
            JSONObject jsonProject = project.toJSONObject();
            try {
                jsonProject.put("joined", true);
                projects.put(index, jsonProject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            c.moveToNext();
            index++;
        }
        c.close();

        return new SerializableJSONArray(projects);
    }

    private SerializableJSONArray getJoinedProjects() throws AuthenticationException {
        if (ensureCredentials() == false) {
            return null;
        }
        String url = HOST + "/projects/user/" + Uri.encode(mLogin) + ".json";

        JSONArray json = get(url, true);
        JSONArray finalJson = new JSONArray();

        if (json == null) {
            return null;
        }

        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject obj = json.getJSONObject(i);
                JSONObject project = obj.getJSONObject("project");
                project.put("joined", true);
                finalJson.put(project);

                // Save project fields
                addProjectFields(project.getJSONArray("project_observation_fields"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return new SerializableJSONArray(finalJson);
    }


    @SuppressLint("NewApi")
    private void getUserObservations(int maxCount) throws AuthenticationException {
        if (ensureCredentials() == false) {
            return;
        }
        String url = HOST + "/observations/" + Uri.encode(mLogin) + ".json";

        long lastSync = mPreferences.getLong("last_sync_time", 0);
        Timestamp lastSyncTS = new Timestamp(lastSync);
        url += String.format("?updated_since=%s&order_by=date_added&order=desc&extra=observation_photos,projects,fields", URLEncoder.encode(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(lastSyncTS)));

        if (maxCount > 0) {
            // Retrieve only a certain number of observations
            url += String.format("&per_page=%d&page=1", maxCount);
        }

        mProjectObservations = new ArrayList<SerializableJSONArray>();
        mProjectFieldValues = new Hashtable<Integer, Hashtable<Integer,ProjectFieldValue>>();

        JSONArray json = get(url);
        if (json != null && json.length() > 0) {
            syncJson(json, true);
        } else {
            if (mResponseHeaders != null) {
                // Delete any local observations which were deleted remotely by the user
                for (Header header : mResponseHeaders) {
                    if (!header.getName().equalsIgnoreCase("X-Deleted-Observations")) continue;

                    String deletedIds = header.getValue().trim();
                    getContentResolver().delete(Observation.CONTENT_URI, "(id IN ("+deletedIds+"))", null);
                    // Delete associated project-fields and photos
                    int count1 = getContentResolver().delete(ObservationPhoto.CONTENT_URI, "observation_id in (" + deletedIds + ")", null);
                    int count2 = getContentResolver().delete(ProjectObservation.CONTENT_URI, "observation_id in (" + deletedIds + ")", null);
                    int count3 = getContentResolver().delete(ProjectFieldValue.CONTENT_URI, "observation_id in (" + deletedIds + ")", null);
                    break;
                }

                mResponseHeaders = null;
            }
        }
    }

    private void syncObservationFields() throws AuthenticationException {

        // First, remotely update the observation fields which were modified

        Cursor c = getContentResolver().query(ProjectFieldValue.CONTENT_URI,
                ProjectFieldValue.PROJECTION,
                "_updated_at > _synced_at AND _synced_at IS NOT NULL",
                null,
                ProjectFieldValue.DEFAULT_SORT_ORDER);

        c.moveToFirst();
        while (c.isAfterLast() == false) {
            ProjectFieldValue localField = new ProjectFieldValue(c);

            if (!mProjectFieldValues.containsKey(Integer.valueOf(localField.observation_id))) {
                // Need to retrieve remote observation fields to see how to sync the fields
                JSONArray jsonResult = get(HOST + "/observations/" + localField.observation_id + ".json");

                if (jsonResult != null) {
                    Hashtable<Integer, ProjectFieldValue> fields = new Hashtable<Integer, ProjectFieldValue>();

                    try {
                        JSONArray jsonFields = jsonResult.getJSONObject(0).getJSONArray("observation_field_values");

                        for (int j = 0; j < jsonFields.length(); j++) {
                            JSONObject jsonField = jsonFields.getJSONObject(j);
                            JSONObject observationField = jsonField.getJSONObject("observation_field");
                            int id = observationField.optInt("id", jsonField.getInt("observation_field_id"));
                            fields.put(id, new ProjectFieldValue(new BetterJSONObject(jsonField)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mProjectFieldValues.put(localField.observation_id, fields);
                }
            }

            Hashtable<Integer, ProjectFieldValue> fields = mProjectFieldValues.get(Integer.valueOf(localField.observation_id));

            boolean shouldOverwriteRemote = false;
            ProjectFieldValue remoteField = null;

            if (fields == null) {
                continue;
            }

            if (!fields.containsKey(Integer.valueOf(localField.field_id))) {
                // No remote field - add it
                shouldOverwriteRemote = true;
            } else {
                remoteField = fields.get(Integer.valueOf(localField.field_id));

                if (remoteField.updated_at.before(localField._updated_at)) {
                    shouldOverwriteRemote = true;
                }
            }

            if (shouldOverwriteRemote) {
                // Overwrite remote value
                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("observation_field_value[observation_id]", Integer.valueOf(localField.observation_id).toString()));
                params.add(new BasicNameValuePair("observation_field_value[observation_field_id]", Integer.valueOf(localField.field_id).toString()));
                params.add(new BasicNameValuePair("observation_field_value[value]", localField.value));
                post(HOST + "/observation_field_values.json", params);

            } else {
                // Overwrite local value
                localField.created_at = remoteField.created_at;
                localField.id = remoteField.id;
                localField.observation_id = remoteField.observation_id;
                localField.field_id = remoteField.field_id;
                localField.value = remoteField.value;
                localField.updated_at = remoteField.updated_at;
            }

            ContentValues cv = localField.getContentValues();
            cv.put(ProjectFieldValue._SYNCED_AT, System.currentTimeMillis());
            getContentResolver().update(localField.getUri(), cv, null, null);

            fields.remove(Integer.valueOf(localField.field_id));

            c.moveToNext();
        }
        c.close();

        // Next, add any new observation fields
        for (Hashtable<Integer, ProjectFieldValue> fields : mProjectFieldValues.values()) {
            for (ProjectFieldValue field : fields.values()) {
                ContentValues cv = field.getContentValues();
                cv.put(ProjectFieldValue._SYNCED_AT, System.currentTimeMillis());
                getContentResolver().insert(ProjectFieldValue.CONTENT_URI, cv);

                c = getContentResolver().query(ProjectField.CONTENT_URI, ProjectField.PROJECTION,
                        "field_id = " + field.field_id, null, Project.DEFAULT_SORT_ORDER);
                if (c.getCount() == 0) {
                    // This observation has a non-project custom field - add it as well
                    addProjectField(field.field_id);
                }
                c.close();

            }
        }

    }

    private void addProjectField(int fieldId) throws AuthenticationException {
        try {
            JSONArray result = get(String.format("%s/observation_fields/%d.json", HOST, fieldId));
            BetterJSONObject jsonObj;
            jsonObj = new BetterJSONObject(result.getJSONObject(0));
            ProjectField field = new ProjectField(jsonObj);

            getContentResolver().insert(ProjectField.CONTENT_URI, field.getContentValues());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getNearbyObservations(Intent intent) throws AuthenticationException {
        Bundle extras = intent.getExtras();
        Double minx = extras.getDouble("minx");
        Double maxx = extras.getDouble("maxx");
        Double miny = extras.getDouble("miny");
        Double maxy = extras.getDouble("maxy");
        Boolean clearMapLimit = extras.getBoolean("clear_map_limit", false);
        Integer page = extras.getInt("page", 0);

        String url = HOST;
        if (extras.containsKey("username")) {
            url = HOST + "/observations/" + extras.getString("username") + ".json?extra=observation_photos";
        } else {
            url = HOST + "/observations.json?extra=observation_photos";
        }

        url += "&page=" + page + "&per_page=" + NEAR_BY_OBSERVATIONS_PER_PAGE;

        if (extras.containsKey("taxon_id")) {
            url += "&taxon_id=" + extras.getInt("taxon_id");
        }
        if (extras.containsKey("location_id")) {
            url += "&place_id=" + extras.getInt("location_id");
        } else if (!clearMapLimit) {
            url += "&swlat="+miny;
            url += "&nelat="+maxy;
            url += "&swlng="+minx;
            url += "&nelng="+maxx;
        }

        if (extras.containsKey("project_id")) {
            url += "&projects[]=" + extras.getInt("project_id");
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Locale deviceLocale = getResources().getConfiguration().locale;
        String deviceLanguage =   deviceLocale.getLanguage();
        url += "&locale=" + deviceLanguage;


        Log.d(TAG, "Near by observations URL: " + url);

        mNearByObservationsUrl = url;

        JSONArray json = get(url, mApp.loggedIn());
        Intent reply = new Intent(ACTION_NEARBY);
        reply.putExtra("minx", minx);
        reply.putExtra("maxx", maxx);
        reply.putExtra("miny", miny);
        reply.putExtra("maxy", maxy);
        if (json == null) {
            reply.putExtra("error", getString(R.string.couldnt_load_nearby_observations));
        } else {
            //syncJson(json, false);
        }

        if (!mIsStopped && url.equalsIgnoreCase(mNearByObservationsUrl)) {
            // Only send the reply if a new near by observations request hasn't been made yet
            mApp.setServiceResult(ACTION_NEARBY, new SerializableJSONArray(json));
            sendBroadcast(reply);
        }
    }

    private JSONArray put(String url, ArrayList<NameValuePair> params) throws AuthenticationException {
        params.add(new BasicNameValuePair("_method", "PUT"));
        return request(url, "put", params, true);
    }

    private JSONArray delete(String url, ArrayList<NameValuePair> params) throws AuthenticationException {
        return request(url, "delete", params, true);
    }

    private JSONArray post(String url, ArrayList<NameValuePair> params, boolean authenticated) throws AuthenticationException {
        return request(url, "post", params, authenticated);
    }

    private JSONArray post(String url, ArrayList<NameValuePair> params) throws AuthenticationException {
        return request(url, "post", params, true);
    }

    private JSONArray get(String url) throws AuthenticationException {
        return get(url, false);
    }

    private JSONArray get(String url, boolean authenticated) throws AuthenticationException {
        return request(url, "get", null, authenticated);
    }

    private JSONArray request(String url, String method, ArrayList<NameValuePair> params, boolean authenticated) throws AuthenticationException {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);

//        Log.d(TAG, String.format("%s (%b - %s): %s", method, authenticated,
//                authenticated ? mCredentials : "<null>",
//                url));

        HttpRequestBase request;

        Log.d(TAG, String.format("URL: %s - %s (%s)", method, url, (params != null ? params.toString() : "null")));

        if (method.equalsIgnoreCase("post")) {
            request = new HttpPost(url);
        } else if (method.equalsIgnoreCase("delete")) {
            request = new HttpDelete(url);
        } else if (method.equalsIgnoreCase("put")) {
            request = new HttpPut(url);
        } else {
            request = new HttpGet(url);
        }

        // POST params
        if (params != null) {
            Charset utf8Charset = Charset.forName("UTF-8");
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i).getName().equalsIgnoreCase("image") || params.get(i).getName().equalsIgnoreCase("file")) {
                    // If the key equals to "image", we use FileBody to transfer the data

                    // Resize image to max size of 2048x2048 before sending it to the server
                    String filename = resizeImage(params.get(i).getValue());

                    entity.addPart(params.get(i).getName(), new FileBody(new File (filename)));
                } else {
                    // Normal string data
                    try {
                        entity.addPart(params.get(i).getName(), new StringBody(params.get(i).getValue(), utf8Charset));
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "failed to add " + params.get(i).getName() + " to entity for a " + method + " request: " + e);
                    }
                }
            }
            if (method.equalsIgnoreCase("put")) {
                ((HttpPut) request).setEntity(entity);
            } else {
                ((HttpPost) request).setEntity(entity);
            }
        }

        // auth
        if (authenticated) {
            ensureCredentials();

            if (mLoginType == LoginType.PASSWORD) {
                request.setHeader("Authorization", "Basic " + mCredentials);
            } else {
                request.setHeader("Authorization", "Bearer " + mCredentials);
            }
        }

        try {
            /*
            com.google.api.client.http.HttpResponse response = request.execute();
            String content = response.parseAsString();
            */
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);

            Log.d(TAG, String.format("RESP: %s", content));

            JSONArray json = null;
            switch (response.getStatusLine().getStatusCode()) {
                //switch (response.getStatusCode()) {
                case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                    // Validation error - still need to return response
                    Log.e(TAG, response.getStatusLine().toString());
                case HttpStatus.SC_OK:
                    try {
                        json = new JSONArray(content);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to create JSONArray, JSONException: " + e.toString());
                        try {
                            JSONObject jo = new JSONObject(content);
                            json = new JSONArray();
                            json.put(jo);
                        } catch (JSONException e2) {
                            Log.e(TAG, "Failed to create JSONObject, JSONException: " + e2.toString());
                        }
                    }

                    mResponseHeaders = response.getAllHeaders();

                    try {
                        if (json != null && json.length() > 0) {
                            JSONObject result = json.getJSONObject(0);
                            if (result.has("errors")) {
                                // Error response
                                Log.e(TAG, "Got an error response: " + result.get("errors").toString());
                                mResponseErrors = result.getJSONArray("errors");
                                return null;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mResponseErrors = null;


                    return json;

                case HttpStatus.SC_UNAUTHORIZED:
                    throw new AuthenticationException();
                case HttpStatus.SC_GONE:
                    Log.e(TAG, "GONE: " + response.getStatusLine().toString());
                    // TODO create notification that informs user some observations have been deleted on the server,
                    // click should take them to an activity that lets them decide whether to delete them locally
                    // or post them as new observations
                default:
                    Log.e(TAG, response.getStatusLine().toString());
                    //Log.e(TAG, response.getStatusMessage());
            }
        }
        catch (IOException e) {
            //request.abort();
            Log.w(TAG, "Error for URL " + url, e);
        }
        return null;
    }


    // EXIF-copying code taken from: https://bricolsoftconsulting.com/copying-exif-metadata-using-sanselan/
    public static boolean copyExifData(File sourceFile, File destFile, List<TagInfo> excludedFields) {
        String tempFileName = destFile.getAbsolutePath() + ".tmp";
        File tempFile = null;
        OutputStream tempStream = null;

        try {
            tempFile = new File (tempFileName);

            TiffOutputSet sourceSet = getSanselanOutputSet(sourceFile, TiffConstants.DEFAULT_TIFF_BYTE_ORDER);
            TiffOutputSet destSet = getSanselanOutputSet(destFile, sourceSet.byteOrder);

            // If the EXIF data endianess of the source and destination files
            // differ then fail. This only happens if the source and
            // destination images were created on different devices. It's
            // technically possible to copy this data by changing the byte
            // order of the data, but handling this case is outside the scope
            // of this implementation
            if (sourceSet.byteOrder != destSet.byteOrder) return false;

            destSet.getOrCreateExifDirectory();

            // Go through the source directories
            List<?> sourceDirectories = sourceSet.getDirectories();
            for (int i=0; i<sourceDirectories.size(); i++) {
                TiffOutputDirectory sourceDirectory = (TiffOutputDirectory)sourceDirectories.get(i);
                TiffOutputDirectory destinationDirectory = getOrCreateExifDirectory(destSet, sourceDirectory);

                if (destinationDirectory == null) continue; // failed to create

                // Loop the fields
                List<?> sourceFields = sourceDirectory.getFields();
                for (int j=0; j<sourceFields.size(); j++) {
                    // Get the source field
                    TiffOutputField sourceField = (TiffOutputField) sourceFields.get(j);

                    // Check exclusion list
                    if (excludedFields != null && excludedFields.contains(sourceField.tagInfo)) {
                        destinationDirectory.removeField(sourceField.tagInfo);
                        continue;
                    }

                    // Remove any existing field
                    destinationDirectory.removeField(sourceField.tagInfo);

                    // Add field
                    destinationDirectory.add(sourceField);
                }
            }

            // Save data to destination
            tempStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            new ExifRewriter().updateExifMetadataLossless(destFile, tempStream, destSet);
            tempStream.close();

            // Replace file
            if (destFile.delete()) {
                tempFile.renameTo(destFile);
            }

            return true;

        } catch (ImageReadException exception) {
            exception.printStackTrace();

        } catch (ImageWriteException exception) {
            exception.printStackTrace();

        } catch (IOException exception) {
            exception.printStackTrace();

        } finally {
            if (tempStream != null) {
                try {
                    tempStream.close();
                } catch (IOException e) {
                }
            }

            if (tempFile != null) {
                if (tempFile.exists()) tempFile.delete();
            }
        }

        return false;
    }

    private static TiffOutputSet getSanselanOutputSet(File jpegImageFile, int defaultByteOrder)
            throws IOException, ImageReadException, ImageWriteException {
        TiffImageMetadata exif = null;
        TiffOutputSet outputSet = null;

        IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (jpegMetadata != null) {
            exif = jpegMetadata.getExif();

            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }

        // If JPEG file contains no EXIF metadata, create an empty set
        // of EXIF metadata. Otherwise, use existing EXIF metadata to
        // keep all other existing tags
        if (outputSet == null)
            outputSet = new TiffOutputSet(exif==null?defaultByteOrder:exif.contents.header.byteOrder);

        return outputSet;
    }

    private static TiffOutputDirectory getOrCreateExifDirectory(TiffOutputSet outputSet, TiffOutputDirectory outputDirectory) {
        TiffOutputDirectory result = outputSet.findDirectory(outputDirectory.type);
        if (result != null)
            return result;
        result = new TiffOutputDirectory(outputDirectory.type);
        try {
            outputSet.addDirectory(result);
        } catch (ImageWriteException e) {
            return null;
        }
        return result;
    }

    /**
     * Resizes an image to max size of 2048x2048
     * @param filename the image filename
     * @return the reized image - or original image if smaller than 2048x2048
     */
    private String resizeImage(String filename) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        InputStream is = null;
        try {
            is = new FileInputStream(filename);
            Bitmap bitmap = BitmapFactory.decodeStream(is,null,options);
            is.close();
            int originalHeight = options.outHeight;
            int originalWidth = options.outWidth;
            int newHeight, newWidth;


            if (Math.max(originalHeight, originalWidth) < 2048) {
                // Original file is smaller than 2048x2048 - no need to resize
                return filename;
            }

            // Resize but make sure we have the same width/height aspect ratio
            if (originalHeight > originalWidth) {
                newHeight = 2048;
                newWidth = (int)(2048 * ((float)originalWidth / originalHeight));
            } else {
                newWidth = 2048;
                newHeight = (int)(2048 * ((float)originalHeight / originalWidth));
            }

            Log.d(TAG, "Bitmap h:" + options.outHeight + "; w:" + options.outWidth);
            Log.d(TAG, "Resized Bitmap h:" + newHeight + "; w:" + newWidth);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            // Save resized image
            File imageFile = new File(getExternalCacheDir(), UUID.randomUUID().toString() + ".jpeg");
            OutputStream os = new FileOutputStream(imageFile);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            // Copy all EXIF data from original image into resized image
            copyExifData(new File(filename), new File(imageFile.getAbsolutePath()), null);

            return imageFile.getAbsolutePath();

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filename;
    }

    private boolean ensureCredentials() throws AuthenticationException {
        if (mCredentials != null) { return true; }

        // request login unless passive
        if (!mPassive) {
            throw new AuthenticationException();
        }
        stopSelf();
        return false;
    }

    private void requestCredentials() {
        stopSelf();
        Intent intent = new Intent(
                mLogin == null ? "signin" : INaturalistPrefsActivity.REAUTHENTICATE_ACTION,
                null, getBaseContext(), INaturalistPrefsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mApp.sweepingNotify(AUTH_NOTIFICATION, getString(R.string.please_sign_in), getString(R.string.please_sign_in_description), null, intent);
    }


    public static String verifyCredentials(String credentials) {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
        String url = HOST + "/observations/new.json";
        HttpRequestBase request = new HttpGet(url);
        request.setHeader("Authorization", "Basic "+credentials);
        request.setHeader("Content-Type", "application/json");

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // Next, find the iNat username (since we currently only have email address)
                request = new HttpGet(HOST + "/users/edit.json");
                request.setHeader("Authorization", "Basic "+credentials);

                response = client.execute(request);
                entity = response.getEntity();
                content = EntityUtils.toString(entity);

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    return null;
                }

                JSONObject json = new JSONObject(content);
                if (!json.has("login")) {
                    return null;
                }

                String username = json.getString("login");

                return username;
            } else {
                Log.e(TAG, "Authentication failed: " + content);
                return null;
            }
        }
        catch (IOException e) {
            request.abort();
            Log.w(TAG, "Error for URL " + url, e);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String verifyCredentials(String username, String password) {
        String credentials = Base64.encodeToString(
                (username + ":" + password).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP
        );
        return verifyCredentials(credentials);
    }

    // Returns an array of two strings: access token + iNat username
    public static String[] verifyCredentials(String oauth2Token, LoginType authType) {
        String grantType;
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
        String url = HOST + "/oauth/assertion_token";
        HttpRequestBase request = new HttpPost(url);
        ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();

        postParams.add(new BasicNameValuePair("format", "json"));
        postParams.add(new BasicNameValuePair("client_id", INaturalistApp.getAppContext().getString(R.string.oauth_client_id)));
        if (authType == LoginType.FACEBOOK) {
            grantType = "facebook";
        } else {
            grantType = "google";
        }

        postParams.add(new BasicNameValuePair("grant_type", grantType));
        postParams.add(new BasicNameValuePair("assertion", oauth2Token));

        try {
            ((HttpPost)request).setEntity(new UrlEncodedFormEntity(postParams));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // Upgrade to an access token
//                Log.d(TAG, "Authorization Response: " + content);
                JSONObject json = new JSONObject(content);
                String accessToken = json.getString("access_token");

                // Next, find the iNat username (since we currently only have the FB/Google email)
                request = new HttpGet(HOST + "/users/edit.json");
                request.setHeader("Authorization", "Bearer " + accessToken);

                response = client.execute(request);
                entity = response.getEntity();
                content = EntityUtils.toString(entity);

                Log.d(TAG, String.format("RESP2: %s", content));

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    return null;
                }

                json = new JSONObject(content);
                if (!json.has("login")) {
                    return null;
                }

                String username = json.getString("login");

                return new String[] { accessToken, username };

            } else {
                Log.e(TAG, "Authentication failed: " + content);
                return null;
            }
        }
        catch (IOException e) {
            request.abort();
            Log.w(TAG, "Error for URL " + url, e);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }


    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(Observation observation){
        ContentValues values = new ContentValues();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name = "observation_" + observation.created_at.getTime() + "_" + timeStamp;
        values.put(android.provider.MediaStore.Images.Media.TITLE, name);
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    private Uri createObservationPhotoForPhoto(Uri photoUri, Observation observation) {
        ObservationPhoto op = new ObservationPhoto();
        Long photoId = ContentUris.parseId(photoUri);
        ContentValues cv = op.getContentValues();
        cv.put(ObservationPhoto._OBSERVATION_ID, observation._id);
        cv.put(ObservationPhoto.OBSERVATION_ID, observation.id);
        cv.put(ObservationPhoto._SYNCED_AT, System.currentTimeMillis()); // So we won't re-add this photo as though it was a local photo
        if (photoId > -1) {
            cv.put(ObservationPhoto._PHOTO_ID, photoId.intValue());
        }
        return getContentResolver().insert(ObservationPhoto.CONTENT_URI, cv);
    }


    public void syncJson(JSONArray json, boolean isUser) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ArrayList<Integer> existingIds = new ArrayList<Integer>();
        ArrayList<Integer> newIds = new ArrayList<Integer>();
        HashMap<Integer,Observation> jsonObservationsById = new HashMap<Integer,Observation>();
        Observation observation;
        Observation jsonObservation;

        BetterJSONObject o;
        for (int i = 0; i < json.length(); i++) {
            try {
                o = new BetterJSONObject(json.getJSONObject(i));
                ids.add(o.getInt("id"));
                jsonObservationsById.put(o.getInt("id"), new Observation(o));

                if (isUser) {
                    // Save the project observations aside (will be later used in the syncing of project observations)
                    mProjectObservations.add(o.getJSONArray("project_observations"));

                    // Save project field values
                    Hashtable<Integer, ProjectFieldValue> fields = new Hashtable<Integer, ProjectFieldValue>();
                    JSONArray jsonFields = o.getJSONArray("observation_field_values").getJSONArray();

                    for (int j = 0; j < jsonFields.length(); j++) {
                        BetterJSONObject field = new BetterJSONObject(jsonFields.getJSONObject(j));
                        fields.put(field.getJSONObject("observation_field").getInt("id"), new ProjectFieldValue(field));
                    }

                    mProjectFieldValues.put(o.getInt("id"), fields);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException: " + e.toString());
            }
        }
        // find obs with existing ids
        String joinedIds = StringUtils.join(ids, ",");
        // TODO why doesn't selectionArgs work for id IN (?)
        Cursor c = getContentResolver().query(Observation.CONTENT_URI,
                Observation.PROJECTION,
                "id IN ("+joinedIds+")", null, Observation.DEFAULT_SORT_ORDER);

        // update existing
        c.moveToFirst();
        ContentValues cv;
        while (c.isAfterLast() == false) {
            observation = new Observation(c);
            jsonObservation = jsonObservationsById.get(observation.id);
            boolean isModified = observation.merge(jsonObservation);
            cv = observation.getContentValues();
            if (observation._updated_at.before(jsonObservation.updated_at)) {
                // Remote observation is newer (and thus has overwritten the local one) - update its
                // sync at time so we won't update the remote servers later on (since we won't
                // accidently consider this an updated record)
                cv.put(Observation._SYNCED_AT, System.currentTimeMillis());
            }

            // Add any new photos that were added remotely
            ArrayList<Integer> observationPhotoIds = new ArrayList<Integer>();
            ArrayList<Integer> existingObservationPhotoIds = new ArrayList<Integer>();
            Cursor pc = getContentResolver().query(
                    ObservationPhoto.CONTENT_URI,
                    ObservationPhoto.PROJECTION,
                    "observation_id = "+observation.id,
                    null, null);
            pc.moveToFirst();
            while(pc.isAfterLast() == false) {
                int photoId = pc.getInt(pc.getColumnIndexOrThrow(ObservationPhoto.ID));
                if (photoId != 0) {
                    existingObservationPhotoIds.add(photoId);
                }
                pc.moveToNext();
            }
            pc.close();
            for (int j = 0; j < jsonObservation.photos.size(); j++) {
                ObservationPhoto photo = jsonObservation.photos.get(j);
                photo._observation_id = jsonObservation._id;
                observationPhotoIds.add(photo.id);
                if (existingObservationPhotoIds.contains(photo.id)) {
                    Log.d(TAG, "photo " + photo.id + " has already been added, skipping...");
                    continue;
                }
                ContentValues opcv = photo.getContentValues();
                // So we won't re-add this photo as though it was a local photo
                opcv.put(ObservationPhoto._SYNCED_AT, System.currentTimeMillis());
                opcv.put(ObservationPhoto._OBSERVATION_ID, photo.observation_id);
                opcv.put(ObservationPhoto._PHOTO_ID, photo._photo_id);
                opcv.put(ObservationPhoto.ID, photo.id);
                try {
                    getContentResolver().insert(ObservationPhoto.CONTENT_URI, opcv);
                } catch(SQLException ex) {
                    // Happens when the photo already exists - ignore
                }
            }

            // Delete photos that were synced but weren't present in the remote response, 
            // indicating they were deleted elsewhere
            String joinedPhotoIds = StringUtils.join(observationPhotoIds, ",");
            String where = "observation_id = " + observation.id + " AND id IS NOT NULL";
            if (joinedPhotoIds.length() > 0) {
                where += " AND id NOT in (" + joinedPhotoIds + ")";
            }
            int deleteCount = getContentResolver().delete(
                    ObservationPhoto.CONTENT_URI,
                    where,
                    null);

            if (deleteCount > 0) {
                Crashlytics.log(1, TAG, String.format("Warning: Deleted %d photos locally after sever did not contain those IDs - observation id: %s, photo ids: %s",
                        deleteCount, observation.id, joinedPhotoIds));
            }

            if (isModified) {
                // Only update the DB if needed
                getContentResolver().update(observation.getUri(), cv, null, null);
            }
            existingIds.add(observation.id);
            c.moveToNext();
        }
        c.close();

        // insert new
        List<Observation> newObservations = new ArrayList<Observation>();
        newIds = (ArrayList<Integer>) CollectionUtils.subtract(ids, existingIds);
        Collections.sort(newIds);
        for (int i = 0; i < newIds.size(); i++) {
            jsonObservation = jsonObservationsById.get(newIds.get(i));
            cv = jsonObservation.getContentValues();
            cv.put(Observation._SYNCED_AT, System.currentTimeMillis());
            cv.put(Observation.LAST_COMMENTS_COUNT, jsonObservation.comments_count);
            cv.put(Observation.LAST_IDENTIFICATIONS_COUNT, jsonObservation.identifications_count);
            Uri newObs = getContentResolver().insert(Observation.CONTENT_URI, cv);
            Long newObsId = ContentUris.parseId(newObs);
            jsonObservation._id = Integer.valueOf(newObsId.toString());
            newObservations.add(jsonObservation);
        }

        if (isUser) {
            for (int i = 0; i < newObservations.size(); i++) {
                jsonObservation = newObservations.get(i);

                // Save the new observation's photos
                for (int j = 0; j < jsonObservation.photos.size(); j++) {
                    ObservationPhoto photo = jsonObservation.photos.get(j);
                    photo._observation_id = jsonObservation._id;

                    ContentValues opcv = photo.getContentValues();
                    opcv.put(ObservationPhoto._SYNCED_AT, System.currentTimeMillis()); // So we won't re-add this photo as though it was a local photo
                    opcv.put(ObservationPhoto._OBSERVATION_ID, photo._observation_id);
                    opcv.put(ObservationPhoto._PHOTO_ID, photo._photo_id);
                    opcv.put(ObservationPhoto._ID, photo.id);
                    getContentResolver().insert(ObservationPhoto.CONTENT_URI, opcv);
                }
            }
        }


        if (isUser) {
            if (mResponseHeaders != null) {
                // Delete any local observations which were deleted remotely by the user
                for (Header header : mResponseHeaders) {
                    if (!header.getName().equalsIgnoreCase("X-Deleted-Observations")) continue;

                    String deletedIds = header.getValue().trim();
                    getContentResolver().delete(Observation.CONTENT_URI, "(id IN ("+deletedIds+"))", null);
                    // Delete associated project-fields and photos
                    int count1 = getContentResolver().delete(ObservationPhoto.CONTENT_URI, "observation_id in (" + deletedIds + ")", null);
                    int count2 = getContentResolver().delete(ProjectObservation.CONTENT_URI, "observation_id in (" + deletedIds + ")", null);
                    int count3 = getContentResolver().delete(ProjectFieldValue.CONTENT_URI, "observation_id in (" + deletedIds + ")", null);

                    break;
                }

                mResponseHeaders = null;
            }
        }
    }

    private ArrayList<NameValuePair> paramsForObservation(Observation observation, boolean isPOST) {
        ArrayList<NameValuePair> params = observation.getParams();
        params.add(new BasicNameValuePair("ignore_photos", "true"));

        if (isPOST) {
            String inatNetwork = mApp.getInaturalistNetworkMember();
            params.add(new BasicNameValuePair("site_id", mApp.getStringResourceByName("inat_site_id_" + inatNetwork)));
        }

        return params;
    }

    private void handleObservationResponse(Observation observation, JSONArray response) {
        try {
            if (response == null || response.length() != 1) {
                return;
            }
            JSONObject json = response.getJSONObject(0);
            BetterJSONObject o = new BetterJSONObject(json);
            Observation jsonObservation = new Observation(o);
            observation.merge(jsonObservation);
            ContentValues cv = observation.getContentValues();
            cv.put(Observation._SYNCED_AT, System.currentTimeMillis());
            getContentResolver().update(observation.getUri(), cv, null, null);
        } catch (JSONException e) {
            // Log.d(TAG, "JSONException: " + e.toString());
        }
    }

    private class AuthenticationException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Log.e(TAG, "onConnectionFailed: " + (arg0 != null ? arg0.toString() : "null"));

        // Try using the GPS for the location
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SerializableJSONArray projects;
                try {
                    if (mGetLocationForProjects) {
                        projects = getNearByProjects(false);
                    } else {
                        projects = getNearByGuides(false);
                    }
                } catch (AuthenticationException e) {
                    projects = new SerializableJSONArray();
                    e.printStackTrace();
                }

                Intent reply = new Intent(mGetLocationForProjects ? ACTION_NEARBY_PROJECTS_RESULT: ACTION_NEAR_BY_GUIDES_RESULT);
                reply.putExtra(mGetLocationForProjects ? PROJECTS_RESULT : GUIDES_RESULT, projects);
                sendBroadcast(reply);
            }
        });
        thread.start();
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.i(TAG, "onConnected: " + (arg0 != null ? arg0.toString() : "null"));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SerializableJSONArray projects;
                try {
                    if (mGetLocationForProjects) {
                        projects = getNearByProjects(true);
                    } else {
                        projects = getNearByGuides(true);
                    }
                } catch (AuthenticationException e) {
                    projects = new SerializableJSONArray();
                    e.printStackTrace();
                }

                Intent reply = new Intent(mGetLocationForProjects ? ACTION_NEARBY_PROJECTS_RESULT : ACTION_NEAR_BY_GUIDES_RESULT);
                reply.putExtra(mGetLocationForProjects ? PROJECTS_RESULT : GUIDES_RESULT, projects);
                sendBroadcast(reply);
            }
        });
        thread.start();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onDisconnected");
    }

  /*  @Override
    public void onDisconnected() {
        Log.e(TAG, "onDisconnected");
    }*/

    @Override
    public void onDestroy() {
        mIsStopped = true;
        super.onDestroy();
    }
}
