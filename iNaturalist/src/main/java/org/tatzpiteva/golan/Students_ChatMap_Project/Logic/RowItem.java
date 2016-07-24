package org.tatzpiteva.golan.Students_ChatMap_Project.Logic;

import android.widget.Button;

/**
 * Created by ta2er Mosa.
 */
public class RowItem {

    private String member_name;
    private String pic ;


    public RowItem(String member_name , String pic) {

        this.member_name = member_name;
        this.pic = pic ;


    }
    public RowItem() {
        // TODO Auto-generated constructor stub
    }
    public String getMember_name() {
        return member_name;
    }
    public String getPic(){
        return this.pic ;
    }
    public void setPic(String pic){
        this.pic = pic ;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }



}
