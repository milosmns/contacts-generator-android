package me.angrybyte.contactsgenerator.api;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite.BriteContentResolver;
import com.squareup.sqlbrite.SqlBrite;
import com.venmo.cursor.IterableCursor;
import com.venmo.cursor.IterableCursorWrapper;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.angrybyte.contactsgenerator.parser.data.Person;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ContactOperations {

    private static final String TAG = ContactOperations.class.getSimpleName();
    private final Context mContext;
    private Cursor mCursor;
    private String mMatchingEmail;
    private ContentResolver mContentResolver;
    private BriteContentResolver mBriteContentResolver;

    //<editor-fold desc="Contact query constants">
    private static final String EXAMPLE_DOMAIN = "@example.com";
    private static final Uri CONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Email.CONTACT_ID,
            ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Email.LOOKUP_KEY
    };
    private static final String SELECTION = ContactsContract.CommonDataKinds.Email.ADDRESS + " LIKE ?";
    private static final String[] SELECTION_ARGS = new String[]{"%" + EXAMPLE_DOMAIN};

    private static final int ID = 0;
    private static final int DISPLAY_NAME = 1;
    private static final int EMAIL = 2;
    private static final int PHOTO_THUMBNAIL = 3;
    private static final int LOOKUP_KEY = 4;
    //</editor-fold>

    public ContactOperations(Context context) {
        // TODO: See if the lifecycle of this object needs to be managed
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        mBriteContentResolver = sqlBrite.wrapContentProvider(mContentResolver, Schedulers.io());
    }

    /**
     * Stores a {@link Person} object to the Android database. Doesn't use accounts, the contact should be stored
     * locally only. <b><font color="#FF3030">Not thread-safe.</font></b>
     *
     * @param person The person to be persisted to the database
     */
    public void storeContact(@NonNull Person person) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        ContentProviderOperation.Builder providerOperation = ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);

        operations.add(providerOperation.build());

        // TODO: Maybe we should insert first name and last name separately
        providerOperation = ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        person.getFirstName() + " " + person.getLastName());

        operations.add(providerOperation.build());

        // Phone
        final List<String> phoneNumbers = Arrays.asList(person.getPhone(), generatePhoneNumber());
        final Random rndPhoneTypeGenerator = new Random();

        for (String phoneNumber : phoneNumbers) {
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                            phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            getPhoneType(rndPhoneTypeGenerator)).build());
        }

        providerOperation = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS,
                        person.getEmail())
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE_HOME);

        if (person.getImage() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            person.getImage().compress(Bitmap.CompressFormat.PNG, 100, stream);

            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,
                            stream.toByteArray()).build());

            try {
                stream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error while flushing stream! Contact picture not added properly.", e);
            }
            close(stream);
        }

        operations.add(providerOperation.build());
        Log.d(TAG, "Creating contact: " + person.getFirstName() + " " + person.getLastName());

        mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
    }

    /**
     * Stores a {@link Person}s list to the Android database. Doesn't use accounts, all contacts from the list should be
     * stored locally only. <b><font color="#FF3030">Not thread-safe.</font></b>
     *
     * @param persons Persons list to be persisted to the database
     */
    public void storeContacts(@NonNull List<Person> persons) throws RemoteException, OperationApplicationException {
        for (int i = 0; i < persons.size(); i++) {
            storeContact(persons.get(i));
        }
    }

    /**
     * Prepares the cursor for the scrubbing (erasing) action. It is <b>mandatory</b> to call this method before calling
     * {@link #getFoundContactsCount()} and {@link #deleteNextContact()}. Failing to do so will result in a crash.
     *
     * @param matchingEmail You can parametrize the search query of the cursor with an email address. Sending {@code
     *                      null} will find all the contacts, while sending in a valid email address will find contacts
     *                      with only that email address associated with them
     * @return {@code True} if the cursor has been opened successfully, {@code false} otherwise
     */
    public boolean prepareCursorForScrubbing(@Nullable String matchingEmail) {
        // create a projection based on the given email
        String[] projection;
        String where;
        String[] whereArgs;
        if (matchingEmail != null) {
            mMatchingEmail = matchingEmail;
            projection = new String[]{
                    ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.CommonDataKinds.Email.ADDRESS
            };

            where = ContactsContract.CommonDataKinds.Email.ADDRESS + " LIKE ?";
            whereArgs = new String[]{
                    "%" + matchingEmail
            };
        } else {
            projection = new String[]{
                    ContactsContract.Contacts.LOOKUP_KEY
            };

            where = null;
            whereArgs = null;
        }

        // try to create a cursor
        mCursor = mContentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                projection,
                where,
                whereArgs,
                null);

        if (mCursor == null) {
            Log.e(TAG, "Cursor not available");
            return false;
        }

        return true;
    }

    /**
     * Retrieve the number of contacts that were found when the query was set up in {@link
     * #prepareCursorForScrubbing(String)}. Do not call this method before calling that one!
     *
     * @return The number of contacts that are available to be deleted in the cursor.
     */
    public int getFoundContactsCount() {
        return mCursor.getCount();
    }

    /**
     * Deletes one single contact from the Cursor that was prepared with {@link #prepareCursorForScrubbing(String)}. Do
     * not call this method before calling that one!
     *
     * @return {@code true} if the contact was deleted successfully, {@code false} otherwise.
     */
    public boolean deleteNextContact() {
        if (mCursor.moveToNext()) {
            int column = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
            boolean delete = mMatchingEmail == null || mCursor.getString(column).contains(mMatchingEmail);
            if (delete) {
                String lookupKey = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                int deletedRows = mContentResolver.delete(uri, null, null);
                if (deletedRows < 1) {
                    Log.w(TAG, "Nothing deleted for URI " + uri);
                    return false;
                }
            }
        } else {
            // close the cursor ignoring the close error, if there are no more contacts to erase
            try {
                mCursor.close();
            } catch (Exception e) {
                Log.w(TAG, "Error closing cursor");
            }
        }

        return true;
    }

    private String generatePhoneNumber() {
        Random rndGenerator = new Random();
        StringBuilder stringBuilder = new StringBuilder();

        while (stringBuilder.length() < 11) {
            stringBuilder.append(rndGenerator.nextInt(9));
        }

        return stringBuilder.toString();
    }

    private int getPhoneType(Random rndPhoneTypeGenerator) {
        switch (rndPhoneTypeGenerator.nextInt(2) + 1) {

            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                return ContactsContract.CommonDataKinds.Phone.TYPE_WORK;

            default: return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
        }
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Log.w(TAG, "Cannot close the closeable " + closeable);
            }
        }
    }

    private class PersonCursor extends IterableCursorWrapper<Person> {

        /**
         * Convenience class to create a {@link IterableCursor} backed by the {@link Cursor} {@code cursor}.
         */
        PersonCursor(Cursor cursor) {
            super(cursor);
        }

        @Override
        public Person peek() {
            Person person = new Person();
            person.setId(getInt(ID));
            person.setFirstName(getString(DISPLAY_NAME));
            person.setEmail(getString(EMAIL));
            person.setThumbImageUrl(getString(PHOTO_THUMBNAIL));
            person.setLookupUri(getString(LOOKUP_KEY));
            return person;
        }
    }

    // TODO: Refactor and move this to the service. Currently, because this is here, we have a bug where - after we
    // delete all contacts - the service does not get stopped on the completion of this observable, and that triggers
    // a new generation request once we start up the application again from the recents, for example
    public Observable<Person> getDeletionObservable() {
        // TODO: This is probably very wrong, still learning RxJava
        return mBriteContentResolver.createQuery(CONTENT_URI, PROJECTION, SELECTION, SELECTION_ARGS, null, false)
                .take(1)
                .flatMap(new Func1<SqlBrite.Query, Observable<Person>>() {
                    @Override
                    public Observable<Person> call(SqlBrite.Query query) {
                        final PersonCursor personCursor = new PersonCursor(query.run());
                        return Observable.from(personCursor)
                                .doOnTerminate(new Action0() {
                                    @Override
                                    public void call() {
                                        personCursor.close();
                                    }
                                });
                    }
                })
                .map(new Func1<Person, Person>() {
                    @Override
                    public Person call(Person person) {
                        Uri contactUri = ContactsContract.Contacts.getLookupUri(person.getId(), person.getLookupUri());
                        int deletedRows = mContentResolver.delete(contactUri, null, null);
                        if (deletedRows < 1) {
                            Log.e(TAG, "Nothing deleted for ID " + person.getId());
                        }
                        return person;
                    }
                });
    }
}
