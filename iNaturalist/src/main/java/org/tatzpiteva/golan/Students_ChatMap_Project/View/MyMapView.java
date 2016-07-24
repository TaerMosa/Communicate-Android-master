package org.tatzpiteva.golan.Students_ChatMap_Project.View;
/**
 * created by Ta2er Mosa
 */
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.loopj.android.http.HttpGet;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.inaturalist.android.ActivityHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.inaturalist.android.ImageUtils;
import org.inaturalist.android.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tatzpiteva.golan.Students_ChatMap_Project.ConnectionServer.AsyncTaskConn;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.GoogleLocationHolder;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.GoogleLocationListener;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.MyItem;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.UserInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.Logic.tblUserFriendInfo;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities.ChatActivity;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities.DialogsActivity;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities.NewDialogActivity;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.adapters.UsersAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;


public class MyMapView extends Fragment implements GoogleLocationListener {

    private Context ctx = null;
    private GoogleMap googlemap;
    private Marker currentLocationMarker = null;
    private MapView mMapView = null;
    private static int mode = GoogleMap.MAP_TYPE_NORMAL;
    private ImageButton target = null;
    private ImageButton chat_icon = null;
    private ImageButton unreadCountMsg = null;
    private TextView unreadCounterText = null;

    private LatLng myLocation = null;
    private float defaultZoom = 17;
    private GroundOverlay facilityGroundOverlay = null;
    private Polyline path = null;
    private ClusterManager<MyItem> mClusterManager;
    private View InfoWindowView;
    private View SideMenuView ;
    private boolean check = false ;
    private String userName  ;
    private String checkResult  ;
    private ArrayList<UserInfo> userInfoArray ;
    private ArrayList<UserInfo> userInfoArrayAfterRefresh ;
    private ArrayList<UserInfo> userSpecificArray ;
    private ArrayList<tblUserFriendInfo> friendsArray ;
    private View v;
    private String userIconUrl ;

    private final LatLng Location_Naharya = new LatLng(33.009241, 35.098695);
    private UsersAdapter usersAdapter;
    private AsyncTaskConn async ;
    private AsyncTaskConn async2 ;
    private AsyncTaskConn async3 ;
    private AsyncTaskConn async4 ;
    private AsyncTaskConn async5 ;
    private AsyncTaskConnInsideFrag async7 ;
    private AsyncTaskGetMyFriends async6 ;
    int count ;
    private ActivityHelper mHelper;
    private Communicate cm ;
    private String observation ;
    private int sendToServerEveryTwomins = 0  ;
    MyItem offsetItem ;
    ArrayList<MyItem> m ;
    private String str = "noChoose" ;
    private OnResumeOnPause asyncResPau ;
    private int justOnce = 0 ;






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ctx = this.getActivity();
        this.userInfoArrayAfterRefresh = new ArrayList<UserInfo>();
        userSpecificArray = new ArrayList<UserInfo>();
        this.userInfoArray = new ArrayList<UserInfo>();
        userName = getUserNamepref();
        async = new AsyncTaskConn(ctx);
        mHelper = new ActivityHelper(ctx);
        cm = (Communicate) getActivity();
        //Load the pic
        SharedPreferences prefs = ctx.getSharedPreferences("iNaturalistPreferences", ctx.MODE_PRIVATE);
        userIconUrl = prefs.getString("user_icon_url", null);

        //check if the user exist or not




        async.execute("http://www.chatmaphannataer.com/index.html/check.php?userName=" + getUserNamepref() + "", "getDataFromUserName");

        v = inflater.inflate(R.layout.activity_my_map_view, container, false);

        chat_icon = (ImageButton) v.findViewById(R.id.chat);
        unreadCounterText = (TextView) v.findViewById(R.id.unreadCounterText);
        unreadCountMsg = (ImageButton) v.findViewById(R.id.unreadCounterMsg);
        unreadCounterText.setVisibility(View.INVISIBLE);
        unreadCountMsg.setVisibility(View.INVISIBLE);
        chat_icon.setVisibility(View.INVISIBLE);
        chat_icon.setOnClickListener(chatRoom_btnlistener);

        unreadCountMsg.setOnClickListener(ToDialogRoom_btnlistener);

        LayoutInflater Linflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        InfoWindowView = getLayoutInflater(Bundle.EMPTY).inflate(R.layout.communi_info_window_map, null);
        SideMenuView = getLayoutInflater(Bundle.EMPTY).inflate(R.layout.side_menu, null);
        getSS();


        this.init(savedInstanceState);

        asyncResPau = new OnResumeOnPause(ctx);
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "","sendData");

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
      //  Toast.makeText(getApplicationContext(),"OnDetach", Toast.LENGTH_SHORT).show();

    }

    public void init(Bundle savedInstanceState) {

        try {
            MapsInitializer.initialize(ctx);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        mMapView = (MapView) v.findViewById(R.id.googleMap);
        mMapView.onCreate(savedInstanceState);


        googlemap = mMapView.getMap();
        googlemap.setMapType(mode);
        googlemap.getUiSettings().setZoomControlsEnabled(true);


        target = (ImageButton) v.findViewById(R.id.target);

        target.setOnClickListener(targetlistener);

        GoogleLocationHolder.getInstance().init(ctx);
        GoogleLocationHolder.getInstance().subscribeForLocation(this);

        // info window init
        googlemap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {

                chat_icon.setVisibility(View.INVISIBLE);
                String checkContact ;
                String[] str ;
                TextView tvTitle = ((TextView) InfoWindowView.findViewById(R.id.userName));

                //take the username from the info window
                str = marker.getTitle().split(" ");
                tvTitle.setText(str[0]+" "+str[1]+" "+str[2]);
                TextView tvSnippet = ((TextView) InfoWindowView.findViewById(R.id.destance));
                tvSnippet.setText(marker.getSnippet());


                //check if the username who contact me ? friends ? nobody ? everyone
                if(!userInfoArray.isEmpty()) {
                    for (int i = 0; i < userInfoArray.size(); i++) {

                        //str[3]  = the username
                        if (userInfoArray.get(i).getUserName().equals(str[3])
                                &&  !userInfoArray.get(i).getUserName().equals(getUserNamepref()) ) {
                            checkContact = userInfoArray.get(i).getWhoContactMe() ;

                            // if the username has put everyone so the contact me btn always apear
                            if(checkContact.equals("Everyone")){

                                chat_icon.setVisibility(View.VISIBLE);
                            }
                            //if the username has put who contact me mycontacts check if the usernames are friends?
                            //if yes so let the btn appear if not dissapear it
                            else if(checkContact.equals("MyContacts")){

                             String ifFriends =   checkIfFriendsOrNot(userInfoArray.get(i).getUserName());
                           //     Toast.makeText(getApplicationContext(), ifFriends, Toast.LENGTH_SHORT).show();
                             if(ifFriends.equals("friends")){
                                 chat_icon.setVisibility(View.VISIBLE);

                             }
                                else{
                                 chat_icon.setVisibility(View.INVISIBLE);

                             }

                            }
                            // if the username put nodody so disspear it
                            else if(checkContact.equals("Nobody")){
                                chat_icon.setVisibility(View.INVISIBLE);
                            }
                              // Toast.makeText(getApplicationContext(), "Load", Toast.LENGTH_SHORT).show();

                            // load the pic and put it in the info window
                            LoadUserImg_URLAfterImp(userInfoArray.get(i).getPic());

                        }
                        else if (str[3].equals(getUserNamepref())){
                            //LoadUserImg_URL();
                            LoadUserImg_URLAfterImp(userIconUrl);
                          //  Toast.makeText(getApplicationContext(),userInfoArray.get(i).getUserName(), Toast.LENGTH_SHORT).show();
                          //  Toast.makeText(getApplicationContext(),getUserNamepref(), Toast.LENGTH_SHORT).show();

                        //    Toast.makeText(getApplicationContext(), "saveme", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
                else{
                 //   LoadUserImg_URL();
                    LoadUserImg_URLAfterImp(userIconUrl);

                }


                return InfoWindowView;
            }

            @Override
            public View getInfoContents(Marker marker) {

                return null;
            }
            public void setUserNameInsideClus(String user){
                userName = user ;
            }


        });



    }


    private void drawPlolyLine() {
        PolylineOptions polyoptions = new PolylineOptions().color(Color.BLUE);

        polyoptions.add(new LatLng(30, 31), new LatLng(32, 33));

        if (polyoptions != null) {
            path = googlemap.addPolyline(polyoptions);
        }
    }

    // target my location listener
    private View.OnClickListener targetlistener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (getCheck() == true) {
                if (myLocation != null) {

                    googlemap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, defaultZoom));

                } else {
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myLocation, defaultZoom);
                    googlemap.animateCamera(update);

                }

            }
            else{
                Toast.makeText(getApplicationContext(),getString(R.string.GPS), Toast.LENGTH_SHORT).show();

            }
        }
    };

    public void refreshTblUser(){
        googlemap.clear();
        BitmapDescriptor myicon = BitmapDescriptorFactory.fromResource(R.drawable.communi_my_marker);
        MarkerOptions markerOptions = new MarkerOptions().position(myLocation)
                .title(getString(R.string.username) + " " + getUserNamepref() +" "+ "Me " +getUserNamepref())
                .snippet(LoadObsrvation() + " " + getString(R.string.observtionsInfoWindow))
                .icon(myicon);

        currentLocationMarker = googlemap.addMarker(markerOptions);
        //call this method to show the markerts with the new location
        setUpClustererAfterRefresh();


    }

    //if the user press the refresh button
    public View.OnClickListener refresh_btnlistener = new View.OnClickListener() {



        @Override
        public void onClick(View v) {


            //remove the old markers when click the refresh
            googlemap.clear();
            BitmapDescriptor myicon = BitmapDescriptorFactory.fromResource(R.drawable.communi_my_marker);
            MarkerOptions markerOptions = new MarkerOptions().position(myLocation)
                    .title(getString(R.string.username) + " " + getUserNamepref() +" "+ "Me " +getUserNamepref())
                    .snippet(LoadObsrvation() + " " + getString(R.string.observtionsInfoWindow))
                    .icon(myicon);

            currentLocationMarker = googlemap.addMarker(markerOptions);
            //call this method to show the markerts with the new location
           setUpClustererAfterRefresh();



        }
    };


    public View.OnClickListener ToDialogRoom_btnlistener= new View.OnClickListener() {



        @Override
        public void onClick(View v) {

            //Go to char Room between the users
            Intent i = new Intent(getActivity(),DialogsActivity.class);
            startActivity(i);
            //   Toast.makeText(getApplicationContext(), getString(R.string.chatRoom), Toast.LENGTH_SHORT).show();



        }
    };

    public View.OnClickListener chatRoom_btnlistener= new View.OnClickListener() {



        @Override
        public void onClick(View v) {

            //Go to char Room between the users
            Intent i = new Intent(getActivity(),DialogsActivity.class);
            startActivity(i);
         //   Toast.makeText(getApplicationContext(), getString(R.string.chatRoom), Toast.LENGTH_SHORT).show();



        }
    };

    public void startSingleChat(QBDialog dialog) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);

        ChatActivity.start(ctx, bundle);
    }


    private String usersListToChatName(){
        String chatName = "";
        for(QBUser user : usersAdapter.getSelected()){
            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getLogin();
        }
        return chatName;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (facilityGroundOverlay != null) {

            facilityGroundOverlay.remove();
            facilityGroundOverlay = null;
        }
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        if (path != null) {
            path.remove();
        }

        mMapView.onDestroy();
    }

    //when the user exit , put the status in db off
    @Override
    public void onPause() {
        super.onPause();

        mMapView.onPause();
        //when the user go out from the app make himm offline
     //   asyncResPau = new OnResumeOnPause(ctx);
     //   asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "", "sendData");

    }
    //when the user exit and getback to the app put the status on in db
    @Override
    public void onResume() {
        super.onResume();

        try {


            mMapView.onResume();
            asyncResPau = new OnResumeOnPause(ctx);
            asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");


            //    asyncResPau = new OnResumeOnPause(ctx);
         //   asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateDataWhenResumeOnAndData.php?userName=" + getUserNamepref() + "&status="+"ON"+"&whoCanSeeMe=" + userSpecificArray.get(0).getWhoCanSeeMe() + "&whatIseeOnMap=" + userSpecificArray.get(0).getWhatIseeOnMap() + "&region=" + userSpecificArray.get(0).getRegion() + "&WhoContactMe=" + userSpecificArray.get(0).getWhoContactMe() + "&location=" + userSpecificArray.get(0).getLocation() + "&FakeRealUserName=" + userSpecificArray.get(0).getFakeRealUserName() + "", "sendData");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
    @Override
    public void onStart() {
        super.onStart();
        try {

            asyncResPau = new OnResumeOnPause(ctx);
              asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "","sendData");

            getSS();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSS() {


        ChatService.getInstance().getDialogs(new QBEntityCallback<ArrayList<QBDialog>>() {
            int sum = 0;

            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle bundle) {
                for (QBDialog d : dialogs) {
                    if (d != null) {
                        sum += d.getUnreadMessageCount();
                    } else {
                        sum = -1;
                    }
                }

                if (sum > 0) {
                    unreadCounterText.setVisibility(View.VISIBLE);
                    unreadCountMsg.setVisibility(View.VISIBLE);
                    unreadCounterText.setText(String.valueOf(sum));

                } else {
                    unreadCounterText.setVisibility(View.INVISIBLE);
                    unreadCountMsg.setVisibility(View.INVISIBLE);
                }


            }

            @Override
            public void onError(QBResponseException errors) {
            }
        });

    }





    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);

    }
    // Location change listener
    @Override
    public void GoogleLocationChange(LatLng GLocation) {

        myLocation = GLocation;
        if (myLocation != null) {
            showBtn(true);
            count++;
            getSS();
            firstEnterToFirstActivity();
            sendToServerEveryTwomins++;
            //  Toast.makeText(getApplicationContext(),Integer.toString(sendToServerEveryTwomins), Toast.LENGTH_SHORT).show();

            if (currentLocationMarker == null) {
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myLocation, defaultZoom);
                googlemap.animateCamera(update);

                BitmapDescriptor myicon = BitmapDescriptorFactory.fromResource(R.drawable.communi_my_marker);
                MarkerOptions markerOptions = new MarkerOptions().position(myLocation)
                        .title(getString(R.string.username) + " " + getUserNamepref()+" "+ "Me " +getUserNamepref())
                        .snippet(LoadObsrvation()+" "+getString(R.string.observtionsInfoWindow))
                        .icon(myicon);

                currentLocationMarker = googlemap.addMarker(markerOptions);


                //the loop call itsself every second so %20 t2rebn one min so update the location
                if(sendToServerEveryTwomins%15==0){
                    async7 = new AsyncTaskConnInsideFrag(ctx);
                    async4 = new AsyncTaskConn(ctx);
                    async6 = new AsyncTaskGetMyFriends(ctx);
                    getSS();

                    async4.execute("http://www.chatmaphannataer.com/index.html/updateLocationEveryTwoMins?userName=" + getUserNamepref() + "&latitudeLocation=" + myLocation.latitude + "&longitudeLocation=" + myLocation.longitude + "", "sendData");
                   // async5.execute("http://www.chatmaphannataer.com/index.html/updateLocationEveryTwoMinsFriendsTable?userName=" + getUserNamepref() + "&latitudeLocation=" + myLocation.latitude + "&longitudeLocation=" + myLocation.longitude + "", "sendData");

                    async7.execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");


                }


                showBtn(true);

            } else {
                getSS();
                currentLocationMarker.setPosition(myLocation);
                //the loop call itsself every second so %20 t2rebn one min so update the location
                if(sendToServerEveryTwomins%15==0){
                    //getSS();
                    async7 = new AsyncTaskConnInsideFrag(ctx);
                    async4 = new AsyncTaskConn(ctx);
                    async6 = new AsyncTaskGetMyFriends(ctx);

                    async4.execute("http://www.chatmaphannataer.com/index.html/updateLocationEveryTwoMins?userName=" + getUserNamepref() + "&latitudeLocation=" + myLocation.latitude + "&longitudeLocation=" + myLocation.longitude + "", "sendData");
                 //   async5.execute("http://www.chatmaphannataer.com/index.html/updateLocationEveryTwoMinsFriendsTable?userName=" + getUserNamepref() + "&latitudeLocation=" + myLocation.latitude + "&longitudeLocation=" + myLocation.longitude + "", "sendData");

                    async7 = new AsyncTaskConnInsideFrag(ctx);
                    //async3 = new AsyncTaskConn(ctx);
                    async7.execute("http://www.chatmaphannataer.com/index.html/getUserInfo.php");








                }
                showBtn(true);


            }


        }


        showBtn(true);
    }
    public void showBtn(boolean t)
    {
        this.check = t  ;

    }
    public boolean getCheck()
    {
        //there is data about the location
        return this.check ;
    }
    public void firstEnterToFirstActivity()
    {    String observation = Integer.toString(LoadObsrvation());

        if(count < 2){

            while(getCheck()==false) {
                Toast.makeText(getApplicationContext(),getString(R.string.WaittoUploadLocation), Toast.LENGTH_SHORT).show();

            }//if the location aploaded
            if(getCheck()==true) {

                // get the result from php file if the user exist or not
                checkResult = async.getResultFromPHP().toString();


                //if the user exist just update the status and location and pic
                if(checkResult.contains("exist"))
                {
                    async4 = new AsyncTaskConn(ctx);
                    async5 = new AsyncTaskConn(ctx) ;
                    async4.execute("http://www.chatmaphannataer.com/index.html/updateUserData_Status_location.php?userName=" + getUserNamepref() + "&latitudeLocation=" + myLocation.latitude + "&longitudeLocation=" + myLocation.longitude +"&status=" + "ON" +"&observation=" +observation+"&pic="+userIconUrl+"", "sendData");
                    async5.execute("http://www.chatmaphannataer.com/index.html/updateFriendsData_Status_location?userName=" + getUserNamepref() + "&latitudeLocation=" + myLocation.latitude + "&longitudeLocation=" + myLocation.longitude +"&status=" + "ON" +"&observation=" +observation+"&pic="+userIconUrl+"", "sendData");



                }
                //if the user doesnt exist add him to tbl user
                else {
                    async3 = new AsyncTaskConn(ctx);
                    async4 = new AsyncTaskConn(ctx);
                    async5 = new AsyncTaskConn(ctx);
                    async3.execute("http://www.chatmaphannataer.com/index.html/addDataToTblUser.php?userName=" +getUserNamepref()+ "&latitudeLocation=" + myLocation.latitude + "&longitudeLocation=" + myLocation.longitude + "&status=" + "ON" + "&whatIseeOnMap=" + "Nobody" + "&whoCanSeeMe=" + "Nobody" + "&WhoContactMe=" + "Nobody" + "&region=" + "EveryWhere" + "&location=" + "fake" + "&FakeRealUserName=" + "Anynm" + "&observation=" + observation + "&pic="+userIconUrl+"", "sendData");
                    async5.execute("http://www.chatmaphannataer.com/index.html/addDataToTblFriends.php?userName=" +getUserNamepref()+ "&latitudeLocation=" + myLocation.latitude + "&longitudeLocation=" + myLocation.longitude + "&status=" + "ON" + "&whatIseeOnMap=" + "Nobody" + "&whoCanSeeMe=" + "Nobody" + "&WhoContactMe=" + "Nobody" + "&region=" + "EveryWhere" + "&location=" + "fake" + "&FakeRealUserName=" + "Anynm" + "&observation=" + observation + "&pic="+userIconUrl+"", "sendData");
                    async4.execute("http://www.chatmaphannataer.com/index.html/updateUserData_Status_location.php?userName=" + getUserNamepref() + "&latitudeLocation=" + myLocation.latitude + "&longitudeLocation=" + myLocation.longitude +"&status=" + "ON" +"&observation=" +observation+"&pic="+userIconUrl+"", "sendData");




                }

            }

        }

    }






    //------------------get\set----------------------------------

    public Context getCtx() {
        return ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    public GoogleMap getGooglemap() {
        return googlemap;
    }

    public void setGooglemap(GoogleMap googlemap) {
        this.googlemap = googlemap;
    }

    public Marker getCurrentLocationMarker() {
        return currentLocationMarker;
    }

    public void setCurrentLocationMarker(Marker currentLocationMarker) {
        this.currentLocationMarker = currentLocationMarker;
    }

    public LatLng getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(LatLng myLocation) {
        this.myLocation = myLocation;
    }

    public void clickNew(View v) {
        Toast.makeText(getActivity(), "Show some text on the screen.", Toast.LENGTH_LONG).show();
    }


    //------------------- get pref user data and destance ---------------------------
    public String getUserNamepref() {
        SharedPreferences prefs = ctx.getSharedPreferences("iNaturalistPreferences", ctx.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }

  /* public void LoadUserImg_URL() {
       ImageView UserPicture = (ImageView) InfoWindowView.findViewById(R.id.user_pic_infowindow);
       ImageView NoUserPicture = (ImageView) InfoWindowView.findViewById(R.id.no_user_pic_infowindow);
    //   SharedPreferences prefs = ctx.getSharedPreferences("iNaturalistPreferences", ctx.MODE_PRIVATE);
     //  userIconUrl = prefs.getString("user_icon_url", null);
       if (userIconUrl != null) {

           UrlImageViewHelper.setUrlDrawable(UserPicture, userIconUrl, new UrlImageViewCallback() {
               ImageView UserPicture = (ImageView) InfoWindowView.findViewById(R.id.user_pic_infowindow);
               ImageView NoUserPicture = (ImageView) InfoWindowView.findViewById(R.id.no_user_pic_infowindow);
               @Override
               public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                   UserPicture.setVisibility(View.VISIBLE);
                   NoUserPicture.setVisibility(View.GONE);
               }

               @Override
               public Bitmap onPreSetBitmap(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                   Bitmap centerCrop = ImageUtils.centerCropBitmap(loadedBitmap);
                   return ImageUtils.getCircleBitmap(centerCrop);
               }
           });

       }  else{
           UserPicture.setVisibility(View.GONE);
           NoUserPicture.setVisibility(View.VISIBLE);
       }

   }*/
    public void LoadUserImg_URLAfterImp(String url) {
        //Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();

        ImageView UserPicture = (ImageView) InfoWindowView.findViewById(R.id.user_pic_infowindow);
        ImageView NoUserPicture = (ImageView) InfoWindowView.findViewById(R.id.no_user_pic_infowindow);
       // userIconUrl = url ;
        if (url != null) {
      //      Toast.makeText(getApplicationContext(), "ii", Toast.LENGTH_SHORT).show();


            UrlImageViewHelper.setUrlDrawable(UserPicture, url, new UrlImageViewCallback() {
                ImageView UserPicture = (ImageView) InfoWindowView.findViewById(R.id.user_pic_infowindow);
                ImageView NoUserPicture = (ImageView) InfoWindowView.findViewById(R.id.no_user_pic_infowindow);

                @Override
                public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                    UserPicture.setVisibility(View.VISIBLE);
                    NoUserPicture.setVisibility(View.GONE);
                }

                @Override
                public Bitmap onPreSetBitmap(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                    Bitmap centerCrop = ImageUtils.centerCropBitmap(loadedBitmap);
                    return ImageUtils.getCircleBitmap(centerCrop);
                }
            });

        }
        else{
            UserPicture.setVisibility(View.GONE);
            NoUserPicture.setVisibility(View.VISIBLE);
        }
    }
    public float getdestance(LatLng start, LatLng end) {
        float[] results = new float[1];
        if(start!=null&&end!=null){
            Location.distanceBetween(start.latitude, start.longitude,
                    end.latitude, end.longitude, results);
            return results[0];}
        return 0;
    }

    public Integer LoadObsrvation(){
        SharedPreferences prefs = ctx.getSharedPreferences("iNaturalistPreferences", ctx.MODE_PRIVATE);
        Integer obsCount = prefs.getInt("observation_count", -1);
        return obsCount;
    }

    //---------------------------Cluster Manager ----------------------
    public void setUpClusterer(String str) {

         String anynmName = "$$$$" ;

        double fakeLocation = 0.01 ;
        double fakeLocationLong = 0.0012 ;
        this.str = str ;

        // when the user press appllt in the control view the setUpClusterer will activate and will clean the old markers
        CleanTheMap();

        mClusterManager = new ClusterManager<MyItem>(ctx, getGooglemap());

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        getGooglemap().setOnCameraChangeListener(mClusterManager);
        getGooglemap().setOnMarkerClickListener(mClusterManager);

        BitmapDescriptor evrey_on_icon = BitmapDescriptorFactory.fromResource(R.drawable.communi_everyone_marker);
        BitmapDescriptor my_conact_icon = BitmapDescriptorFactory.fromResource(R.drawable.communi_mycontact_marker);






        // if the user check what i see on the map everyone
        if(str.equals("EveryOne")) {
            // take the user table from the db and friends table
            this.userInfoArray = cm.getUserArray() ;
            this.userSpecificArray = cm.getSpecificUserInThisApp();
            this.friendsArray = cm.getFriendArray() ;

            //show the markers by the conditions the users choose
            for (int i = 0; i < this.userInfoArray.size(); i++) {
                if (this.userInfoArray.get(i).getLatitudeLocation() != null && this.userInfoArray.get(i).getLongitudeLocation() != null
                        && !this.userInfoArray.get(i).getUserName().equals(getUserNamepref())
                        && (this.userInfoArray.get(i).getWhoCanSeeMe().equals("Everyone")
                        /*|| this.userInfoArray.get(i).getWhoCanSeeMe().equals("MyContacts")*/)
                        && this.userInfoArray.get(i).getStatus().equals("ON")) {


                    double latitude = Double.parseDouble(userInfoArray.get(i).getLatitudeLocation());
                    double longitude = Double.parseDouble(userInfoArray.get(i).getLongitudeLocation());
                    LatLng MyUserLocation = new LatLng(latitude, longitude);
                    // check if the users friends or not

                    String checkIfFriend ;
                   checkIfFriend = checkIfFriendsOrNot(this.userInfoArray.get(i).getUserName());

                  //  Toast.makeText(getActivity(),checkIfFriend, Toast.LENGTH_LONG).show();
                  //  Toast.makeText(getActivity(),this.userInfoArray.get(i).getUserName(), Toast.LENGTH_LONG).show();

                    if(checkIfFriend.equals("friends")){

                        if(userInfoArray.get(i).getLocation().contains("Real")){

                            if(userInfoArray.get(i).getFakeRealUserName().contains("Real")){

                                MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + this.userInfoArray.get(i).getUserName() + " "+ "Friend "+ this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(my_conact_icon);

                                addItems(contactmarkerOptions);



                            }
                            else if(userInfoArray.get(i).getFakeRealUserName().contains("Anynm")){

                                MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + anynmName +" "+ "Friend "+ this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(my_conact_icon);
                                addItems(contactmarkerOptions);


                            }

                        }
                        else if(userInfoArray.get(i).getLocation().contains("Fake")){

                            if(userInfoArray.get(i).getFakeRealUserName().contains("Real")){

                                 MyUserLocation = new LatLng(latitude+fakeLocation, longitude+fakeLocationLong);
                                MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + this.userInfoArray.get(i).getUserName() +" "+ "Friend "+ this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(my_conact_icon);
                                addItems(contactmarkerOptions);


                            }
                            else if(userInfoArray.get(i).getFakeRealUserName().contains("Anynm")){
                                 MyUserLocation = new LatLng(latitude+fakeLocation, longitude+fakeLocationLong);
                                MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + anynmName +" "+ "Friend " +this.userInfoArray.get(i).getUserName()+" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(my_conact_icon);
                                addItems(contactmarkerOptions);

                            }

                        }

                    }
                    else{

                        if(userInfoArray.get(i).getLocation().contains("Real")){

                            if(userInfoArray.get(i).getFakeRealUserName().contains("Real")){
                                MarkerOptions publicmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + this.userInfoArray.get(i).getUserName() + " " + "Public "+ this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(evrey_on_icon);
                                addItems(publicmarkerOptions);


                            }
                            else if(userInfoArray.get(i).getFakeRealUserName().contains("Anynm")){
                                MarkerOptions publicmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + anynmName + " " + "Public "+ this.userInfoArray.get(i).getUserName()+" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(evrey_on_icon);
                                addItems(publicmarkerOptions);


                            }


                        }
                        else if(userInfoArray.get(i).getLocation().contains("Fake")){

                            if(userInfoArray.get(i).getFakeRealUserName().contains("Real")){

                                MyUserLocation = new LatLng(latitude + fakeLocation, longitude + fakeLocationLong);

                                MarkerOptions publicmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + this.userInfoArray.get(i).getUserName() + " " + "public " + this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(evrey_on_icon);
                                addItems(publicmarkerOptions);

                            }
                            else if(userInfoArray.get(i).getFakeRealUserName().contains("Anynm")){

                                MyUserLocation = new LatLng(latitude + fakeLocation, longitude + fakeLocationLong);

                                MarkerOptions publicmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + anynmName + " " + "public "+ this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(evrey_on_icon);
                                addItems(publicmarkerOptions);

                            }

                        }
                    }


                }
                else if (userInfoArray.get(i).getWhoCanSeeMe().contains("MyContacts")
                        && this.userInfoArray.get(i).getStatus().equals("ON")){


                    double latitude = Double.parseDouble(userInfoArray.get(i).getLatitudeLocation());
                    double longitude = Double.parseDouble(userInfoArray.get(i).getLongitudeLocation());
                    LatLng MyUserLocation = new LatLng(latitude, longitude);

                    String checkIfFriend ;
                    checkIfFriend = checkIfFriendsOrNot(this.userInfoArray.get(i).getUserName());

                    if(checkIfFriend.equals("friends")){

                        if(userInfoArray.get(i).getLocation().contains("Real")){

                            if(userInfoArray.get(i).getFakeRealUserName().contains("Real")){

                                MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + this.userInfoArray.get(i).getUserName() +" "+ "Friend "+ this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(my_conact_icon);
                                addItems(contactmarkerOptions);



                            }
                            else if(userInfoArray.get(i).getFakeRealUserName().contains("Anynm")){

                                MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + anynmName +" "+ "Friend "+ this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(my_conact_icon);
                                addItems(contactmarkerOptions);


                            }

                        }
                        else if(userInfoArray.get(i).getLocation().contains("Fake")){

                            if(userInfoArray.get(i).getFakeRealUserName().contains("Real")){

                                MyUserLocation = new LatLng(latitude+fakeLocation, longitude+fakeLocationLong);
                                MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + this.userInfoArray.get(i).getUserName() +" "+ "Friend "+ this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(my_conact_icon);
                                addItems(contactmarkerOptions);


                            }
                            else if(userInfoArray.get(i).getFakeRealUserName().contains("Anynm")){
                                MyUserLocation = new LatLng(latitude+fakeLocation, longitude+fakeLocationLong);
                                MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                        .title(getString(R.string.username) + " " + anynmName +" "+"Friend "+ this.userInfoArray.get(i).getUserName() +" ")
                                        .snippet(checkObservation(userInfoArray.get(i).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                        .icon(my_conact_icon);
                                addItems(contactmarkerOptions);

                            }

                        }

                    }



                }


            }
        }
        else if(str.equals("Friend")) {
            this.userInfoArray = cm.getFriendArrayWithPic();
            this.userSpecificArray = cm.getSpecificUserInThisApp();
            this.friendsArray = cm.getFriendArray();

            for (int i = 0; i < friendsArray.size(); i++) {


                if (friendsArray.get(i).getUserName().equals(getUserNamepref())) {

                    for (int j = 0; j < userInfoArray.size(); j++) {

                        if (userInfoArray.get(j).getUserName().equals(friendsArray.get(i).getFriendWith())) {

                            if (userInfoArray.get(j).getStatus().contains("ON")
                                    &&(userInfoArray.get(j).getWhoCanSeeMe().contains("Everyone")
                                    || userInfoArray.get(j).getWhoCanSeeMe().contains("MyContacts")) ) {


                                double latitude = Double.parseDouble(userInfoArray.get(j).getLatitudeLocation());
                                double longitude = Double.parseDouble(userInfoArray.get(j).getLongitudeLocation());

                                if(userInfoArray.get(j).getLocation().contains("Fake"))
                                {

                                    if(userInfoArray.get(j).getFakeRealUserName().contains("Anynm")){
                                        LatLng MyUserLocation = new LatLng(latitude+fakeLocation, longitude+fakeLocationLong);
                                        MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                                .title(getString(R.string.username) + " " + anynmName + " "+ "Friend "+ this.userInfoArray.get(j).getUserName() +" ")
                                                .snippet(checkObservation(userInfoArray.get(j).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                                .icon(my_conact_icon);
                                        addItems(contactmarkerOptions);
                                    }
                                    else if (userInfoArray.get(j).getFakeRealUserName().contains("Real"))
                                    {
                                        LatLng MyUserLocation = new LatLng(latitude+fakeLocation, longitude+fakeLocationLong);
                                        MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                                .title(getString(R.string.username) + " " + this.userInfoArray.get(j).getUserName() +" "+ "Friend "+ this.userInfoArray.get(j).getUserName() +" ")
                                                .snippet(checkObservation(userInfoArray.get(j).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                                .icon(my_conact_icon);
                                        addItems(contactmarkerOptions);
                                    }

                                }
                                else if (userInfoArray.get(j).getLocation().contains("Real"))
                                {
                                    if(userInfoArray.get(j).getFakeRealUserName().contains("Real")){
                                        LatLng MyUserLocation = new LatLng(latitude, longitude);
                                        MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                                .title(getString(R.string.username) + " " + this.userInfoArray.get(j).getUserName() +" "+ "Friend "+ this.userInfoArray.get(j).getUserName() +" ")
                                                .snippet(checkObservation(userInfoArray.get(j).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                                .icon(my_conact_icon);
                                        addItems(contactmarkerOptions);
                                    }
                                    else if(userInfoArray.get(j).getFakeRealUserName().contains("Anynm")){
                                        LatLng MyUserLocation = new LatLng(latitude, longitude);
                                        MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                                .title(getString(R.string.username) + " " + anynmName + " "+"Friend "+ this.userInfoArray.get(j).getUserName() +" ")
                                                .snippet(checkObservation(userInfoArray.get(j).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                                .icon(my_conact_icon);
                                        addItems(contactmarkerOptions);
                                    }

                                }


                            }

                        }
                    }


                }
                else if(friendsArray.get(i).getFriendWith().equals(getUserNamepref())){
                    for (int j = 0; j < userInfoArray.size(); j++) {

                        if(userInfoArray.get(j).getUserName().equals(friendsArray.get(i).getUserName())) {

                            if (userInfoArray.get(j).getStatus().contains("ON")
                                    &&(userInfoArray.get(j).getWhoCanSeeMe().contains("Everyone")
                                     || userInfoArray.get(j).getWhoCanSeeMe().contains("MyContacts")) ) {

                                double latitude = Double.parseDouble(userInfoArray.get(j).getLatitudeLocation());
                                double longitude = Double.parseDouble(userInfoArray.get(j).getLongitudeLocation());

                                if(userInfoArray.get(j).getLocation().contains("Fake"))
                                {
                                    if(userInfoArray.get(j).getFakeRealUserName().contains("Anynm")){
                                        LatLng MyUserLocation = new LatLng(latitude+fakeLocation, longitude+fakeLocationLong);
                                        MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                                .title(getString(R.string.username) + " " + anynmName +" "+ "Friend "+this.userInfoArray.get(j).getUserName() +" ")
                                                .snippet(checkObservation(userInfoArray.get(j).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                                .icon(my_conact_icon);
                                        addItems(contactmarkerOptions);
                                    }
                                    else if (userInfoArray.get(j).getFakeRealUserName().contains("Real")){
                                        LatLng MyUserLocation = new LatLng(latitude+fakeLocation, longitude+fakeLocationLong);
                                        MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                                .title(getString(R.string.username) + " " + this.userInfoArray.get(j).getUserName() +" "+"Friend "+ this.userInfoArray.get(j).getUserName() +" ")
                                                .snippet(checkObservation(userInfoArray.get(j).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                                .icon(my_conact_icon);
                                        addItems(contactmarkerOptions);
                                    }


                                }
                                else if(userInfoArray.get(j).getLocation().contains("Real"))
                                {
                                    if(userInfoArray.get(j).getFakeRealUserName().contains("Real")){
                                        LatLng MyUserLocation = new LatLng(latitude, longitude);
                                        MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                                .title(getString(R.string.username) + " " + this.userInfoArray.get(j).getUserName() + " "+"Friend "+ this.userInfoArray.get(j).getUserName() +" ")
                                                .snippet(checkObservation(userInfoArray.get(j).getObservation()) + " " + getString(R.string.observtionsInfoWindow))
                                                .icon(my_conact_icon);
                                        addItems(contactmarkerOptions);
                                    }
                                    else if(userInfoArray.get(j).getFakeRealUserName().contains("Anynm"))
                                    {
                                        LatLng MyUserLocation = new LatLng(latitude, longitude);
                                        MarkerOptions contactmarkerOptions = new MarkerOptions().position(MyUserLocation)
                                                .title(getString(R.string.username) + " " + anynmName + " "+"Friend "+ this.userInfoArray.get(j).getUserName() +" ")
                                                .snippet(userInfoArray.get(j).getObservation() + " " + getString(R.string.observtionsInfoWindow))
                                                .icon(my_conact_icon);
                                        addItems(contactmarkerOptions);
                                    }

                                }


                            }
                        }
                    }


                }
            }
        }
        else if(str.equals("Nobody")){

            CleanTheMap();
        }
        else if(str.equals("noChoose")){

        }



        mClusterManager.setRenderer(new OwnIconRendered(ctx, getGooglemap(), mClusterManager));



    }
    public void setUpClustererAfterRefresh() {

        setUpClusterer(this.str);


    }

    // if the observation < 0 show it on the info window 0
    public String checkObservation(String obs ){
        int obs1 = Integer.parseInt(obs);
        if(obs1<0){
            obs1 = 0 ;

            return String.valueOf(obs1) ;
        }
        return String.valueOf(obs1) ;

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

    // add the marker on the map
    private void addItems(MarkerOptions markerOptions) {

        offsetItem = new MyItem(markerOptions.getPosition().latitude,
                markerOptions.getPosition().longitude, markerOptions.getTitle(), markerOptions.getSnippet(), markerOptions.getIcon());
        mClusterManager.addItem(offsetItem);


    }
    public void removeItem(MyItem myItem){
        m = new ArrayList<MyItem>();
        m.add(myItem);

    }

    class OwnIconRendered extends DefaultClusterRenderer<MyItem> {

        public OwnIconRendered(Context context, GoogleMap map,
                               ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(item.getIcon());
            markerOptions.snippet(item.getSnippet());
            markerOptions.title(item.getTitle());
            super.onBeforeClusterItemRendered(item, markerOptions);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<MyItem> cluster) {
            //start clustering if at least 2 items overlap
            return cluster.getSize() > 1;
        }
    }

    public void CleanTheMap(){
        googlemap.clear();
        BitmapDescriptor myicon = BitmapDescriptorFactory.fromResource(R.drawable.communi_my_marker);
        MarkerOptions markerOptions = new MarkerOptions().position(myLocation)
                .title(getString(R.string.username) + " " + getUserNamepref()+" "+ "Me " +getUserNamepref())
                .snippet(LoadObsrvation()+" "+getString(R.string.observtionsInfoWindow))
                .icon(myicon);

        currentLocationMarker = googlemap.addMarker(markerOptions);
     //   setUpClusterer();
        //Make the Button Gone

    }


    // the connection with the db to get the specific tables
    class AsyncTaskGetMyFriends  extends AsyncTask<String, String, Void> {
        String checkQery;
        private ProgressDialog progressDialog;
        InputStream is = null;
        String result = "s";
        private Context ctx;
        private String fName = null;
        private String checkP = "no";

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
                    //fName = Jasonobject.getString("userName");
                    String userName = Jasonobject.getString("userName");
                    String userSendR = Jasonobject.getString("friendWith");

                    requestInfoArray.add(new tblUserFriendInfo(userName, userSendR));

                }





            } catch (Exception e) {
                // TODO: handle exception
                Log.e("log_tag", "Error parsing data " + e.toString());
            }

            this.progressDialog.dismiss();
            //friendsArray = async6.getRequestInfoFromServer();
            userInfoArray = async7.getUserInfoFromServer();
            friendsArray = async6.getRequestInfoFromServer();
            //send to communicate class the userArray
            cm.getUserArray(userInfoArray);
            cm.getFriendArrayWithPic(userInfoArray);
            cm.getFriendArray(friendsArray);
            Toast.makeText(getApplicationContext(),"UpdateSuccess", Toast.LENGTH_SHORT).show();
            refreshTblUser();


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


    class AsyncTaskConnInsideFrag extends AsyncTask<String, String, Void>
    {

        private ProgressDialog progressDialog ;
        InputStream is = null ;
        String result = "";
        private Context ctx ;
        private String checkP = "no" ;
        private ArrayList<UserInfo> userInfoArray = new ArrayList<UserInfo>();
        public AsyncTaskConnInsideFrag(Context ctx) {

            this.ctx = ctx ;
        }



        protected void onPreExecute() {

            progressDialog = new ProgressDialog(ctx);
            progressDialog.setMessage("Fetching data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    AsyncTaskConnInsideFrag.this.cancel(true);
                }
            });
        }
        @Override
        protected Void doInBackground(String... params) {
            String url_select = params[0];



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
                    async6 = new AsyncTaskGetMyFriends(this.ctx);
                    async6.execute("http://www.chatmaphannataer.com/index.html/getUserFriendsList.php?userName=" + getUserNamepref() + "");


                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("log_tag", "Error parsing data " + e.toString());
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

        public ArrayList getSpeceficUser(ArrayList<UserInfo> array){
            return array ;

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