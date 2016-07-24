package org.tatzpiteva.golan.Students_ChatMap_Project.chat.core;

/**
 * Created by ta2er mosa
 */
public interface ApplicationSessionStateCallback {
    void onStartSessionRecreation();
    void onFinishSessionRecreation(boolean success);
}
