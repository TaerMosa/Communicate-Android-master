package org.tatzpiteva.golan.Students_ChatMap_Project.View;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.loopj.android.http.HttpGet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.inaturalist.android.ActivityHelper;
import org.inaturalist.android.R;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tatzpiteva.golan.Students_ChatMap_Project.ConnectionServer.AsyncTaskConn;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.CustomAdapter;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.RowItem;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserAdapter;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.requestTableInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.tblUserFriendInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MapChat extends Fragment implements OnItemClickListener  {
    // Contacts List Vars
    String[] member_names;
    TypedArray profile_pics;
    private ImageButton refresh_btn ;
    private ImageButton friends_list_img ;
    private ImageButton request_friend_img ;
    private TextView uploadUserL ;
    private Button upload_btn ;
    private Button add_btn ;
    String[] contactType;
    String[] Status;
    ListView list ;
    UserAdapter adapter;
    ArrayList<RowItem> usertList ;
    List<RowItem> rowItems;
    ListView mylistview;
    private View mainview ;
    private Context ctx;
    private ActivityHelper mHelper;
    private AsyncTaskConn2 async ;
    private ArrayList<UserInfo> userSpecificArray ;

    private Communicate cm ;
    private AsyncTaskConn async4 ;
    private AsyncTaskFriendRequest2 async5  ;
    private AsyncTaskFriendRequest3 async7  ;
    private AsyncTaskGetMyFriends async6 ;
    private ArrayList<requestTableInfo> requestArray ;
    private ArrayList<UserInfo> userInfoArray = new ArrayList<UserInfo>();
    private ArrayList<requestTableInfo> requestInfoArray = new ArrayList<requestTableInfo>();
    private ArrayList<requestTableInfo> requestInfoArrayCount = new ArrayList<requestTableInfo>();
    private ArrayList<requestTableInfo> requestInfoArrayCount2 = new ArrayList<requestTableInfo>();

    private ArrayList<tblUserFriendInfo> friendsArray  = new ArrayList<tblUserFriendInfo>();
    private TextView countReq ;
    private OnResumeOnPause asyncResPau ;
    private int sizeofReqArray = -1 ;
    private int countResume = 0 ;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainview = inflater.inflate(R.layout.activity_map_chat, container, false);
        ctx = this.getActivity();
        mHelper = new ActivityHelper(ctx);
        userSpecificArray = new ArrayList<UserInfo>();
        cm = (Communicate) getActivity();

        usertList = new ArrayList<RowItem>();
        refresh_btn = (ImageButton) mainview.findViewById(R.id.refresh_btn);
        friends_list_img = (ImageButton) mainview.findViewById(R.id.friends_btn);
        request_friend_img = (ImageButton) mainview.findViewById(R.id.request_btn);
        uploadUserL = (TextView) mainview.findViewById(R.id.textView2) ;

        countReq = (TextView) mainview.findViewById(R.id.countReq);
        request_friend_img.setOnClickListener(request_friend_img_btnlistener);
        friends_list_img.setOnClickListener(friend_img_btnlistener);
    //    async = new AsyncTaskConn2(ctx);
    //    async.execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");

        // get users from db
   //     async = new AsyncTaskConn2(ctx);
   //     async.execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");
   //     userInfoArray = async.getUserInfoFromServer();



        add_btn = (Button) mainview.findViewById(R.id.addBtn);
        return mainview ;
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String member_name = rowItems.get(position).getMember_name();
        Toast.makeText(ctx, "" + member_name,
                Toast.LENGTH_SHORT).show();
    }

    // when the user click request img go to the right avtivity
    public View.OnClickListener request_friend_img_btnlistener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

           Intent i = new Intent(getActivity(),Accept_FrReq_Ac.class);
            startActivity(i);





        }
    };

    // when the user click the friends img go to right activity
    public View.OnClickListener friend_img_btnlistener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Intent i = new Intent(getActivity(),AllFriends.class);
            startActivity(i);





        }
    };



    // show the specific users by the conditions on the screen
    public void Load_user_fromServer(){
        int count = 0 ;
        countResume++;

        usertList.clear();
    // put the number of who send me request on the req img
    for (int i = 0; i < userInfoArray.size(); i++) {
        count = 0;
        RowItem actor = new RowItem();

        for (int j = 0; j < requestInfoArray.size(); j++) {

            if (userInfoArray.get(i).getUserName().equals(requestInfoArray.get(j).getUserName())) {


            } else {
                count++;
            }


        }
        if (count == requestInfoArray.size()) {
            String checkFriend;
            if (!userInfoArray.get(i).getUserName().equals(getUserNamepref())) {

                checkFriend = checkIfFriendsOrNot(userInfoArray.get(i).getUserName());

                // if we r not friends show u on the screen
                if (checkFriend.equals("notFriends")) {
                    actor.setMember_name(userInfoArray.get(i).getUserName());
                    actor.setPic(userInfoArray.get(i).getPic());

                    usertList.add(actor);
                }
            }
        }


    }
    mylistview = (ListView) mainview.findViewById(R.id.list1);
    adapter = new UserAdapter(getApplicationContext(), R.layout.row, usertList);

    mylistview.setAdapter(adapter);
    refresh_btn.setVisibility(View.INVISIBLE);
    uploadUserL.setVisibility(View.INVISIBLE);
    if (userInfoArray.isEmpty()) {
        Toast.makeText(getActivity(), getString(R.string.ThereisnouserstosendFriendRequest), Toast.LENGTH_LONG).show();

    }



    }


    public String getUserNamepref() {
        SharedPreferences prefs = mainview.getContext().getSharedPreferences("iNaturalistPreferences", mainview.getContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }
    @Override
    public void onPause() {
        super.onPause();

      //  asyncResPau = new OnResumeOnPause(ctx);

    //    asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "", "sendData");


    }
    // when the user enter  the app update status to on

    @Override
    public void onStart() {
        super.onStart();
        try {
            async = new AsyncTaskConn2(ctx);
            async.execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");


                    asyncResPau = new OnResumeOnPause(ctx);
                    asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "","sendData");


        //    async7 = new AsyncTaskFriendRequest3(this.ctx);
        //    async7.execute("http://www.chatmaphannataer.com/index.html/getRequestTable.php?userName=" + getUserNamepref() + "","Resume");



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // get the right tables from db and put them in the right arrays
    class AsyncTaskConn2 extends AsyncTask<String, String, Void>
    {
        String checkQery ;
        private ProgressDialog progressDialog ;
        InputStream is = null ;
        String result = "";
        private Context ctx ;
        private String fName = null ;
        private String checkP = "no" ;
        public AsyncTaskConn2(Context ctx) {

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
            userInfoArray = async.getUserInfoFromServer();
            async5 = new AsyncTaskFriendRequest2(this.ctx);
            async5.execute("http://www.chatmaphannataer.com/index.html/getRequestTableChatScreen.php?userName=" + getUserNamepref() + "");
        }


        public String ifProgressDialogFinish() {
            return checkP;
        }

        public ArrayList getUserInfoFromServer() {

            return userInfoArray;
        }

        public ArrayList getSpeceficUser(ArrayList<UserInfo> array) {
            return array;

        }
    }

    class AsyncTaskFriendRequest2  extends AsyncTask<String, String, Void> {
        String checkQery;
        private ProgressDialog progressDialog;
        InputStream is = null;
        String result = "s";
        private String fName = null;
        private String checkP = "no";
        private Context ctx ;

        public AsyncTaskFriendRequest2(Context ctx) {

            this.ctx = ctx;
        }


        protected void onPreExecute() {

            progressDialog = new ProgressDialog(ctx);
            progressDialog.setMessage("Fetching data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    AsyncTaskFriendRequest2.this.cancel(true);
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
                    String userSendR = Jasonobject.getString("userSendR");
                    String pic = Jasonobject.getString("pic");
                    requestInfoArray.add(new requestTableInfo(userName, userSendR, pic));

                }



            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }
            this.progressDialog.dismiss();
            requestInfoArray = async5.getRequestInfoFromServer();
            async6 = new AsyncTaskGetMyFriends(this.ctx);
            async6.execute("http://www.chatmaphannataer.com/index.html/getUserFriendsList.php?userName=" + getUserNamepref() + "");





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
    public String checkIfFriendsOrNot(String userName){


        for (int i = 0; i < friendsArray.size(); i++) {


            if (friendsArray.get(i).getUserName().equals(getUserNamepref())) {


                if (userName.equals(friendsArray.get(i).getFriendWith())) {


                    return "friends" ;

                }

                //return "notFriends" ;


            }
            else if(friendsArray.get(i).getFriendWith().equals(getUserNamepref())){


                if(userName.equals(friendsArray.get(i).getUserName())) {


                    return "friends" ;
                }



            }
        }



        return "notFriends";
    }


    class AsyncTaskGetMyFriends  extends AsyncTask<String, String, Void> {
        String checkQery;
        private ProgressDialog progressDialog;
        InputStream is = null;
        String result = "s";
        private Context ctx;
        private String fName = null;
        private String checkP = "no";


        public AsyncTaskGetMyFriends(Context ctx) {

            this.ctx = ctx ;
        }


        protected void onPreExecute() {

            progressDialog = new ProgressDialog(ctx);
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
            //  HttpPost httpPost = new HttpPost(url_select);
            HttpGet httpPost = new HttpGet(url_select);
            // ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

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

        protected void onPostExecute(Void v) {


            // ambil data dari Json database
            try {

                JSONArray Jarray = new JSONArray(result);
                for (int i = 0; i < Jarray.length(); i++) {

                    JSONObject Jasonobject = null;
                    Jasonobject = Jarray.getJSONObject(i);

                    //get an output on the screen
                    //fName = Jasonobject.getString("userName");
                    String userName = Jasonobject.getString("userName");
                    String userSendR = Jasonobject.getString("friendWith");

                    friendsArray.add(new tblUserFriendInfo(userName, userSendR));

                }



            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }
            this.progressDialog.dismiss();
            friendsArray = async6.getRequestInfoFromServer();
            async7 = new AsyncTaskFriendRequest3(this.ctx);
            async7.execute("http://www.chatmaphannataer.com/index.html/getRequestTable.php?userName=" + getUserNamepref() + "");


        }

        public ArrayList getRequestInfoFromServer() {

            return friendsArray;
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



    class AsyncTaskFriendRequest3  extends AsyncTask<String, String, Void> {
        String checkQery ;
        private ProgressDialog progressDialog;
        InputStream is = null;
        String result = "s";
        private String fName = null;
        private String checkP = "no";
        private Context ctx ;

        public AsyncTaskFriendRequest3(Context ctx) {

            this.ctx = ctx;
        }


        protected void onPreExecute() {

            progressDialog = new ProgressDialog(ctx);
            progressDialog.setMessage("Fetching data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    AsyncTaskFriendRequest3.this.cancel(true);
                }
            });
        }

        @Override
        protected Void doInBackground(String... params) {
            String url_select = params[0]; //"http://www.chatmaphannataer.com/index.html/getRequestTable.php?userName=hannasal";


            HttpClient httpClient = new DefaultHttpClient();
            //  HttpPost httpPost = new HttpPost(url_select);
            HttpGet httpPost = new HttpGet(url_select);
            // ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

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

        protected void onPostExecute(Void v) {



                // ambil data dari Json database
                try {

                    JSONArray Jarray = new JSONArray(result);
                    for (int i = 0; i < Jarray.length(); i++) {

                        JSONObject Jasonobject = null;
                        Jasonobject = Jarray.getJSONObject(i);

                        //get an output on the screen
                        //fName = Jasonobject.getString("userName");
                        String userName = Jasonobject.getString("userName");
                        String userSendR = Jasonobject.getString("userSendR");
                        String pic = Jasonobject.getString("pic");
                        requestInfoArrayCount.add(new requestTableInfo(userName, userSendR, pic));
                        requestInfoArrayCount2.add(new requestTableInfo(userName, userSendR, pic));




                    }


                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }

            this.progressDialog.dismiss();

                Load_user_fromServer();
                putCountOnText();







        }

        public ArrayList getRequestInfoFromServer() {

            return requestInfoArrayCount;
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



    public class OnResumeOnPause extends AsyncTask<String, String, Void>
    {
        String checkQery ;
        InputStream is = null ;
        String result = "";
        private Context ctx ;
        public OnResumeOnPause(Context ctx) {

            this.ctx = ctx ;
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

            return null ;
        }

        protected void onPostExecute(Void v) {

        }





    }
    public void putCountOnText(){
        sizeofReqArray = 0 ;
        sizeofReqArray = requestInfoArrayCount2.size();
        countReq.setText("");
        countReq.setText(String.valueOf(requestInfoArrayCount2.size()));
        sizeofReqArray = 0 ;
        requestInfoArrayCount2.clear();
        userInfoArray.clear();
        friendsArray.clear();
        requestInfoArray.clear();
    }

}
