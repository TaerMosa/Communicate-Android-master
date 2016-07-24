package org.tatzpiteva.golan.Students_ChatMap_Project.chat.pushnotifications;



public class GcmPushInstanceIDService extends CoreGcmPushInstanceIDService {
    @Override
    protected String getSenderId() {
        return Consts.PROJECT_NUMBER;
    }
}
