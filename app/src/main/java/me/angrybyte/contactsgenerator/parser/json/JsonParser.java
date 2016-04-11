
package me.angrybyte.contactsgenerator.parser.json;

import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import me.angrybyte.contactsgenerator.parser.data.Person;

/**
 * Parses data from <a href="https://www.randomuser.me">RandomUser.me</a> and returns a list of {@link Person} objects.
 * <p/>
 * This is the specification:
 * <p/>
 *
 * <pre>
 * {
 * results: [{
 * user: {
 * gender: "female",
 * name: {
 * title: "ms",
 * first: "manuela",
 * last: "velasco"
 * },
 * location: {
 * street: "1969 calle de alberto aguilera",
 * city: "la coruña",
 * state: "asturias",
 * zip: "56298"
 * },
 * email: "manuela.velasco50@example.com",
 * username: "heavybutterfly920",
 * password: "enterprise",
 * salt: ">egEn6YsO",
 * md5: "2dd1894ea9d19bf5479992da95713a3a",
 * sha1: "ba230bc400723f470b68e9609ab7d0e6cf123b59",
 * sha256: "f4f52bf8c5ad7fc759d1d4156b25a4c7b3d1e2eec6c92d80e508aa0b7946d4ba",
 * registered: "1303647245",
 * dob: "415458547",
 * phone: "994-131-106",
 * cell: "626-695-164",
 * DNI: "52434048-I",
 * picture: {
 * large: "http://api.randomuser.me/portraits/women/39.jpg",
 * medium: "http://api.randomuser.me/portraits/med/women/39.jpg",
 * thumbnail: "http://api.randomuser.me/portraits/thumb/women/39.jpg",
 * },
 * version: "0.6"
 * nationality: "ES"
 * },
 * seed: "graywolf"
 * }]
 * }
 * </pre>
 */
public class JsonParser {
    /**
     * Parses the response from <a href="https://www.randomuser.me">RandomUser.me</a> and returns a list of {@link Person} objects, filled
     * with Bitmaps, alongside other contact data.
     *
     * @param response The response received from a query sent to <a href="https://www.randomuser.me">RandomUser.me</a>
     * @return A list of {@link Person} objects, suitable for use with ContactPersister
     */
    @NonNull
    public List<Person> parseResponse(String response) {
        List<Person> personList = new ArrayList<>();

        com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
        JsonElement element = parser.parse(response);

        if (element.isJsonObject()) {
            JsonObject root = element.getAsJsonObject();
            JsonArray results = root.getAsJsonArray(JsonConstants.RESULTS);
            for (int i = 0; i < results.size(); i++) {
                JsonObject result = results.get(i).getAsJsonObject();
                personList.add(parsePerson(result));
            }
        }

        return personList;
    }

    /**
     * Parses the "user" field found in the JSON response. You can expand this method, alongside the {@link Person} class to get more data.
     *
     * @param jsonUser The result of the query previously parsed by {@link com.google.gson.JsonParser}
     * @return A {@link Person} object filled with the data from #jsonResult
     */
    private Person parsePerson(JsonObject jsonUser) {
        Person person = new Person();
        String gender = uppercaseFirstLetter(jsonUser.get(JsonConstants.GENDER).getAsString());
        person.setGender(gender);

        parseName(person, jsonUser);

        person.setEmail(jsonUser.get(JsonConstants.EMAIL).getAsString());
        person.setPhone(jsonUser.get(JsonConstants.PHONE).getAsString());

        parsePicture(person, jsonUser);

        return person;
    }

    /**
     * Parses the name field of the JSON response from the server.
     *
     * @param person The user in which you will store name data
     * @param jsonUser The parsed {@link JsonObject} which contains the "name" field
     */
    private void parseName(Person person, JsonObject jsonUser) {
        JsonObject jsonName = jsonUser.getAsJsonObject(JsonConstants.NAME);

        String title = uppercaseFirstLetter(jsonName.get(JsonConstants.NAME_TITLE).getAsString());
        person.setTitle(title);

        String firstName = uppercaseFirstLetter(jsonName.get(JsonConstants.NAME_FIRST).getAsString());
        person.setFirstName(firstName);

        String lastName = uppercaseFirstLetter(jsonName.get(JsonConstants.NAME_LAST).getAsString());
        person.setLastName(lastName);
    }

    /**
     * Puts the links of the pictures provided inside of the JSON response
     *
     * @param person The {@link Person} object to be filled with image URL data
     * @param jsonUser The parsed {@link JsonObject} which contains the "picture" field
     */
    private void parsePicture(Person person, JsonObject jsonUser) {
        JsonObject jsonPicture = jsonUser.getAsJsonObject(JsonConstants.PICTURE);

        person.setImageUrl(jsonPicture.get(JsonConstants.PICTURE_LARGE).getAsString());
        person.setThumbImageUrl(jsonPicture.get(JsonConstants.PICTURE_THUMB).getAsString());
    }

    /**
     * Returns the same String that was passed, only with a different case.
     *
     * @param string Any string.
     * @return A string with the same characters, in the same order as the one that was passed, but the returned String has an uppercase
     *         first letter, and the rest of the letters lowercase - regardless of the previous case of the String.
     */
    private String uppercaseFirstLetter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
