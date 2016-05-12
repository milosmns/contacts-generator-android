
package me.angrybyte.contactsgenerator;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.api.Operations;
import me.angrybyte.contactsgenerator.service.GeneratorService;
import me.angrybyte.contactsgenerator.service.GeneratorServiceBinder;
import me.angrybyte.contactsgenerator.service.ServiceApi;
import me.angrybyte.numberpicker.view.ActualNumberPicker;

public class MainActivity extends AppCompatActivity
        implements Toolbar.OnMenuItemClickListener, View.OnClickListener, ServiceConnection, AlertDialog.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_WRITE_REQUEST_CODE = 0;
    private static final int PERMISSIONS_READ_REQUEST_CODE = 1;

    private static final String PERSISTENT_NUMBER_OF_REQUESTS = "number";
    private static final String PERSISTENT_GENDER = "gender";
    private static final String PERSISTENT_USAGE_OF_PHOTOS = "use_photos";

    private Dialog mConfirmationDialog;
    private CheckBox mUseAvatars;
    private AlertDialog mAboutDialog;
    private RadioButton mMales;
    private RadioButton mFemales;
    private RadioButton mBothGenders;
    private ImageButton mIncrement;
    private ImageButton mDecrement;
    private ProgressDialog mProgressDialog;
    private ActualNumberPicker mPicker;
    private boolean mServiceDisconnected;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating " + TAG + "...");
        setContentView(R.layout.activity_main);

        // prepare the toolbar with title coloring
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_main);

        // initialize views
        mPicker = (ActualNumberPicker) findViewById(R.id.main_number_picker);
        mMales = (RadioButton) findViewById(R.id.main_gender_male);
        mFemales = (RadioButton) findViewById(R.id.main_gender_female);
        mUseAvatars = (CheckBox) findViewById(R.id.main_avatars_checkbox);
        mBothGenders = (RadioButton) findViewById(R.id.main_gender_both);
        mIncrement = (ImageButton) findViewById(R.id.main_button_increment);
        mDecrement = (ImageButton) findViewById(R.id.main_button_decrement);

        findViewById(R.id.main_button_generate).setOnClickListener(this);
        mIncrement.setOnClickListener(this);
        mDecrement.setOnClickListener(this);

        // hack-fix for the buggy RadioGroup
        mBothGenders.setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Starting " + TAG + "...");

        Intent serviceIntent = new Intent(this, GeneratorService.class);
        bindService(serviceIntent, this, 0);

        restoreUiState();
    }

    @Gender
    private String getChosenGender() {
        if (mMales.isChecked()) {
            return Operations.MALE;
        } else if (mFemales.isChecked()) {
            return Operations.FEMALE;
        } else {
            return Operations.BOTH;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_button_generate: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    generateContacts();
                } else {
                    ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.WRITE_CONTACTS
                    }, PERMISSIONS_WRITE_REQUEST_CODE);
                }
                break;
            }
            case R.id.main_button_increment: {
                mPicker.setValue(mPicker.getValue() + 1);
                // not invalidating automatically to prevent a catastrophic number of invalidations in a single frame
                mPicker.invalidate();
                break;
            }
            case R.id.main_button_decrement: {
                mPicker.setValue(mPicker.getValue() - 1);
                // not invalidating automatically to prevent a catastrophic number of invalidations in a single frame
                mPicker.invalidate();
                break;
            }
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    private void generateContacts() {
        Intent generatorServiceIntent = new Intent(this, GeneratorService.class);
        startService(generatorServiceIntent);

        if (mServiceDisconnected) {
            bindService(generatorServiceIntent, this, 0);
        }
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    private void deleteGeneratedContacts() {
        Intent deleteIntent = new Intent(MainActivity.this, GeneratorService.class);
        deleteIntent.setAction(ServiceApi.DELETE_CONTACTS_ACTION);
        MainActivity.this.startService(deleteIntent);

        if (mServiceDisconnected) {
            bindService(deleteIntent, this, 0);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rate: {
                rateApp();
                return true;
            }
            case R.id.action_info: {
                closeAboutDialog();
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ContactsTheme_AlertDialog);
                builder.setCancelable(false);
                builder.setTitle(R.string.developer_about);
                builder.setMessage(R.string.developer_note);
                builder.setPositiveButton(R.string.developer_share, this);
                builder.setNegativeButton(R.string.developer_dont_care, this);
                builder.setNeutralButton(R.string.developer_github, this);
                mAboutDialog = builder.show();
                return true;
            }
            case R.id.action_delete_generated: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ContactsTheme_AlertDialog);
                builder.setTitle(R.string.delete_confirmation_title);
                builder.setMessage(R.string.delete_confirmation_message);
                builder.setNegativeButton(R.string.delete_confirmation_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.delete_confirmation_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                            deleteGeneratedContacts();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                                    Manifest.permission.READ_CONTACTS
                            }, PERMISSIONS_READ_REQUEST_CODE);
                        }
                    }
                });

                mConfirmationDialog = builder.show();

                return true;
            }
        }
        return false;
    }

    private void shareApp() {
        String url = getString(R.string.developer_store) + getPackageName();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        String text = getString(R.string.developer_check_out) + " " + url;
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, getString(R.string.app_name)));
    }

    private void rateApp() {
        String url = getString(R.string.developer_store) + getPackageName();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void viewAppSource() {
        String url = getString(R.string.developer_source);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                shareApp();
                break;
            }
            case DialogInterface.BUTTON_NEGATIVE: {
                dialog.dismiss();
                break;
            }
            case DialogInterface.BUTTON_NEUTRAL: {
                viewAppSource();
                break;
            }
        }
    }

    private void closeAboutDialog() {
        if (mAboutDialog != null && mAboutDialog.isShowing()) {
            mAboutDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissConfirmationPrompt();
        dismissProgressDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Stopping " + TAG + "...");
        saveUiState();
        unbindService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy " + TAG + "...");
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.d(TAG, "Service " + name.getShortClassName() + " connected to " + TAG);
        mServiceDisconnected = false;

        ServiceApi serviceApi = ((GeneratorServiceBinder) binder).getService();
        Intent nextActivityIntent;
        if (serviceApi.isDeleting()) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.delete_progress_message));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            return;
        } else if (serviceApi.getStats() == null) {
            Log.d(TAG, "Stats object is null, means generating hasn't started yet");
            nextActivityIntent = new Intent(this, ProgressActivity.class);
            nextActivityIntent.putExtra(ProgressActivity.KEY_NUMBER, mPicker.getValue());
            nextActivityIntent.putExtra(ProgressActivity.KEY_IMAGES, mUseAvatars.isChecked());
            nextActivityIntent.putExtra(ProgressActivity.KEY_GENDER, getChosenGender());
        } else if (serviceApi.isGenerating()) {
            Log.d(TAG, "Stats object is not null, and it's still generating");
            nextActivityIntent = new Intent(this, ProgressActivity.class);
        } else {
            Log.d(TAG, "Stats object is not null, and it's finished generating");
            nextActivityIntent = new Intent(this, StatsActivity.class);
        }

        startActivity(nextActivityIntent);
        finish();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service " + name.getShortClassName() + " connected to " + TAG);
        mServiceDisconnected = true;
        dismissProgressDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_WRITE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // noinspection MissingPermission | We just checked?!
                    generateContacts();
                } else {
                    Toast.makeText(this, R.string.permission_denied_wow, Toast.LENGTH_SHORT).show();
                }

                break;
            }
            case PERMISSIONS_READ_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // noinspection MissingPermission | We just checked?!
                    deleteGeneratedContacts();
                } else {
                    Toast.makeText(this, R.string.permission_denied_cheeky, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                Log.d(TAG, "Random permission request code. Ignore.");
        }
    }

    private void restoreUiState() {
        SharedPreferences sharedPreferences = getSharedPreferences(getApplicationInfo().name, Context.MODE_PRIVATE);
        int requests = sharedPreferences.getInt(PERSISTENT_NUMBER_OF_REQUESTS, 1);
        String gender = sharedPreferences.getString(PERSISTENT_GENDER, Operations.BOTH);
        boolean usePhotos = sharedPreferences.getBoolean(PERSISTENT_USAGE_OF_PHOTOS, true);

        mPicker.setValue(requests);
        switch (gender) {
            case Operations.BOTH:
                mBothGenders.toggle();
                break;
            case Operations.MALE:
                mMales.toggle();
                break;
            case Operations.FEMALE:
                mFemales.toggle();
                break;
            default:
                Log.w(TAG, "How?");
        }

        mUseAvatars.setChecked(usePhotos);
    }

    private void saveUiState() {
        SharedPreferences sharedPreferences = getSharedPreferences(getApplicationInfo().name, Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putInt(PERSISTENT_NUMBER_OF_REQUESTS, mPicker.getValue());
        preferencesEditor.putString(PERSISTENT_GENDER, getChosenGender());
        preferencesEditor.putBoolean(PERSISTENT_USAGE_OF_PHOTOS, mUseAvatars.isChecked());
        preferencesEditor.apply();
    }

    private void dismissConfirmationPrompt() {
        if (mConfirmationDialog != null && mConfirmationDialog.isShowing()) {
            mConfirmationDialog.dismiss();
            mConfirmationDialog = null;
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
