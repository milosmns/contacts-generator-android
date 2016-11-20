
package me.angrybyte.contactsgenerator.parser.data;

import android.graphics.Bitmap;
import android.util.Log;

import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.api.Operations;

/**
 * A data class holding all the relevant info about the user needed by our app.
 */
public class Person {

    private static final String NEW_LINE = "\n";

    // TODO: These two fields can't be populated when we pull data from the server and make a contact in memory
    // We should probably make a separate data structure, that we will use for deleting
    private int id;
    private String lookupUri;

    private String gender;
    private String title;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String imageUrl;
    private String thumbImageUrl;
    private Bitmap image;

    public Person() {
        super();
    }

    public Person(Person otherPerson) {
        // @formatter:off
        this(otherPerson.getGender(), otherPerson.getTitle(), otherPerson.getFirstName(),
             otherPerson.getLastName(), otherPerson.getEmail(), otherPerson.getPhone(),
             otherPerson.getImageUrl(), otherPerson.getThumbImageUrl(), otherPerson.getImage());
        // @formatter:on
    }

    // @formatter:off
    public Person(String gender, String title, String firstName,
                  String lastName, String email, String phone,
                  String imageUrl, String thumbUrl, Bitmap image) {
    // @formatter:on
        super();
        this.gender = gender;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.thumbImageUrl = thumbUrl;
        this.image = image;
    }

    public String getGender() {
        return gender;
    }

    /**
     * @return A {@link Gender} constant, defaults to {@link Operations#MALE} if {@code null} or invalid
     */
    @Gender
    public String getAppGender() {
        if (gender == null) {
            Log.e("ERROR", "No gender here: " + getDisplayName());
            return Operations.MALE;
        }

        if (gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("m")) {
            return Operations.MALE;
        } else if (gender.equalsIgnoreCase("female") || gender.equalsIgnoreCase("f")) {
            return Operations.FEMALE;
        } else {
            Log.e("ERROR", "What gender is this? " + gender + ": " + getDisplayName());
            return Operations.MALE;
        }
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbImageUrl() {
        return thumbImageUrl;
    }

    public void setThumbImageUrl(String thumbImageUrl) {
        this.thumbImageUrl = thumbImageUrl;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getDisplayName() {
        return getFirstName() + " " + getLastName();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setLookupUri(String lookupUri) {
        this.lookupUri = lookupUri;
    }

    public String getLookupUri() {
        return lookupUri;
    }

    @Override
    public String toString() {
        // @formatter:off
        return "Gender: " + getGender() + NEW_LINE +
                "Title: " + getTitle() + NEW_LINE +
                "First name: " + getFirstName() + NEW_LINE +
                "Last name: " + getLastName() + NEW_LINE +
                "Email: " + getEmail() + NEW_LINE +
                "Phone: " + getPhone(); // etc.
        // @formatter:on
    }

}
