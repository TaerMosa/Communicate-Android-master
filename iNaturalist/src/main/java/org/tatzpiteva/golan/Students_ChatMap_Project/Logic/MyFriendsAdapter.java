package org.tatzpiteva.golan.Students_ChatMap_Project.Logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.loopj.android.http.HttpGet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.inaturalist.android.ImageUtils;
import org.inaturalist.android.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by hanna on 5/15/2016.
 */
public class MyFriendsAdapter extends ArrayAdapter<RowItem> {
    ArrayList<RowItem> actorList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;
    private Button deletFriend ;
    List<RowItem> rowItems;
    private int position ;
    private Context ctx ;
    private String userIconUrl = null ;


    public MyFriendsAdapter(Context context, int resource, ArrayList<RowItem> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.ctx = context ;
        Resource = resource;
        actorList = objects;
    }


    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        // convert view = design
        this.position = position ;
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);

            //	holder.tvImg = (ImageView) v.findViewById(R.id.ivImage);
            //new DownloadImageTask(holder.imageview).execute(actorList.get(position).getImage());
            holder.tvName = (TextView) v.findViewById(R.id.FriendName);
            holder.pic = (ImageView) v.findViewById(R.id.FriendImg);

            //Sending friend request
            deletFriend = (Button)v.findViewById(R.id.DeletBtn) ;
            deletFriend.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                   new deletFriends().execute("http://www.chatmaphannataer.com/index.html/deletFriend.php?userName="+actorList.get(position).getMember_name()+"&friendWith="+getUserNamepref()+"");
                  //notifyDataSetChanged();
                   // actorList.remove(position);
                    //  new sendReq().execute("http://www.chatmaphannataer.com/index.html/enterDataToRequestTable.php?userName="+actorList.get(position).getMember_name()+"&userSendR="+getUserNamepref()+"&pic="+actorList.get(position).getPic()+"");
                    Toast.makeText(getApplicationContext(), "You delet Friend  " + actorList.get(position).getMember_name() + "", Toast.LENGTH_SHORT).show();
                    actorList.remove(position); //or some other task
                    notifyDataSetChanged();

                    //makeFriend.setPressed(true);
                    //  v.setSelected(true);



                    // Your code that you want to execute on this button click
                }

            });


            v.setTag(holder);
        } else {


            holder = (ViewHolder) v.getTag();

        }

      //  holder.pic.setImageResource(R.drawable.ic_launcher);
        new DownloadImageTask(holder.pic).execute(actorList.get(position).getPic());
        holder.tvName.setText(actorList.get(position).getMember_name());



        //  holder.tvHeight.setText("User Name: " + actorList.get(position).getMember_name());


        return v;

    }

    static class ViewHolder {

        public TextView tvName;
        public ImageView pic ;




    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String  url = null ;


        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            url = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            // bmImage.setImageBitmap(result);
            if(result==null){
                bmImage.setImageResource(R.drawable.usericon);
            }else {
                LoadUserImg_URLAfterImp(url, bmImage);
            }
        }

    }
    public void LoadUserImg_URLAfterImp(String url,ImageView bmImage) {


        userIconUrl = url ;
        if (url != null) {

            UrlImageViewHelper.setUrlDrawable(bmImage, userIconUrl, new UrlImageViewCallback() {

                @Override
                public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                    //  bmImage.setVisibility(View.VISIBLE);


                }

                @Override
                public Bitmap onPreSetBitmap(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                    Bitmap centerCrop = ImageUtils.centerCropBitmap(loadedBitmap);
                    return ImageUtils.getCircleBitmap(centerCrop);
                }
            });

        }

    }

    public String getUserNamepref() {
        SharedPreferences prefs = vi.getContext().getSharedPreferences("iNaturalistPreferences", vi.getContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }

    class deletFriends extends AsyncTask<String, String, String> {
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
