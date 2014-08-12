package com.stepout.main.models;

/**
 * Created by Yuri on 25.07.2014.
 */
public class User extends Entity {
    //To get profile link: https://www.facebook.com/app_scoped_user_id/xxxxxxxxx/ wherer xxxxxxxxx is user id
    private String firstName;
    private String lastName;
    private String phone;
    private String fbId;

    public User(String firstName, String lastName, String phone,String fbId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.fbId = fbId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getFbId() {
        return fbId;
    }
}
