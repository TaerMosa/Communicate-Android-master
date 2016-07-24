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
import org.tatzpiteva.golan.Students_ChatMap_Project.ConnectionServer.AsyncTaskFriendRequest;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.RowItem;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserAdapter;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.requestAdapter;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.requestTableInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

public class Accept_FrReq_Ac extends Activity {

    private ImageButton userImage ;
    private TextView uploadTV ;
    private AsyncTaskFriendRequest2 async2  ;
    private AsyncTaskConn async4,async5 ;
    private ArrayList<requestTableInfo> requestArray ;
    private ArrayList<RowItem> usertList ;
    private ListView mylistview;
    requestAdapter adapter;
    private Context ctx ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept__fr_req_);

         this.ctx = getApplicationContext();
        usertList = new ArrayList<RowItem>();
        requestArray = new ArrayList<requestTableInfo>();
        userImage = (ImageButton) findViewById(R.id.refresh_Fr_req_btn);
        uploadTV = (TextView) findViewById(R.id.up_re_list);


        // GET the request table from db
        async2 = new AsyncTaskFriendRequest2(Accept_FrReq_Ac.this);
        async2.execute("http://www.chatmaphannataer.com/index.html/getRequestTable.php?userName="+getUserNamepref()+"");


    }



    public String getUserNamepref() {
        SharedPreferences prefs =getApplicationContext().getSharedPreferences("iNaturalistPreferences", getApplicationContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }

    // load the requests and show them  (who send me request)
    public void Load_user_fromServer(){

        for(int i=0 ; i < requestArray.size(); i++)
        {

            RowItem actor = new RowItem();

               if(!requestArray.get(i).getUserSendR().equals(getUserNamepref())) {
            actor.setMember_name(requestArray.get(i).getUserSendR());
            actor.setPic(requestArray.get(i).getPic());

            usertList.add(actor);
              }




        }
        mylistview = (ListView) findViewById(R.id.list_Fr_Req);
        adapter = new requestAdapter(getApplicationContext(), R.layout.row_friend_req_accept, usertList);

        mylistview.setAdapter(adapter);
        userImage.setVisibility(View.INVISIBLE);
        uploadTV.setVisibility(View.INVISIBLE);
        if(requestArray.isEmpty()){
            Toast.makeText(getApplicationContext(),getString(R.string.Thereisnorequestsfromaotherusers), Toast.LENGTH_LONG).show();

        }
    }

        @Override
        public void onPause() {
            super.onPause();
            new ExitAndEnterApp().execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "&status=" + "OFF" + "", "sendData");

           // new ExitAndEnterApp().execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "");

        }
        @Override
        public void onStart() {
            super.onStart();
            try {


                new ExitAndEnterApp().execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "");
             //   Toast.makeText(getApplicationContext(), "StartAc", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    @Override
    public void onResume() {
        super.onResume();
        try {


            new ExitAndEnterApp().execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "");
          //  Toast.makeText(getApplicationContext(), "ResAc ", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // get the right tables from db
   class AsyncTaskFriendRequest2  extends AsyncTask<String, String, Void> {
       String checkQery;
       private ProgressDialog progressDialog;
       InputStream is = null;
       String result = "s";
       private Context ctx;
       private String fName = null;
       private String checkP = "no";
       private Accept_FrReq_Ac ac;
       private ArrayList<requestTableInfo> requestInfoArray = new ArrayList<requestTableInfo>();

       public AsyncTaskFriendRequest2(Accept_FrReq_Ac ac) {

           this.ac = ac;
       }


       protected void onPreExecute() {

           progressDialog = new ProgressDialog(ac);
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

               this.progressDialog.dismiss();



           } catch (Exception e) {
               // TODO: handle exception
               Log.e("log_tag", "Error parsing data " + e.toString());
           }
           this.progressDialog.dismiss();
           requestArray = async2.getRequestInfoFromServer();

           Load_user_fromServer();

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

}
