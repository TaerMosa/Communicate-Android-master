package org.tatzpiteva.golan.Students_ChatMap_Project.ConnectionServer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.HttpGet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.requestTableInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.View.Accept_FrReq_Ac;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by hanna on 5/11/2016.
 */
public class AsyncTaskFriendRequest  extends AsyncTask<String, String, Void>
{
    String checkQery ;
    private ProgressDialog progressDialog ;
    InputStream is = null ;
    String result = "s";
    private Context ctx ;
    private String fName = null ;
    private String checkP = "no" ;
    private Accept_FrReq_Ac ac ;
    private ArrayList<requestTableInfo> requestInfoArray = new ArrayList<requestTableInfo>();
    public AsyncTaskFriendRequest(Accept_FrReq_Ac ac) {

        this.ac = ac ;
    }



    protected void onPreExecute() {

        progressDialog = new ProgressDialog(ac);
        progressDialog.setMessage("Fetching data...");
        progressDialog.show();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                AsyncTaskFriendRequest.this.cancel(true);
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



        return null ;
    }

    protected void onPostExecute(Void v) {



            // ambil data dari Json database
           try {

                JSONArray Jarray = new JSONArray(result);
                for(int i=0;i<Jarray.length();i++)
                {

                    JSONObject Jasonobject = null;
                    Jasonobject = Jarray.getJSONObject(i);

                    //get an output on the screen
                    //fName = Jasonobject.getString("userName");
                    String userName = Jasonobject.getString("userName");
                    String userSendR = Jasonobject.getString("userSendR");
                    String pic = Jasonobject.getString("pic");
                    requestInfoArray.add(new requestTableInfo(userName,userSendR,pic));

                }

                this.progressDialog.dismiss();



            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }
      this.progressDialog.dismiss();

    }

    public ArrayList getRequestInfoFromServer()
    {

        return requestInfoArray ;
    }
    public String getResult()
    {
        return result ;
    }
    public String userName(){
        return fName ;
    }
    public ArrayList getSpeceficUser(ArrayList<UserInfo> array){
        return array ;

    }
}
