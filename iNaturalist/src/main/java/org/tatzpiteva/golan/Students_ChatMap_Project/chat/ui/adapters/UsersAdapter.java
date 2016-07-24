package org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.loopj.android.http.HttpGet;
import com.quickblox.users.model.QBUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.inaturalist.android.INaturalistApp;
import org.inaturalist.android.R;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.tblUserFriendInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.ResourceUtils;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.SharedPreferencesUtil;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.UiUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ta2er mosa
 */
public class UsersAdapter extends BaseAdapter {

    private AsyncTaskConn2 async ;
    private AsyncTaskGetMyFriends async2;
    private   ArrayList<UserInfo> userInfoArray;
    private ArrayList<tblUserFriendInfo> friendsArray ;
    ViewHolder holder;
   // private ArrayList<tblUserFriendInfo> requestInfoArray = new ArrayList<tblUserFriendInfo>();


    private List<QBUser> dataSource;
    private LayoutInflater inflater;
    private List<QBUser> selected = new ArrayList<QBUser>();
    private Context context;

    public UsersAdapter(List<QBUser> dataSource, Context ctx) {

        this.context=ctx;
        this.dataSource = dataSource;
        this.inflater = LayoutInflater.from(ctx);
        userInfoArray = new ArrayList<UserInfo>();
        friendsArray = new ArrayList<tblUserFriendInfo>();
      //  new AsyncTaskConn2(inflater.getContext()).execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");


    }

    public List<QBUser> getSelected() {
        return selected;
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
     //   userInfoArray = new ArrayList<UserInfo>();
     //   friendsArray = new ArrayList<tblUserFriendInfo>();

//        new AsyncTaskConn2(inflater.getContext()).execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, null);
            holder = new ViewHolder();
            holder.login = (TextView) convertView.findViewById(R.id.userLogin);
            holder.add = (CheckBox) convertView.findViewById(R.id.addCheckBox);
            holder.userImageView = (ImageView) convertView.findViewById(R.id.image_user);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final QBUser user = dataSource.get(position);
        if (user != null) {
            if(user.getLogin().equals(INaturalistApp.USER_LOGIN)){
                holder.add.setEnabled(false);
                holder.login.setText(INaturalistApp.USER_LOGIN + context.getString(R.string.placeholder_username_you));
                holder.login.setTextColor(ResourceUtils.getColor(R.color.text_color_medium_grey));
            //    holder.userImageView()

            }
            else{

                    holder.login.setText(user.getLogin());

                }
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((((CheckBox) v).isChecked())) {
                        selected.add(user);
                    } else {
                        selected.remove(user);
                    }
                }
            });

       /*     if (isUserMe(user)) {
                holder.login.setText(context.getString(R.string.placeholder_username_you, user.getFullName()));
            } else {
                holder.login.setText(user.getFullName());
            }

            if (isAvailableForSelection(user)) {
                holder.login.setTextColor(ResourceUtils.getColor(R.color.text_color_black));
            } else {
                holder.login.setTextColor(ResourceUtils.getColor(R.color.text_color_medium_grey));
            }*/
            holder.add.setChecked(selected.contains(user));
            holder.userImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));

        }
      //  new AsyncTaskConn2(inflater.getContext()).execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");

        return convertView;
    }

    public void LoadUserOnScreen(){

     //   holder.login.setText("yy");
    }


    protected boolean isUserMe(QBUser user) {
//        QBUser currentUser = SharedPreferencesUtil.getQbUser();
        final QBUser currentUser = new QBUser();

        user.setLogin(INaturalistApp.USER_LOGIN);
        user.setPassword(INaturalistApp.USER_PASSWORD);
        return currentUser != null &&
                INaturalistApp.USER_LOGIN.equals(user.getLogin())&&
                INaturalistApp.USER_PASSWORD.equals(user.getPassword());
    }

    protected boolean isAvailableForSelection(QBUser user) {
//        QBUser currentUser = SharedPreferencesUtil.getQbUser();
        final QBUser currentUser = new QBUser();

        user.setLogin(INaturalistApp.USER_LOGIN);
        user.setPassword(INaturalistApp.USER_PASSWORD);
        return currentUser == null || !INaturalistApp.USER_LOGIN.equals(user.getLogin());
    }

    public List<QBUser> getSelectedUsers() {
        return selected;
    }
    private static class ViewHolder {
        TextView login;
        CheckBox add;
        ImageView userImageView;
    }




    // get the right tables from db and put them in the right arrays
    class AsyncTaskConn2 extends AsyncTask<String, String, Void>
    {
        String checkQery ;
        InputStream is = null ;
        String result = "";
        private ProgressDialog progressDialog ;

        private Context ctx ;
        private String fName = null ;
        private String checkP = "no" ;
        public AsyncTaskConn2(Context ctx) {

            this.ctx = ctx ;
        }


        protected void onPreExecute() {

            progressDialog = new ProgressDialog(inflater.getContext());
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




            //    async5 = new AsyncTaskFriendRequest2(this.ctx);
            //       async5.execute("http://www.chatmaphannataer.com/index.html/getRequestTableChatScreen.php?userName=" + getUserNamepref() + "");
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


    // bring from db the specific tables and update the db ...

    class AsyncTaskGetMyFriends  extends AsyncTask<String, String, Void> {

        InputStream is = null;
        String result = "s";
        private Context ctx ;
        private ProgressDialog progressDialog ;
        private String fName = null;
        private String check = null ;
        private ArrayList<tblUserFriendInfo> requestInfoArray = new ArrayList<tblUserFriendInfo>();

        public  AsyncTaskGetMyFriends(Context ctx) {

            this.ctx = ctx ;
        }


        protected void onPreExecute() {

            progressDialog = new ProgressDialog(inflater.getContext());
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

                    friendsArray.add(new tblUserFriendInfo(userName, userSendR));

                }


            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }
            this.progressDialog.dismiss();

     //         Toast.makeText(inflater.getContext(), userInfoArray.get(0).getUserName(), Toast.LENGTH_LONG).show();
      //      Toast.makeText(inflater.getContext(), friendsArray.get(0).getUserName(), Toast.LENGTH_LONG).show();





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

    public String getUserNamepref() {
        SharedPreferences prefs = inflater.getContext().getSharedPreferences("iNaturalistPreferences", inflater.getContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }

    // check if the this username friend with me or not
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
}