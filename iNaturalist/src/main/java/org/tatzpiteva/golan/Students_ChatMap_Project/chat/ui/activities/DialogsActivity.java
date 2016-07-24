package org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.HttpGet;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.inaturalist.android.R;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.Chat;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.pushnotifications.Consts;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.pushnotifications.GooglePlayServicesHelper;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.pushnotifications.PlayServicesHelper;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.adapters.DialogsAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DialogsActivity extends BaseActivity {

    private static final String TAG = DialogsActivity.class.getSimpleName();

    private ListView dialogsListView;
    private ProgressBar progressBar;
    private OnResumeOnPause asyncResPau ;
    private GooglePlayServicesHelper googlePlayServicesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogs_activity);


        googlePlayServicesHelper = new GooglePlayServicesHelper();
        if (googlePlayServicesHelper.checkPlayServicesAvailable(this)) {
            googlePlayServicesHelper.registerForGcm(Consts.GCM_SENDER_ID);
        }
        dialogsListView = (ListView) findViewById(R.id.roomsList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        LinearLayout emptyHintLayout = (LinearLayout) findViewById(R.id.layout_chat_empty);
        dialogsListView.setEmptyView(emptyHintLayout);
        // Register to receive push notifications events
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));

        // Get dialogs if the session is active
        //
        if(isSessionActive()){
            getDialogs();
        }
        asyncResPau = new OnResumeOnPause(DialogsActivity.this);
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");

    }

    private void getDialogs(){
        progressBar.setVisibility(View.VISIBLE);

        // Get dialogs
        //
        ChatService.getInstance().getDialogs(new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle bundle) {
                progressBar.setVisibility(View.GONE);

                // build list view
                //



                buildListView(dialogs);

                asyncResPau = new OnResumeOnPause(DialogsActivity.this);
                asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");

            }

            @Override
            public void onError(QBResponseException errors) {
                progressBar.setVisibility(View.GONE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(DialogsActivity.this);
                dialog.setMessage(R.string.InetnetConnectionExc).create().show();
            }
        });
    }


    void buildListView(List<QBDialog> dialogs){
        final DialogsAdapter adapter = new DialogsAdapter(dialogs, DialogsActivity.this);
        dialogsListView.setAdapter(adapter);


        // choose dialog
        //
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBDialog selectedDialog = (QBDialog) adapter.getItem(position);

                Bundle bundle = new Bundle();
                bundle.putSerializable(ChatActivity.EXTRA_DIALOG, selectedDialog);

                // Open chat activity
                //
                ChatActivity.start(DialogsActivity.this, bundle);

                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        googlePlayServicesHelper.checkPlayServices(this);
    }
    @Override
    public void onStart() {
        super.onStart();
        try {

            asyncResPau = new OnResumeOnPause(DialogsActivity.this);
            asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onStop() {
        super.onStop();

        asyncResPau = new OnResumeOnPause(DialogsActivity.this);
      //  asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "", "sendData");
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "&status="+"OFF"+"", "sendData");


    }
    public String getUserNamepref() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("iNaturalistPreferences", getApplicationContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rooms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {

            // go to New Dialog activity
            //
            Intent intent = new Intent(DialogsActivity.this, NewDialogActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onStartNewChatClick(View view){
        // go to New Dialog activity
        //
        Intent intent = new Intent(DialogsActivity.this, NewDialogActivity.class);
        startActivity(intent);
        finish();
    }

    // Our handler for received Intents.
    //
    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            String message = intent.getStringExtra(Consts.EXTRA_MESSAGE);

            Log.i(TAG, "Receiving event " + Consts.NEW_PUSH_EVENT + " with data: " + message);
        }
    };


    //
    // ApplicationSessionStateCallback
    //

    @Override
    public void onStartSessionRecreation() {

    }

    @Override
    public void onFinishSessionRecreation(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    getDialogs();
                }
            }
        });
    }

    public class OnResumeOnPause extends AsyncTask<String, String, Void> {
        String checkQery;
        InputStream is = null;
        String result = "";
        private DialogsActivity ctx;

        public OnResumeOnPause(DialogsActivity ctx) {

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
