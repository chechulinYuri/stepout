package main;

/**
 * Created by Yuri on 25.07.2014.
 */
public class User extends Entity {
    public String firstName;
    public String lastName;
    public String photoLink;
    public String phone;
    public String fbId;

    public User(String firstName, String lastName, String phone, String photoLink, String fbId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.photoLink = photoLink;
        this.fbId = fbId;
    }
}
