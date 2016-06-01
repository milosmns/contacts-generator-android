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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.angrybyte.contactsgenerator.parser.data.Person;

public class ContactOperations {

    private static final String TAG = ContactOperations.class.getSimpleName();
    private final Context mContext;
    private Cursor mCursor;
    private String mMatchingEmail;
    private ContentResolver mContentResolver;

    public ContactOperations(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
    }

    /**
     * Stores a {@link Person} object to the Android database. Doesn't use accounts, the contact should be stored locally only. <b><font
     * color="#FF3030">Not thread-safe.</font></b>
     *
     * @param person The person to be persisted to the database
     */
    public void storeContact(@NonNull Person person) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        ContentProviderOperation.Builder providerOperation = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null).withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);

        operations.add(providerOperation.build());

        providerOperation = ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, person.getFirstName() + " " + person.getLastName());

        operations.add(providerOperation.build());

        providerOperation = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, person.getPhone())
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);

        operations.add(providerOperation.build());

        providerOperation = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, person.getEmail())
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME);

        if (person.getImage() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            person.getImage().compress(Bitmap.CompressFormat.PNG, 100, stream);

            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray()).build());

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
     * Stores a {@link Person}s list to the Android database. Doesn't use accounts, all contacts from the list should be stored locally
     * only. <b><font color="#FF3030">Not thread-safe.</font></b>
     *
     * @param persons Persons list to be persisted to the database
     */
    public void storeContacts(@NonNull List<Person> persons) throws RemoteException, OperationApplicationException {
        for (int i = 0; i < persons.size(); i++) {
            storeContact(persons.get(i));
        }
    }

    /**
     * Prepares the cursor for the scrubbing (erasing) action. It is <b>mandatory</b> to call this method before
     * calling {@link #getFoundContactsCount()} and {@link #deleteNextContact()}. Failing to do so will result in a crash.
     *
     * @param matchingEmail You can parametrize the search query of the cursor with an email address. Sending {@code null} will find
     *                      all the contacts, while sending in a valid email address will find contacts with only that email address
     *                      associated with them
     * @return {@code True} if the cursor has been opened successfully, {@code false} otherwise
     */
    public boolean prepareCursorForScrubbing(@Nullable String matchingEmail) {
        // create a projection based on the given email
        String[] projection;
        String where;
        String[] whereArgs;
        if (matchingEmail != null) {
            mMatchingEmail = matchingEmail;
            projection = new String[] {
                    ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.CommonDataKinds.Email.ADDRESS
            };

            where = ContactsContract.CommonDataKinds.Email.ADDRESS + " LIKE ?";
            whereArgs = new String[] {
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
        mCursor = mContentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection, where, whereArgs, null);

        if (mCursor == null) {
            Log.e(TAG, "Cursor not available");
            return false;
        }

        return true;
    }

    /**
     * Retrieve the number of contacts that were found when the query was set up in {@link #prepareCursorForScrubbing(String)}. Do not call this
     * method before calling that one!
     *
     * @return The number of contacts that are available to be deleted in the cursor.
     */
    public int getFoundContactsCount() {
        return mCursor.getCount();
    }

    /**
     * Deletes one single contact from the Cursor that was prepared with {@link #prepareCursorForScrubbing(String)}. Do not call this method
     * before calling that one!
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
                    Log.w(TAG, "Nothing deleted for URI " + String.valueOf(uri));
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

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Log.w(TAG, "Cannot close the closeable " + String.valueOf(closeable));
            }
        }
    }

}
