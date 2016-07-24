package org.tatzpiteva.golan.Students_ChatMap_Project.Logic;

/**
 * Created by hanna on 5/15/2016.
 */
public class tblUserFriendInfo {
    private String userName ;
    private String friendWith ;

    public tblUserFriendInfo(String userName, String friendWith) {
        this.userName = userName;
        this.friendWith = friendWith;

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFriendWith() {
        return friendWith;
    }

    public void setFriendWith(String friendWith) {
        this.friendWith = friendWith;
    }
}
