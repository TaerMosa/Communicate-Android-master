package org.tatzpiteva.golan.Students_ChatMap_Project.View;
/**
 * created by ta2er Mosa
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RadioGroup;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;
import com.loopj.android.http.HttpGet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.sanselan.color.ColorConversions;
import org.inaturalist.android.ActivityHelper;

import org.inaturalist.android.R;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tatzpiteva.golan.Students_ChatMap_Project.ConnectionServer.AsyncTaskConn;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.tblUserFriendInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MapControl extends Fragment  {

    private View mainview ;
    private Spinner RadiusSpinner;
    private Button Apply;
    private ActivityHelper mHelper;
    private Context ctx = null;
    private TextView UserName_TV ;
    private RadioGroup rg1 ;
    private RadioGroup rg2 ;
    private RadioGroup rg3 ;
    private RadioGroup rg4 ;
    private RadioGroup rg5 ;
    private String selectionVOOMT ;
    private String selectionWISOM ;
    private String selectionMYLOCATION ;
    private String selectionWCCM;
    private String selectionFakeRealUser;
    private String regionChoose ;
    private AsyncTaskConn async ;
    private AsyncTaskConn async2 ;
    private  getMyFriendAndPic async3 ;
    private ArrayList<UserInfo> userInfoArray ;
    private ArrayList<UserInfo> specificUserArray ;
    private Communicate cm ;
    private AsyncTaskGetMyFriends async4 ;
    private getMyFriendAndPic async5 ;
    private ArrayList<tblUserFriendInfo> friendsArray ;
    private ArrayList<UserInfo> friendsArrayWithPic ;
    private OnResumeOnPause asyncResPau ;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainview = inflater.inflate(R.layout.activity_map_control, container, false);
        ctx = this.getActivity();
        userInfoArray = new ArrayList<UserInfo>();
        specificUserArray = new ArrayList<UserInfo>();
        cm = (Communicate) getActivity();

        rg1 = (RadioGroup) mainview.findViewById(R.id.visibleonmap_RadioG);
        rg2 = (RadioGroup) mainview.findViewById(R.id.seeon_mymap_RadioG);
        rg3 = (RadioGroup) mainview.findViewById(R.id.my_mainLocation);
        rg4 = (RadioGroup) mainview.findViewById(R.id.ConactRadioG);
        rg5 = (RadioGroup) mainview.findViewById(R.id.userName_RG);
     //   specificUserArray.add(new UserInfo(getUserNamepref(), null, null, "ON", "Nobody", "Nobody", "Nobody", null, "Nobody", "Nobody", null, null));

      //  cm.getSpecificArrayFromMapControl(specificUserArray);

        mHelper = new ActivityHelper(ctx);
        UnEableContactMe();
        rg5.setOnCheckedChangeListener(new OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.real_userName) {
                   EnableContactMe();

                } else if(checkedId == R.id.fake_userName) {
                    UnEableContactMe();

                } else {

                    Toast.makeText(getApplicationContext(), "choice: Vibration",

                            Toast.LENGTH_SHORT).show();

                }

            }

        });

        LoadUserName();
        Apply_Listiner();

            return mainview;


    }
    public void UnEableContactMe()
    {
        for(int i = 0 ; i < rg4.getChildCount() ; i++){
            ((RadioButton)rg4.getChildAt(i)).setEnabled(false);
        }
    }
    public void EnableContactMe(){
        for(int i = 0 ; i < rg4.getChildCount() ; i++){
            ((RadioButton)rg4.getChildAt(i)).setEnabled(true);
        }
    }

    // when the user exit the app update status to off
    @Override
    public void onPause() {
        super.onPause();

      //  asyncResPau = new OnResumeOnPause(ctx);
      //  asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "", "sendData");


    }
    // when the user enter  the app update status to on

    @Override
    public void onStart() {
        super.onStart();
        try {

            asyncResPau = new OnResumeOnPause(ctx);
            asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "","sendData");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void LoadUserName(){
        UserName_TV = (TextView)mainview.findViewById(R.id.UserNameText);
        SharedPreferences prefs= ctx.getSharedPreferences("iNaturalistPreferences", ctx.MODE_PRIVATE);
        String username = prefs.getString("username", null);
        UserName_TV.setText(username);
        if(username!=null){
            UserName_TV.setHint(username);
        }
    }

    // when the user press the apply btn
    public void Apply_Listiner(){

        Apply = (Button) mainview.findViewById(R.id.Apply_Id_btn);
        Apply.setOnClickListener(new View.OnClickListener() {


            @Override

            public void onClick(View view) {

                intilizaRadioGroupAndSpinner();
                mHelper.confirm(R.string.ChatMapControl, R.string.MapControlsaveChanges

                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int id) {
                                asyncResPau = new OnResumeOnPause(ctx);
                                asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "","sendData");

                                updateDataToUserTable();


                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int i) {

                                dialog.cancel();
                            }
                        }
                );
            }
        });
    }

    // get what the user choose from this activity
    public void intilizaRadioGroupAndSpinner() {
        if (rg1.getCheckedRadioButtonId() != -1) {
            int id = rg1.getCheckedRadioButtonId();
            View radioButton = rg1.findViewById(id);
            int radioId = rg1.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) rg1.getChildAt(radioId);
            selectionVOOMT = (String) btn.getText();
            if(selectionVOOMT.equals("Friends")){
                selectionVOOMT = "MyContacts";
            }
        }
        if (rg2.getCheckedRadioButtonId() != -1) {
            int id = rg2.getCheckedRadioButtonId();
            View radioButton = rg2.findViewById(id);
            int radioId = rg2.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) rg2.getChildAt(radioId);
            selectionWISOM = (String) btn.getText();
            if(selectionWISOM.equals("Friends")){
                selectionWISOM = "MyContacts";
            }
        }
        if (rg3.getCheckedRadioButtonId() != -1) {
            int id = rg3.getCheckedRadioButtonId();
            View radioButton = rg3.findViewById(id);
            int radioId = rg3.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) rg3.getChildAt(radioId);
            selectionMYLOCATION = (String) btn.getText();
            if(selectionMYLOCATION.equals("Real Location"))
            {
                selectionMYLOCATION = "Real";
            }else{
                selectionMYLOCATION = "Fake";
            }
        }
        if (rg4.getCheckedRadioButtonId() != -1) {
            int id = rg4.getCheckedRadioButtonId();
            View radioButton = rg4.findViewById(id);
            int radioId = rg4.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) rg4.getChildAt(radioId);
            selectionWCCM = (String) btn.getText();
            if(selectionWCCM.equals("Friends")){
                selectionWCCM = "MyContacts";
            }
        }
        if (rg5.getCheckedRadioButtonId() != -1) {
            int id = rg5.getCheckedRadioButtonId();
            View radioButton = rg5.findViewById(id);
            int radioId = rg5.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) rg5.getChildAt(radioId);
            selectionFakeRealUser = (String) btn.getText();
            if(selectionFakeRealUser.equals("Real"))
            {
                selectionFakeRealUser = "Real";
            }else{
                selectionFakeRealUser = "Anynm";
            }
        }

    }
    public String getUserNamepref() {
        SharedPreferences prefs = ctx.getSharedPreferences("iNaturalistPreferences", ctx.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }
    //update the row of the user when what he choose
    public void updateDataToUserTable(){


        //update the datqa that the user choose
        async = new AsyncTaskConn(ctx);
        async2 = new AsyncTaskConn(ctx);
        async.execute("http://www.chatmaphannataer.com/index.html/updateUserControlView.php?userName=" + getUserNamepref() + "&whoCanSeeMe=" + selectionVOOMT + "&whatIseeOnMap=" + selectionWISOM + "&region=" + regionChoose + "&WhoContactMe=" + selectionWCCM + "&location=" + selectionMYLOCATION + "&FakeRealUserName=" + selectionFakeRealUser + "", "sendData");


        async3 = new  getMyFriendAndPic(ctx);
        async4 = new AsyncTaskGetMyFriends(ctx);
        async5 = new getMyFriendAndPic(ctx);
//        cm = (Communicate) getActivity();

        putDataIntoMyUser();
        // if the user select what i see on map everone ?
        if(selectionWISOM.equals("Everyone")) {
            //get the user table from the server


            //bring tbl user from db
            async3.execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php","EveryOne");





            // if the user select my contacts bring the user table and friends table
        }else  if(selectionWISOM.equals("MyContacts")){
            async5.execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php","Friend");

         //   async4.execute("http://www.chatmaphannataer.com/index.html/getUserFriendsList.php?userName=" + getUserNamepref() + "");


        }
        // if he choose nobosy just call the method callsetupcluster in cumminicate frag
        else if (selectionWISOM.equals("Nobody"))
        {
            cm.callSetUpCluster("Nobody");

        }






    }
    public void putDataIntoMyUser(){
        specificUserArray.add(new UserInfo(getUserNamepref() ,null,null,"ON",selectionWISOM,selectionVOOMT, selectionWCCM,null,selectionMYLOCATION,selectionFakeRealUser,null,null));
        cm.getSpecificArrayFromMapControl(specificUserArray);
    }

  /*  @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
          Toast.makeText(getApplicationContext(),"changed", Toast.LENGTH_SHORT).show();




    }*/


    // bring from db the specific tables and update the db ...

    class AsyncTaskGetMyFriends  extends AsyncTask<String, String, Void> {

        private ProgressDialog progressDialog;
        InputStream is = null;
        String result = "s";
        private Context ctx;
        private String fName = null;
        private String check = null ;
        private ArrayList<tblUserFriendInfo> requestInfoArray = new ArrayList<tblUserFriendInfo>();

        public AsyncTaskGetMyFriends(Context ctx) {

            this.ctx = ctx;
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

            check = params[1];

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

            if(check.equals("EveryOne")){
                friendsArray = async4.getRequestInfoFromServer();
                userInfoArray = async3.getUserInfoFromServer();
                cm.getUserArray(userInfoArray);
                cm.getFriendArray(friendsArray);
                cm.getSpecificArrayFromMapControl(specificUserArray);
                cm.callSetUpCluster("EveryOne");
            }else if(check.equals("Friend")) {

                friendsArray = async4.getRequestInfoFromServer();
                friendsArrayWithPic = async5.getUserInfoFromServer();
                cm.getFriendArrayWithPic(friendsArrayWithPic);
                cm.getFriendArray(friendsArray);
                cm.getSpecificArrayFromMapControl(specificUserArray);
                cm.callSetUpCluster("Friend");

            }


        }

        public ArrayList getRequestInfoFromServer() {

            return requestInfoArray;
        }

        public String getResult() {
            return result;
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
        private Context ctx;
        private String  check = null ;
        private ArrayList<UserInfo> userInfoArray = new ArrayList<UserInfo>();

        public getMyFriendAndPic(Context ctx) {

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
            check = params[1] ;





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
            if(check.equals("EveryOne")) {
                async4.execute("http://www.chatmaphannataer.com/index.html/getUserFriendsList.php?userName=" + getUserNamepref() + "", "EveryOne");

            }
            else if(check.equals("Friend")){
                async4.execute("http://www.chatmaphannataer.com/index.html/getUserFriendsList.php?userName=" + getUserNamepref() + "", "Friend");


            }


        }



        public ArrayList getUserInfoFromServer() {

            return userInfoArray;
        }

        public String getResult() {
            return result;
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


}
