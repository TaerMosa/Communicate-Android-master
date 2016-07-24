package org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.HttpGet;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import com.quickblox.users.model.QBUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ApplicationSessionStateCallback;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class BaseActivity extends Activity implements ApplicationSessionStateCallback {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final String USER_LOGIN_KEY = "USER_LOGIN_KEY";
    private static final String USER_PASSWORD_KEY = "USER_PASSWORD_KEY";
    private OnResumeOnPause asyncResPau ;

    private boolean sessionActive = false;
    private boolean needToRecreateSession = false;

    private ProgressDialog progressDialog;
    private final Handler handler = new Handler();

    public boolean isSessionActive() {
        return sessionActive;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 'initialised' will be true if it's the 1st start of the app or if the app's process was killed by OS(or user)
        //
        if(savedInstanceState != null){
            needToRecreateSession = true;
        }else{
            sessionActive = true;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(needToRecreateSession){
            needToRecreateSession = false;

            Log.d(TAG, "Need to restore chat connection");

            QBUser user = new QBUser();
            user.setLogin(savedInstanceState.getString(USER_LOGIN_KEY));
            user.setPassword(savedInstanceState.getString(USER_PASSWORD_KEY));

            savedInstanceState.remove(USER_LOGIN_KEY);
            savedInstanceState.remove(USER_PASSWORD_KEY);

            recreateSession(user);
        }
    }

    private void recreateSession(final QBUser user){
        sessionActive = false;
        this.onStartSessionRecreation();

        showProgressDialog();

        // Restoring Chat session
        //
        ChatService.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.d(TAG, "Chat login onSuccess");

                progressDialog.dismiss();
                progressDialog = null;

                sessionActive = true;
                BaseActivity.this.onFinishSessionRecreation(true);
                asyncResPau = new OnResumeOnPause(BaseActivity.this);
                asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");

            }

            @Override
            public void onError(QBResponseException errors) {

                Log.d(TAG, "Chat login onError: " + errors);

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error in the recreate session request, trying again in 3 seconds.. Check you internet connection.", Toast.LENGTH_SHORT);
                toast.show();

                // try again
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreateSession(user);
                    }
                }, 3000);

                BaseActivity.this.onFinishSessionRecreation(false);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        QBUser currentUser = ChatService.getInstance().getCurrentUser();
        if(currentUser != null) {
            outState.putString(USER_LOGIN_KEY, currentUser.getLogin());
            outState.putString(USER_PASSWORD_KEY, currentUser.getPassword());
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    private void showProgressDialog(){
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(BaseActivity.this);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Restoring chat session...");
            progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        }
        progressDialog.show();
        asyncResPau = new OnResumeOnPause(BaseActivity.this);
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");

    }


    //
    // ApplicationSessionStateCallback
    //

    @Override
    public void onStartSessionRecreation() {
    }
    public String getUserNamepref() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("iNaturalistPreferences", getApplicationContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }
    @Override
    public void onFinishSessionRecreation(boolean success) {
    }
    @Override
    public void onStart() {
        super.onStart();
        try {

                asyncResPau = new OnResumeOnPause(BaseActivity.this);
               asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onPause() {
        super.onPause();

        asyncResPau = new OnResumeOnPause(BaseActivity.this);
    //    asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "", "sendData");
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "&status="+"OFF"+"", "sendData");


    }
    public class OnResumeOnPause extends AsyncTask<String, String, Void> {
        String checkQery;
        InputStream is = null;
        String result = "";
        private BaseActivity ctx;


        public OnResumeOnPause(BaseActivity ctx) {

            this.ctx = ctx;
        }


        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            String url_select = params[0];
            checkQery = params[1];

            if (checkQery.equals("sendData")) {
                HttpClient httpClient = new DefaultHttpClient();
                //  HttpPost httpPost = new HttpPost(url_select);
                HttpGet httpPost = new HttpGet(url_select);
                ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

                try {
                    //httpPost.setEntity(new UrlEncodedFormEntity(param));

                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    HttpEntity httpEntity = httpResponse.getEntity();

                    //read content
                    is = httpEntity.getContent();

                } catch (Exception e) {

                    Log.e("log_tag", "Error in http connection " + e.toString());

                }
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    //get the table data and put it in the result and send it to the Onpostexecute
                    result = sb.toString();


                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("log_tag", "Error converting result " + e.toString());
                }

            }

            return null;
        }

        protected void onPostExecute(Void v) {

        }
    }

}
