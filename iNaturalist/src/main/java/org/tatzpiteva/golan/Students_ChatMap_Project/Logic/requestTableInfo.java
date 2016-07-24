package org.tatzpiteva.golan.Students_ChatMap_Project.Logic;

/**
 * Created by hanna on 5/12/2016.
 */
public class requestTableInfo {
    private String userName ;
    private String userSendR;
    private String pic ;

    public requestTableInfo(String userName, String userSendR, String pic) {
        this.userName = userName;
        this.userSendR = userSendR;
        this.pic = pic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserSendR() {
        return userSendR;
    }

    public void setUserSendR(String userSendR) {
        this.userSendR = userSendR;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
