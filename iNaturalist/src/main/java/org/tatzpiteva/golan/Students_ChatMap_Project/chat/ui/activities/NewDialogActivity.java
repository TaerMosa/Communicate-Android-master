package org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.HttpGet;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;

import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.inaturalist.android.R;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.RowItem;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.tblUserFriendInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.adapters.UsersAdapter;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.Toaster;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NewDialogActivity extends BaseActivity implements QBEntityCallback<ArrayList<QBUser>> {

    private static final int PAGE_SIZE = 10;
    private OnResumeOnPause asyncResPau ;
    private ArrayList<UserInfo> userInfoArray ;
    private ArrayList<tblUserFriendInfo> friendsArray ;
    private int listViewIndex;
    private int listViewTop;
    private int currentPage = 0;
    private List<QBUser> users = new ArrayList<>();
    private AsyncTaskConn2 async ;
    private AsyncTaskGetMyFriends async2 ;
    private PullToRefreshListView usersList;
    private ArrayList<RowItem> usertList2 ;
    private View createChatButton;
    private ProgressBar progressBar;
    private UsersAdapter usersAdapter;
    public static final int MINIMUM_CHAT_OCCUPANTS_SIZE = 2;
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);
    private long lastClickTime = 0l;
    public static final String EXTRA_QB_USERS = "qb_users";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_new);
       userInfoArray = new ArrayList<UserInfo>();
        friendsArray = new ArrayList<tblUserFriendInfo>()  ;
       usersList = (PullToRefreshListView) findViewById(R.id.usersList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        createChatButton = (View) findViewById(R.id.createChatButton);
        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                ChatService.getInstance().addDialogsUsers(usersAdapter.getSelected());

                // Create new group dialog
                //
                QBDialog dialogToCreate = new QBDialog();
                dialogToCreate.setName(usersListToChatName());
                if (usersAdapter.getSelected().size() == 1) {
                    dialogToCreate.setType(QBDialogType.PRIVATE);
                } else {
                    dialogToCreate.setType(QBDialogType.GROUP);
                }
                dialogToCreate.setOccupantsIds(getUserIds(usersAdapter.getSelected()));

                QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate, new QBEntityCallback<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        if (usersAdapter.getSelected().size() == 1) {
                            startSingleChat(dialog);
                            asyncResPau = new OnResumeOnPause(NewDialogActivity.this);
                            asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");


                        } else {
                            startGroupChat(dialog);
                            asyncResPau = new OnResumeOnPause(NewDialogActivity.this);
                            asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");


                        }
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(NewDialogActivity.this);
                        dialog.setMessage(R.string.InetnetConnectionExc).create().show();
                    }
                });
            }
        });

        async = new AsyncTaskConn2(NewDialogActivity.this);
        async.execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");

        usersList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                loadNextPage();
                listViewIndex = usersList.getRefreshableView().getFirstVisiblePosition();
                View v = usersList.getRefreshableView().getChildAt(0);
                listViewTop = (v == null) ? 0 : v.getTop();
            }
        });

        if(isSessionActive()){
            loadNextPage();
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(NewDialogActivity.this, DialogsActivity.class);
        startActivity(i);
        finish();
    }
    @Override
    public void onStart() {
        super.onStart();
        try {

            asyncResPau = new OnResumeOnPause(NewDialogActivity.this);
            asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onPause() {
        super.onPause();

        asyncResPau = new OnResumeOnPause(NewDialogActivity.this);
      //  asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "", "sendData");
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "&status="+"OFF"+"", "sendData");


    }

    public static QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(PAGE_SIZE);

        return pagedRequestBuilder;
    }


    @Override
    public void onSuccess(ArrayList<QBUser> newUsers, Bundle bundle){

        // save users
        //


        for(int i= 0 ; i < newUsers.size(); i++){

            for(int  j = 0 ; j < userInfoArray.size() ; j++){
                if(newUsers.get(i).getLogin().equals(userInfoArray.get(j).getUserName())){
                    if(userInfoArray.get(j).getWhoContactMe().equals("Everyone")) {
                        users.add(newUsers.get(i));
                    }
                    else if (userInfoArray.get(j).getWhoContactMe().equals("MyContacts")){

                        String ifFriends =   checkIfFriendsOrNot(userInfoArray.get(j).getUserName());
                        if(ifFriends.equals("friends")){
                            users.add(newUsers.get(i));

                        }
                    }
                }
            }

        }
      //  users.addAll(newUsers);

        // Prepare users list for simple adapter.
        //

        usersAdapter = new UsersAdapter(users, this);
        usersList.setAdapter(usersAdapter);
        usersList.onRefreshComplete();
        usersList.getRefreshableView().setSelectionFromTop(listViewIndex, listViewTop);

        progressBar.setVisibility(View.GONE);
        asyncResPau = new OnResumeOnPause(NewDialogActivity.this);
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");


    }

    @Override
    public void onError(QBResponseException errors){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("get users errors: " + errors).create().show();
    }


    private String usersListToChatName(){
        String chatName = "";
        for(QBUser user : usersAdapter.getSelected()){
            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getLogin();
        }
        return chatName;
    }

    public void startSingleChat(QBDialog dialog) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);

        ChatActivity.start(this, bundle);
    }

    private void startGroupChat(QBDialog dialog){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);

        ChatActivity.start(this, bundle);
    }

    private void loadNextPage() {
        ++currentPage;

        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), this);
    }

    public static ArrayList<Integer> getUserIds(List<QBUser> users){
        ArrayList<Integer> ids = new ArrayList<>();
        for(QBUser user : users){
            ids.add(user.getId());
        }
        return ids;
    }

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
                    loadNextPage();
                }
            }
        });
    }



    private void passResultToCallerActivity() {
        Intent result = new Intent();
        ArrayList<QBUser> selectedUsers = new ArrayList<>(usersAdapter.getSelectedUsers());
        result.putExtra(EXTRA_QB_USERS, selectedUsers);
        setResult(RESULT_OK, result);
        finish();
    }


    class AsyncTaskConn2 extends AsyncTask<String, String, Void>
    {
        String checkQery ;
        private ProgressDialog progressDialog ;
        InputStream is = null ;
        String result = "";
        private NewDialogActivity ctx ;
        private String fName = null ;
        private String checkP = "no" ;

        public AsyncTaskConn2(NewDialogActivity ctx) {

            this.ctx = ctx ;
        }



        protected void onPreExecute() {

            progressDialog = new ProgressDialog(ctx);
            progressDialog.setMessage("Fetching data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    AsyncTaskConn2.this.cancel(true);
                }
            });
        }
        @Override
        protected Void doInBackground(String... params) {
            String url_select = params[0];


            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpPost = new HttpGet(url_select);

            try {

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



            return null ;
        }

        protected void onPostExecute(Void v) {


            // ambil data dari Json database
            try {

                JSONArray Jarray = new JSONArray(result);
                for (int i = 0; i < Jarray.length(); i++) {

                    JSONObject Jasonobject = null;
                    Jasonobject = Jarray.getJSONObject(i);

                    //get an output on the screen
                    String userName = Jasonobject.getString("userName");
                    String latitudeLocation = Jasonobject.getString("latitudeLocation");
                    String longitudeLocation = Jasonobject.getString("longitudeLocation");
                    String status = Jasonobject.getString("status");
                    String whatIseeOnMap = Jasonobject.getString("whatIseeOnMap");
                    String whoCanSeeMe = Jasonobject.getString("whoCanSeeMe");
                    String WhoContactMe = Jasonobject.getString("WhoContactMe");
                    String region = Jasonobject.getString("region");
                    String location = Jasonobject.getString("location");
                    String FakeRealUserName = Jasonobject.getString("FakeRealUserName");
                    String observation = Jasonobject.getString("observation");
                    String img = Jasonobject.getString("pic");
                    userInfoArray.add(new UserInfo(userName, latitudeLocation, longitudeLocation, status, whatIseeOnMap, whoCanSeeMe, WhoContactMe
                            , region, location, FakeRealUserName, observation, img));

                }


            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }
            this.progressDialog.dismiss();
            //   userInfoArray = async.getUserInfoFromServer();
            async2 = new AsyncTaskGetMyFriends(NewDialogActivity.this);
            async2.execute("http://www.chatmaphannataer.com/index.html/getUserFriendsList.php?userName=" + getUserNamepref() + "");
        }




        public ArrayList getUserInfoFromServer() {

            return userInfoArray;
        }

    }


    class AsyncTaskGetMyFriends  extends AsyncTask<String, String, Void> {
        String checkQery;
        private ProgressDialog progressDialog;
        InputStream is = null;
        String result = "s";
        private Context ctx;
        private String fName = null;
        private String checkP = "no";
        private NewDialogActivity ac;
        private ArrayList<tblUserFriendInfo> requestInfoArray = new ArrayList<tblUserFriendInfo>();

        public AsyncTaskGetMyFriends(NewDialogActivity ac) {

            this.ac = ac;
        }


        protected void onPreExecute() {

            progressDialog = new ProgressDialog(ac);
            progressDialog.setMessage("Fetching data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    AsyncTaskGetMyFriends.this.cancel(true);
                }
            });
        }

        @Override
        protected Void doInBackground(String... params) {
            String url_select = params[0]; //"http://www.chatmaphannataer.com/index.html/getRequestTable.php?userName=hannasal";


            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpPost = new HttpGet(url_select);

            try {

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


            return null;
        }

        protected void onPostExecute(Void v) {


            // ambil data dari Json database
            try {

                JSONArray Jarray = new JSONArray(result);
                for (int i = 0; i < Jarray.length(); i++) {

                    JSONObject Jasonobject = null;
                    Jasonobject = Jarray.getJSONObject(i);

                    //get an output on the screen
                    String userName = Jasonobject.getString("userName");
                    String userSendR = Jasonobject.getString("friendWith");

                    friendsArray.add(new tblUserFriendInfo(userName, userSendR));

                }



            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }
            this.progressDialog.dismiss();
            friendsArray = async2.getRequestInfoFromServer();
            userInfoArray = async.getUserInfoFromServer();
          //  Toast.makeText(getApplicationContext(), friendsArray.get(0).getUserName(), Toast.LENGTH_LONG).show();
           // Toast.makeText(getApplicationContext(),userInfoArray.get(0).getUserName(), Toast.LENGTH_LONG).show();





        }

        public ArrayList getRequestInfoFromServer() {

            return friendsArray;
        }

    }

    public String getUserNamepref() {
        SharedPreferences prefs =getApplicationContext().getSharedPreferences("iNaturalistPreferences", getApplicationContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }


    public String checkIfFriendsOrNot(String userName){


        for (int i = 0; i < friendsArray.size(); i++) {
            //  Toast.makeText(getActivity(), userInfoArray.get(i).getUserName(), Toast.LENGTH_LONG).show();
            //  Toast.makeText(getActivity(), userInfoArray.get(i).getStatus(), Toast.LENGTH_LONG).show();

            if (friendsArray.get(i).getUserName().equals(getUserNamepref())) {


                if (userName.equals(friendsArray.get(i).getFriendWith())) {



                    return "friends" ;

                }



            }
            else if(friendsArray.get(i).getFriendWith().equals(getUserNamepref())){


                if(userName.equals(friendsArray.get(i).getUserName())) {


                    return "friends" ;
                }



            }
        }



        return "notFriends";
    }
    public class OnResumeOnPause extends AsyncTask<String, String, Void> {
        String checkQery;
        InputStream is = null;
        String result = "";
        private NewDialogActivity ctx;

        public OnResumeOnPause(NewDialogActivity ctx) {

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
