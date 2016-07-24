package org.tatzpiteva.golan.Students_ChatMap_Project.View;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.HttpGet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.inaturalist.android.R;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tatzpiteva.golan.Students_ChatMap_Project.ConnectionServer.AsyncTaskConn;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.MyFriendsAdapter;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.RowItem;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.requestAdapter;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.requestTableInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.tblUserFriendInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AllFriends extends Activity {

    private ImageButton friendImg ;
    private TextView friendName ;
    private ArrayList<RowItem> usertList ;
    private AsyncTaskGetMyFriends async ;
    private getMyFriendAndPic async2 ;
    private AsyncTaskConn async4 ;
    private ArrayList<tblUserFriendInfo> friendsArray ;
    private ArrayList<UserInfo> friendsArrayWithPic ;
    private ListView mylistview ;
    private MyFriendsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_friends);
        friendImg = (ImageButton) findViewById(R.id.refresh_Friend_btn);
        friendName = (TextView) findViewById(R.id.up_friendd_list);
        usertList = new ArrayList<RowItem>();

        async = new AsyncTaskGetMyFriends(AllFriends.this);
        async2 = new getMyFriendAndPic(AllFriends.this);

        // get frriends table from db
        async.execute("http://www.chatmaphannataer.com/index.html/getUserFriendsList.php?userName=" + getUserNamepref() + "");



    }


    public String getUserNamepref() {
        SharedPreferences prefs =getApplicationContext().getSharedPreferences("iNaturalistPreferences", getApplicationContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }


    // get the right friends and show them on the screen
    public void Load_Friends_fromServer(){
        int pos ;
        for(int i=0 ; i < friendsArray.size(); i++)
        {

            RowItem actor = new RowItem();


            if(friendsArray.get(i).getUserName().equals(getUserNamepref())) {

                actor.setMember_name(friendsArray.get(i).getFriendWith());
                for(int j=0 ; j < friendsArrayWithPic.size() ; j++){
                    if(friendsArrayWithPic.get(j).getUserName().equals(friendsArray.get(i).getFriendWith())) {

                        actor.setPic(friendsArrayWithPic.get(j).getPic());
                        usertList.add(actor);
                    }
                }


            }
            else if(friendsArray.get(i).getFriendWith().equals(getUserNamepref())){

                actor.setMember_name(friendsArray.get(i).getUserName());
                for(int j=0 ; j < friendsArrayWithPic.size() ; j++){
                    if(friendsArrayWithPic.get(j).getUserName().equals(friendsArray.get(i).getUserName())) {

                        actor.setPic(friendsArrayWithPic.get(j).getPic());
                        usertList.add(actor);
                    }
                }
            }





        }
        mylistview = (ListView) findViewById(R.id.list_Friendss);
        adapter = new MyFriendsAdapter(getApplicationContext(), R.layout.rowfriends, usertList);

        mylistview.setAdapter(adapter);
        friendImg.setVisibility(View.INVISIBLE);
        friendName.setVisibility(View.INVISIBLE);
        if(friendsArray.isEmpty()){
            Toast.makeText(getApplicationContext(),getString(R.string.Thereisnofriends), Toast.LENGTH_LONG).show();

        }

    }



    //get the right tables from db
    class AsyncTaskGetMyFriends  extends AsyncTask<String, String, Void> {
        String checkQery;
        private ProgressDialog progressDialog;
        InputStream is = null;
        String result = "s";
        private Context ctx;
        private String fName = null;
        private String checkP = "no";
        private AllFriends ac;
        private ArrayList<tblUserFriendInfo> requestInfoArray = new ArrayList<tblUserFriendInfo>();

        public AsyncTaskGetMyFriends(AllFriends ac) {

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

                    requestInfoArray.add(new tblUserFriendInfo(userName, userSendR));

                }



            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }
            this.progressDialog.dismiss();
            friendsArray = async.getRequestInfoFromServer();
            async2.execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");


        }

        public ArrayList getRequestInfoFromServer() {

            return requestInfoArray;
        }

        public String getResult() {
            return result;
        }

        public String userName() {
            return fName;
        }

        public ArrayList getSpeceficUser(ArrayList<UserInfo> array) {
            return array;

        }
    }


    class getMyFriendAndPic extends AsyncTask<String, String, Void> {

        private ProgressDialog progressDialog;
        InputStream is = null;
        String result = "";
        private String fName = null;
        private String checkP = "no";
        private AllFriends ctx;
        private ArrayList<UserInfo> userInfoArray = new ArrayList<UserInfo>();

        public getMyFriendAndPic(AllFriends ctx) {

            this.ctx = ctx;
        }


        protected void onPreExecute() {

            progressDialog = new ProgressDialog(ctx);
            progressDialog.setMessage("Fetching data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    getMyFriendAndPic.this.cancel(true);
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
                fName = "nono";


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

                this.progressDialog.dismiss();
                friendsArrayWithPic = async2.getUserInfoFromServer();
                Load_Friends_fromServer();


            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }


        }

        public String getResultFromPHP() {
            return result;
        }

        public String ifProgressDialogFinish() {
            return checkP;
        }

        public ArrayList getUserInfoFromServer() {

            return userInfoArray;
        }

        public String getResult() {
            return result;
        }

        public String userName() {
            return fName;
        }

        public ArrayList getSpeceficUser(ArrayList<UserInfo> array) {
            return array;

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new ExitAndEnterApp().execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "&status=" + "OFF" + "", "sendData");

        //new ExitAndEnterApp().execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "");


    }
    @Override
    public void onStart() {
        super.onStart();

        try {


            new ExitAndEnterApp().execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "");
          //  Toast.makeText(getApplicationContext(), "St", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {


            new ExitAndEnterApp().execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "");
        //    Toast.makeText(getApplicationContext(), "Res", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class ExitAndEnterApp extends AsyncTask<String, String, String> {
        InputStream is = null ;
        String result = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }
        @Override
        protected String doInBackground(String... params) {
            String url_select = params[0];
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

            return null;
        }
        @Override
        protected void onPostExecute(String s) {

        }
    }


}
