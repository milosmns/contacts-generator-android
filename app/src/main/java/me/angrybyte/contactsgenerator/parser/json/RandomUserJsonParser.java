package me.angrybyte.contactsgenerator.parser.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import me.angrybyte.contactsgenerator.parser.data.User;

/**
 * Parses data from <a href="https://www.randomuser.me">RandomUser.me</a> and returns a list of {@link User} objects.
 * <p/>
 * This is the specification:
 * <p/>
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
public class RandomUserJsonParser {
    /**
     * Parses the response from <a href="https://www.randomuser.me">RandomUser.me</a> and returns a list
     * of {@link User} objects, filled with Bitmaps, alongside other contact data.
     *
     * @param response The response received from a query sent to <a href="https://www.randomuser.me">RandomUser.me</a>
     * @return A list of {@link User} objects, suitable for use with ContactPersister
     */
    public List<User> parseResponse(String response) {
        List<User> userList = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response);

        if (element.isJsonObject()) {
            JsonObject root = element.getAsJsonObject();
            JsonArray results = root.getAsJsonArray(JsonConstants.RESULTS);
            for (int i = 0; i < results.size(); i++) {
                JsonObject result = results.get(i).getAsJsonObject();
                userList.add(parseUser(result));
            }
        }

        return userList;
    }

    /**
     * Parses the "user" field found in the JSON response. You can expand this method, alongside the
     * {@link User} class to get more data.
     *
     * @param jsonResult The result of the query previously parsed by {@link JsonParser}
     * @return A {@link User} object filled with the data from #jsonResult
     */
    private User parseUser(JsonObject jsonResult) {
        JsonObject jsonUser = jsonResult.getAsJsonObject(JsonConstants.USER);

        User user = new User();
        String gender = uppercaseFirstLetter(jsonUser.get(JsonConstants.GENDER).getAsString());
        user.setGender(gender);

        parseName(user, jsonUser);

        user.setEmail(jsonUser.get(JsonConstants.EMAIL).getAsString());
        user.setPhone(jsonUser.get(JsonConstants.PHONE).getAsString());

        parsePicture(user, jsonUser);

        return user;
    }

    /**
     * Parses the name field of the JSON response from the server.
     *
     * @param user     The user in which you will store name data
     * @param jsonUser The parsed {@link JsonObject} which contains the "name" field
     */
    private void parseName(User user, JsonObject jsonUser) {
        JsonObject jsonName = jsonUser.getAsJsonObject(JsonConstants.NAME);

        String title = uppercaseFirstLetter(jsonName.get(JsonConstants.NAME_TITLE).getAsString());
        user.setTitle(title);

        String firstName = uppercaseFirstLetter(jsonName.get(JsonConstants.NAME_FIRST).getAsString());
        user.setFirstName(firstName);

        String lastName = uppercaseFirstLetter(jsonName.get(JsonConstants.NAME_LAST).getAsString());
        user.setLastName(lastName);
    }

    /**
     * Puts the links of the pictures provided inside of the JSON response
     *
     * @param user     The {@link User} object to be filled with image URL data
     * @param jsonUser The parsed {@link JsonObject} which contains the "picture" field
     */
    private void parsePicture(User user, JsonObject jsonUser) {
        JsonObject jsonPicture = jsonUser.getAsJsonObject(JsonConstants.PICTURE);

        user.setImageUrl(jsonPicture.get(JsonConstants.PICTURE_LARGE).getAsString());
        user.setThumbImageUrl(jsonPicture.get(JsonConstants.PICTURE_THUMB).getAsString());
    }

    /**
     * Returns the same String that was passed, only with a different case.
     *
     * @param string Any string.
     * @return A string with the same characters, in the same order as the one that was passed, but
     * the returned String has an uppercase first letter, and the rest of the letter lowercase - regardless
     * of the previous case of the String.
     */
    private String uppercaseFirstLetter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
