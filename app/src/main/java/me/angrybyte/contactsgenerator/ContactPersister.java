package me.angrybyte.contactsgenerator;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import me.angrybyte.contactsgenerator.parser.data.User;

public class ContactPersister {
    private static final String TAG = ContactPersister.class.getSimpleName();
    private final Context mContext;

    public ContactPersister(Context context) {
        mContext = context;
    }

    /**
     * Stores a {@link User} object to the Android database. Doesn't use accounts, so, the contact
     * should be stored locally only.
     *
     * @param user The user to be persisted to the database.
     */
    public void storeContact(User user) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ContentProviderOperation.Builder op = ContentProviderOperation.newInsert(ContactsContract
                .RawContacts.CONTENT_URI).withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
                null).withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);

        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds
                        .StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, user
                        .getFirstName() + " " + user.getLastName());

        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone
                        .CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, user.getPhone())
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract
                        .CommonDataKinds.Phone.TYPE_HOME);

        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email
                        .CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, user.getEmail())
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract
                        .CommonDataKinds.Email.TYPE_HOME);

        if (user.getImage() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            user.getImage().compress(Bitmap.CompressFormat.PNG, 75, stream);

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds
                            .Photo.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds
                            .Photo.PHOTO, stream.toByteArray()).build());

            try {
                stream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error while flushing stream! Contact picture not added properly.", e);
            }
        }

        // Gives priority to other threads after the chunk before this line has been executed
        op.withYieldAllowed(true);

        ops.add(op.build());

        Log.d(TAG, "Creating contact: " + user.getFirstName() + " " + user.getLastName());

        try {
            mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Context context = mContext.getApplicationContext();

            Toast toast = Toast.makeText(context, "Contact(s) not created", Toast.LENGTH_SHORT);
            toast.show();

            Log.e(TAG, "Exception encountered while inserting contact: " + e);
        }
    }
}