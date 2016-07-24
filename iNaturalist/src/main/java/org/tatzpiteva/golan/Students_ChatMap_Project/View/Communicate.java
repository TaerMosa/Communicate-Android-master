package org.tatzpiteva.golan.Students_ChatMap_Project.View;
/**
 * Created by ta2er Mosa
 */
import android.annotation.SuppressLint;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TabHost;
import android.widget.TextView;
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
import org.inaturalist.android.BaseFragmentActivity;
import org.inaturalist.android.MyPageAdapter;
import org.inaturalist.android.MyTabFactory;
import org.inaturalist.android.R;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tatzpiteva.golan.LaunchScreenActivity;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.requestTableInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.tblUserFriendInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Communicate extends BaseFragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener  {
    MyPageAdapter mPageAdapter;
    private ViewPager mViewPager;
    private boolean exitPending;
    private TabHost mTabHost;
    private ArrayList<requestTableInfo> requestInfoArrayCount2 = new ArrayList<requestTableInfo>();
    private AsyncTaskFriendRequest3 async7  ;
    private TextView t2;
    private Context ctx;
    private ArrayList<UserInfo> array ;
    private MyMapView f1 ;
    private MapChat f2 ;
    private ArrayList<UserInfo> specificArray ;
    private ArrayList<tblUserFriendInfo> friendsArray ;
    private ArrayList<UserInfo> friendsArrayWithPic ;
    private MapControl f3 ;
    private MapControl mc ;
    private int countScreen = 0 ;
    private OnResumeOnPause asyncResPau ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        array = new ArrayList<UserInfo>() ;
        this.specificArray = new ArrayList<UserInfo>();
        setContentView(R.layout.activity_communicate);
        onDrawerCreate(savedInstanceState);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);


        // Tab Initialization
       /* initialiseTabHost();

        // Fragments and ViewPager Initialization

            List<Fragment> fragments = getFragmentsActivies();
            mPageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
            mViewPager.setAdapter(mPageAdapter);
            mViewPager.setOnPageChangeListener(this);
            countScreen++ ;*/



    }

    // 2.0 and above
    @Override
    public void onBackPressed() {
        super.onBackPressed();
       /* Intent i = new Intent(Communicate.this,LaunchScreenActivity.class);
        this.finish();
        startActivity(i);*/
    }

    @Override
    public void onPause() {
        super.onPause();

        asyncResPau = new OnResumeOnPause(Communicate.this);
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "&status="+"OFF"+"", "sendData");
    //    asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "","sendData");

        // Toast.makeText(getApplicationContext(), "Pause", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    //    Toast.makeText(getApplicationContext(), "Destroy ", Toast.LENGTH_SHORT).show();



    }

    @Override
    public void onResume() {
        super.onResume();

        asyncResPau = new OnResumeOnPause(Communicate.this);
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "","sendData");



    }
  /*  @Override
    public void onStop() {
        super.onStop();

        asyncResPau = new OnResumeOnPause(Communicate.this);

        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "", "sendData");

        Toast.makeText(getApplicationContext(), "Stop ", Toast.LENGTH_SHORT).show();



    }
*/

    @Override
    public void onStart() {
        super.onStart();
        try {
            countScreen ++ ;

            async7 = new AsyncTaskFriendRequest3(Communicate.this);
            async7.execute("http://www.chatmaphannataer.com/index.html/getRequestTable.php?userName=" + getUserNamepref() + "");

           // getSS();
          //  asyncResPau = new OnResumeOnPause(Communicate.this);
          //  asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "","sendData");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getSS()
    {


        ChatService.getInstance().getDialogs(new QBEntityCallback<ArrayList<QBDialog>>() {
            int  sum =0;
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle bundle) {
                for (QBDialog d : dialogs) {
                    if (d != null) {
                        sum += d.getUnreadMessageCount();
                    } else {
                        sum = -1;
                    }
                }
                Toast.makeText(getApplicationContext(),String.valueOf(sum), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(QBResponseException errors) {
            }
        });


    }




    // Method to add a TabHost
    private static void AddTab(Communicate activity, TabHost tabHost, TabHost.TabSpec tabSpec) {
        tabSpec.setContent(new MyTabFactory(activity));
        tabHost.addTab(tabSpec);
    }

    // Manages the Tab changes, synchronizing it with Pages
    public void onTabChanged(String tag) {
        int pos = this.mTabHost.getCurrentTab();
        this.mViewPager.setCurrentItem(pos);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    // Manages the Page changes, synchronizing it with Tabs
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        int pos = this.mViewPager.getCurrentItem();
        this.mTabHost.setCurrentTab(pos);
    }

    @Override
    public void onPageSelected(int arg0) {
    }

    private List<Fragment> getFragmentsActivies(){
        List<Fragment> fList = new ArrayList<>();

        f1 = new MyMapView();
        f2 = new MapChat();
        f3 = new MapControl();
        fList.add(f3);
        fList.add(f1);
        fList.add(f2);
       // countFr_req.setText(f2.getCountOfReq());

        return fList;
    }

    // Tabs Creation
    @SuppressLint("NewApi")
    private void initialiseTabHost() {

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        Communicate.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Control").setIndicator("", getResources().getDrawable(R.drawable.communi_controltab)));
        Communicate.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Map").setIndicator("", getResources().getDrawable(R.drawable.communi_map_tab)));
        Communicate.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Chat").setIndicator("", getResources().getDrawable(R.drawable.communi_chattab)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
          ((TextView) mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title)).setAllCaps(false);

            ((TextView) mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title)).setAllCaps(false);
             t2 =  (TextView) mTabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
            if(requestInfoArrayCount2.size()>0) {
                t2.setText(String.valueOf(requestInfoArrayCount2.size()));
                requestInfoArrayCount2.clear();
                //t2.setTextColor();
            }else{
                t2.setText("");
            }

        }

        mTabHost.getTabWidget().getChildAt(0).setBackgroundDrawable(getResources().getDrawable(R.drawable.inatapptheme_tab_indicator_holo));
        mTabHost.getTabWidget().getChildAt(1).setBackgroundDrawable(getResources().getDrawable(R.drawable.inatapptheme_tab_indicator_holo));
        mTabHost.getTabWidget().getChildAt(2).setBackgroundDrawable(getResources().getDrawable(R.drawable.inatapptheme_tab_indicator_holo));


        mTabHost.setOnTabChangedListener(this);

    }
    public ViewPager returnPager()
    {
        return mViewPager ;
    }
    public void getUserArray(ArrayList<UserInfo> array){
        this.array = array ;
        //callSetUpCluster();


    }
    public void getFriendArray( ArrayList<tblUserFriendInfo> friendsArray){
        this.friendsArray = friendsArray ;

    }
    public void getFriendArrayWithPic(ArrayList<UserInfo> friendsArrayWithPic ){
        this.friendsArrayWithPic = friendsArrayWithPic ;

    }
    public ArrayList getFriendArray(){
        return this.friendsArray ;
    }
    public ArrayList getFriendArrayWithPic(){
        return this.friendsArrayWithPic;
    }
    public String getUserName(){
        return this.array.get(0).getUserName() ;

    }
    public ArrayList getUserArray(){
        return this.array ;

    }
    public void callSetUpCluster(String str)
    {


        // go to mapview frag and send to setupclusters what i choose on control frag (what i see on map)
        returnPager().setCurrentItem(1);
        if(str.equals("Friend")){
            f1.setUpClusterer("Friend");
        }
        else if(str.equals("EveryOne")){
            f1.setUpClusterer("EveryOne");
        }
        else if(str.equals("Nobody")){
            f1.setUpClusterer("Nobody");

        }

    }
    public void getSpecificArrayFromMapControl(ArrayList<UserInfo> speceficArray){

        this.specificArray = speceficArray ;
    }
    public ArrayList getSpecificUserInThisApp(){
        return this.specificArray ;
    }
    public void countFriendReq(int c){
        String count = String.valueOf(c);
    }


    class AsyncTaskFriendRequest3  extends AsyncTask<String, String, Void> {
        String checkQery ;
        private ProgressDialog progressDialog;
        InputStream is = null;
        String result = "s";
        private String fName = null;
        private String checkP = "no";
        private Communicate ctx ;

        public AsyncTaskFriendRequest3(Communicate ctx) {

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
                    requestInfoArrayCount2.add(new requestTableInfo(userName, userSendR, pic));




                }


            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }

            this.progressDialog.dismiss();


            // Fragments and ViewPager Initialization

            if(countScreen==1) {
                initialiseTabHost();
                List<Fragment> fragments = getFragmentsActivies();
                mPageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
                mViewPager.setAdapter(mPageAdapter);
                mViewPager.setOnPageChangeListener(Communicate.this);
                asyncResPau = new OnResumeOnPause(Communicate.this);
                asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");

            }else{
                if(requestInfoArrayCount2.size()>0) {

                    t2.setText(String.valueOf(requestInfoArrayCount2.size()));
                    requestInfoArrayCount2.clear();
                    asyncResPau = new OnResumeOnPause(Communicate.this);
                    asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "","sendData");

                }
                else{
                    t2.setText("");
                }
            }










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
    public String getUserNamepref() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("iNaturalistPreferences", getApplicationContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }


    public class OnResumeOnPause extends AsyncTask<String, String, Void> {
        String checkQery;
        InputStream is = null;
        String result = "";
        private Communicate ctx;

        public OnResumeOnPause(Communicate ctx) {

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

