
package me.angrybyte.contactsgenerator;

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

    public ContactOperations(Context context) {
        mContext = context;
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

        // gives priority to other threads after the chunk before this line has been executed
        providerOperation.withYieldAllowed(true);

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
     * Deletes all contacts with a matching email provider from the contacts database. <b><font color="#FF3030">Not thread-safe.</font></b>
     *
     * @param matchingEmail An email provider website (domain) which all obsolete contacts have in common. If set to {@code null}, all
     *            contacts will be deleted
     * @return {@code True} if all contacts are deleted properly, {@code false} if anything fails
     */
    public boolean deleteContacts(@Nullable String matchingEmail) {
        // create a projection based on the given email
        ContentResolver contentResolver = mContext.getContentResolver();
        String[] projection;
        if (matchingEmail != null) {
            projection = new String[] {
                    ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.CommonDataKinds.Email.ADDRESS
            };
        } else {
            projection = new String[] {
                ContactsContract.Contacts.LOOKUP_KEY
            };
        }

        // try to create a cursor
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection, null, null, null);
        if (cursor == null) {
            Log.e(TAG, "Cursor not available");
            return false;
        }

        // loop through them and delete all matching
        while (cursor.moveToNext()) {
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
            boolean delete = matchingEmail == null || cursor.getString(column).contains(matchingEmail);
            if (delete) {
                String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                int deletedRows = contentResolver.delete(uri, null, null);
                if (deletedRows < 1) {
                    Log.w(TAG, "Nothing deleted for URI " + String.valueOf(uri));
                }
            }
        }

        // close the cursor ignoring the close error
        try {
            cursor.close();
        } catch (Exception e) {
            Log.w(TAG, "Error closing cursor");
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
