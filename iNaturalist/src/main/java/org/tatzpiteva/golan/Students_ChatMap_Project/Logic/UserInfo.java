package org.tatzpiteva.golan.Students_ChatMap_Project.Logic;

/**
 * Created by hanna on 4/11/2016.
 */
public class UserInfo {
    private String userName ;
    private String latitudeLocation;
    private String longitudeLocation ;
    private String status ;
    private String whatIseeOnMap ;
    private String whoCanSeeMe ;
    private String WhoContactMe ;
    private String region ;
    private String location ;
    private String FakeRealUserName ;
    private String observation ;
    private String pic ;

    public UserInfo()
    {
        super();
    }


    public UserInfo(String userName ,String latitudeLocation ,String longitudeLocation,String status
            ,String whatIseeOnMap
            , String whoCanSeeMe
            ,String WhoContactMe
            , String region
            , String location
            , String FakeRealUserName
            ,String observation
            ,String pic)
    {
        this.userName = userName ;
        this.userName = userName ;
        this.latitudeLocation = latitudeLocation;
        this.longitudeLocation = longitudeLocation;
        this.status = status;
        this.whatIseeOnMap = whatIseeOnMap;
        this.whoCanSeeMe = whoCanSeeMe;
        this.WhoContactMe = WhoContactMe;
        this.region = region ;
        this.location = location ;
        this.FakeRealUserName = FakeRealUserName ;
        this.observation = observation;
        this.pic = pic ;

    }

    //getter and setter
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLatitudeLocation() {
        return latitudeLocation;
    }

    public void setLatitudeLocation(String latitudeLocation) {
        this.latitudeLocation = latitudeLocation;
    }

    public String getLongitudeLocation() {
        return longitudeLocation;
    }

    public void setLongitudeLocation(String longitudeLocation) {
        this.longitudeLocation = longitudeLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWhatIseeOnMap() {
        return whatIseeOnMap;
    }

    public void setWhatIseeOnMap(String whatIseeOnMap) {
        this.whatIseeOnMap = whatIseeOnMap;
    }

    public String getWhoCanSeeMe() {
        return whoCanSeeMe;
    }

    public void setWhoCanSeeMe(String whoCanSeeMe) {
        this.whoCanSeeMe = whoCanSeeMe;
    }

    public String getWhoContactMe() {
        return WhoContactMe;
    }

    public void setWhoContactMe(String whoContactMe) {
        WhoContactMe = whoContactMe;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFakeRealUserName() {
        return FakeRealUserName;
    }

    public void setFakeRealUserName(String fakeRealUserName) {
        FakeRealUserName = fakeRealUserName;
    }
    public String getObservation(){
        return observation;
    }
    public void setObservation(String observation){
        this.observation = observation ;
    }

    public String getPic(){
        return this.pic;
    }
    public void setPic(String img){
        this.pic = pic ;
    }

}
