package model;

/**
 * Created by Yuri on 25.07.2014.
 */
public class User extends Entity {
    private String firstName;
    private String lastName;
    private String photoLink;
    private String phone;
    private String fbId;

    public User(String firstName, String lastName, String phone, String photoLink, String fbId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.photoLink = photoLink;
        this.fbId = fbId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhotoLink() {
        return photoLink;
    }

    public String getPhone() {
        return phone;
    }

    public String getFbId() {
        return fbId;
    }
}
