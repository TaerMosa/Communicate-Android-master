package org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;

import com.quickblox.users.model.QBUser;

import org.inaturalist.android.INaturalistApp;
import org.inaturalist.android.R;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.ApplicationSingleton;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        //
       final QBUser user = new QBUser();

        user.setLogin(INaturalistApp.USER_LOGIN);
        user.setPassword(INaturalistApp.USER_PASSWORD);


        ChatService.getInstance().login(user, new QBEntityCallback<Void>() {

            @Override
            public void onSuccess(Void result, Bundle bundle) {
                // Go to Dialogs screen
                //
                Intent intent = new Intent(SplashActivity.this, DialogsActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(QBResponseException errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
                dialog.setMessage("chat login errors: " + errors).create().show();
            }
        });
    }



}