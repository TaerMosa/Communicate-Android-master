package org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.HttpGet;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.inaturalist.android.R;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.Chat;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.GroupChatImpl;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.PrivateChatImpl;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.adapters.ChatAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;



public class ChatActivity extends BaseActivity {


    private static final String TAG = ChatActivity.class.getSimpleName();

    public static final String EXTRA_DIALOG = "dialog";
    private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    private OnResumeOnPause asyncResPau ;

    private EditText messageEditText;
    private ListView messagesContainer;
    private Button sendButton;
    private ProgressBar progressBar;
    private ChatAdapter adapter;

    private Chat chat;
    private QBDialog dialog;

    private View stickersFrame;
    private boolean isStickersFrameVisible;
    private ImageView stickerButton;
    private RelativeLayout container;

    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();

        // Init chat if the session is active
        //
        if (isSessionActive()) {
            initChat();
        }

        ChatService.getInstance().addConnectionListener(chatConnectionListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ChatService.getInstance().removeConnectionListener(chatConnectionListener);
    }

    @Override
    public void onBackPressed() {
        if (isStickersFrameVisible) {
            setStickersFrameVisible(false);
            stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else {
            try {
                chat.release();
            } catch (XMPPException e) {
                Log.e(TAG, "failed to release chat", e);
            }
            super.onBackPressed();

            Intent i = new Intent(ChatActivity.this, DialogsActivity.class);
            startActivity(i);
            finish();
        }
    }
    public String getUserNamepref() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("iNaturalistPreferences", getApplicationContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        return username;

    }
    @Override
    public void onStart() {
        super.onStart();
        try {

            asyncResPau = new OnResumeOnPause(ChatActivity.this);
            asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");
//            Toast.makeText(getApplicationContext(), "chatAc", Toast.LENGTH_SHORT).show();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onPause() {
        super.onPause();

        asyncResPau = new OnResumeOnPause(ChatActivity.this);
      //  asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "", "sendData");
        asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserControlWhenExit.php?userName=" + getUserNamepref() + "&status="+"OFF"+"", "sendData");


    }

    public void onSendChatClick(View view){
        String messageText = messageEditText.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        sendChatMessage(messageText);
    }
    private void initViews() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView companionLabel = (TextView) findViewById(R.id.companionLabel);
        ImageView groupview = (ImageView) findViewById(R.id.roomImage);
        ImageView userView =(ImageView) findViewById(R.id.no_user_pic);
        TextView grouplable = (TextView) findViewById(R.id.GroupLabel);
        View lineview = (View) findViewById(R.id.lineview);

        // Setup opponents info
        //
        Intent intent = getIntent();
        dialog = (QBDialog) intent.getSerializableExtra(EXTRA_DIALOG);
        container = (RelativeLayout) findViewById(R.id.container);
        if (dialog.getType() == QBDialogType.GROUP) {
            TextView meLabel = (TextView) findViewById(R.id.meLabel);
         //   meLabel.setVisibility(View.GONE);
            userView.setVisibility(View.GONE);
            companionLabel.setVisibility(View.GONE);
            lineview.setLeft(10);
            grouplable.setVisibility(View.VISIBLE);
            groupview.setVisibility(View.VISIBLE);
        } else if (dialog.getType() == QBDialogType.PRIVATE) {
            Integer opponentID = ChatService.getInstance().getOpponentIDForPrivateDialog(dialog);
            companionLabel.setText(ChatService.getInstance().getDialogsUsers().get(opponentID).getLogin());
        }

        // Send button




                            }

    private void showKeyboard() {
        ((InputMethodManager) messageEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void sendChatMessage(String messageText) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(messageText);
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(new Date().getTime() / 1000);

        try {
            chat.sendMessage(chatMessage);

        } catch (XMPPException e) {
            Log.e(TAG, "failed to send a message", e);
        } catch (SmackException sme) {
            Log.e(TAG, "failed to send a message", sme);
        }

        messageEditText.setText("");

        if (dialog.getType() == QBDialogType.PRIVATE) {
            showMessage(chatMessage);
        }
    }

    public void onKeyboardVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            setStickersFrameVisible(false);
            stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else {
            if (isStickersFrameVisible) {
                stickerButton.setImageResource(R.drawable.ic_action_keyboard);
            } else {
                stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
            }
        }
    }

    private void setStickersFrameVisible(final boolean isVisible) {
        stickersFrame.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        isStickersFrameVisible = isVisible;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {

        } else {

        }
        scrollDown();
    }



    public void setContentBottomPadding(int padding) {
        container.setPadding(0, 0, 0, padding);
    }

    private void initChat() {

        if (dialog.getType() == QBDialogType.GROUP) {
            chat = new GroupChatImpl(this);

            // Join group chat
            //
            progressBar.setVisibility(View.VISIBLE);
            //
            joinGroupChat();

        } else if (dialog.getType() == QBDialogType.PRIVATE) {
            Integer opponentID = ChatService.getInstance().getOpponentIDForPrivateDialog(dialog);

            chat = new PrivateChatImpl(this, opponentID);

            // Load CHat history
            //
            loadChatHistory();
        }
    }

    private void joinGroupChat(){
        ((GroupChatImpl) chat).joinGroupChat(dialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {

                // Load Chat history
                //
                loadChatHistory();
            }

            @Override
            public void onError(QBResponseException list) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                dialog.setMessage("error when join group chat: " + list.toString()).create().show();
            }
        });
    }

    private void loadChatHistory() {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setLimit(100);
        customObjectRequestBuilder.sortDesc("date_sent");

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {

                adapter = new ChatAdapter(ChatActivity.this, new ArrayList<QBChatMessage>());
                messagesContainer.setAdapter(adapter);

                for (int i = messages.size() - 1; i >= 0; --i) {
                    QBChatMessage msg = messages.get(i);
                    showMessage(msg);
                }

                progressBar.setVisibility(View.GONE);
                asyncResPau = new OnResumeOnPause(ChatActivity.this);
                asyncResPau.execute("http://www.chatmaphannataer.com/index.html/updateUserData.php?userName=" + getUserNamepref() + "&status=" + "ON" + "", "sendData");
//            Toast.makeText(getApplicationContext(), "chatAc", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(QBResponseException errors) {
                if (!ChatActivity.this.isFinishing()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                    dialog.setMessage("load chat history errors: " + errors).create().show();
                }
            }
        });
    }

    public void showMessage(QBChatMessage message) {
        adapter.add(message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                scrollDown();
            }
        });
    }

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    ConnectionListener chatConnectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            Log.i(TAG, "connected");
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean authenticated) {
            Log.i(TAG, "authenticated");
        }

        @Override
        public void connectionClosed() {
            Log.i(TAG, "connectionClosed");
        }

        @Override
        public void connectionClosedOnError(final Exception e) {
            Log.i(TAG, "connectionClosedOnError: " + e.getLocalizedMessage());

            // leave active room
            //
            if (dialog.getType() == QBDialogType.GROUP) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((GroupChatImpl) chat).leave();
                    }
                });
            }
        }

        @Override
        public void reconnectingIn(final int seconds) {
            if (seconds % 5 == 0) {
                Log.i(TAG, "reconnectingIn: " + seconds);
            }
        }

        @Override
        public void reconnectionSuccessful() {
            Log.i(TAG, "reconnectionSuccessful");

            // Join active room
            //
            if (dialog.getType() == QBDialogType.GROUP) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinGroupChat();
                    }
                });
            }
        }

        @Override
        public void reconnectionFailed(final Exception error) {
            Log.i(TAG, "reconnectionFailed: " + error.getLocalizedMessage());
        }
    };


    //
    // ApplicationSessionStateCallback
    //

    @Override
    public void onStartSessionRecreation() {

    }

    @Override
    public void onFinishSessionRecreation(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    initChat();
                }
            }
        });
    }

    public class OnResumeOnPause extends AsyncTask<String, String, Void> {
        String checkQery;
        InputStream is = null;
        String result = "";
        private ChatActivity ctx;


        public OnResumeOnPause(ChatActivity ctx) {

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
