package org.tatzpiteva.golan.Students_ChatMap_Project.ConnectionServer;

/**
 * Created by hanna on 3/28/2016.
 */
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.View.MyMapView;

import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.DialogInterface.OnCancelListener;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.View;

import com.loopj.android.http.HttpGet;

public class AsyncTaskConn extends AsyncTask<String, String, Void>
{
	String checkQery ;
	private ProgressDialog progressDialog ;
	InputStream is = null ;
	String result = "";
	private Context ctx ;
	private String fName = null ;
	private String checkP = "no" ;
	private ArrayList<UserInfo> userInfoArray = new ArrayList<UserInfo>();
	public AsyncTaskConn(Context ctx) {

		this.ctx = ctx ;
	}



	protected void onPreExecute() {

		progressDialog = new ProgressDialog(ctx);
		progressDialog.setMessage("Fetching data...");
		progressDialog.show();
		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				AsyncTaskConn.this.cancel(true);
			}
		});
	}
	@Override
	protected Void doInBackground(String... params) {
		String url_select = params[0];
		checkQery = params[1];

		if (checkQery.equals("getData")) {
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
				fName = "nono";



			} catch (Exception e) {
				// TODO: handle exception
				Log.e("log_tag", "Error converting result " + e.toString());
			}

		} else if (checkQery.equals("sendData")) {
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
		else if (checkQery.equals("getDataFromUserName")) {
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
				fName = result;


			} catch (Exception e) {
				// TODO: handle exception
				Log.e("log_tag", "Error converting result " + e.toString());
			}
		}
		return null ;
	}

	protected void onPostExecute(Void v) {


		if(checkQery.equals("getData")) {
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
					userInfoArray.add(new UserInfo(userName,latitudeLocation,longitudeLocation,status,whatIseeOnMap,whoCanSeeMe,WhoContactMe
							,region,location,FakeRealUserName,observation,img));

				}

				this.progressDialog.dismiss();



			} catch (Exception e) {
				// TODO: handle exception
				Log.e("log_tag", "Error parsing data " + e.toString());
			}
		}
		else if(checkQery.equals("sendData"))
		{

			this.progressDialog.dismiss();
			checkP = "yes" ;

		}
		else if(checkQery.equals("getDataFromUserName"))
		{
			this.progressDialog.dismiss();
			checkP = "yes";
		}
	}

	public String getResultFromPHP()
	{
		return result ;
	}public String ifProgressDialogFinish()
{
	return checkP ;
}
	public ArrayList getUserInfoFromServer()
	{

		return userInfoArray ;
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
