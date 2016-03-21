
package me.angrybyte.contactsgenerator;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.widget.RadioButton;
import android.widget.Toast;

import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.api.Operations;
import me.angrybyte.contactsgenerator.service.GeneratorService;
import me.angrybyte.contactsgenerator.service.GeneratorServiceBinder;
import me.angrybyte.contactsgenerator.service.ServiceApi;
import me.angrybyte.numberpicker.view.ActualNumberPicker;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener, ServiceConnection,
        AlertDialog.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_WRITE_REQUEST_CODE = 0;
    private static final int PERMISSIONS_READ_REQUEST_CODE = 1;

    private CheckBox mUseAvatars;
    private RadioButton mMales;
    private RadioButton mFemales;
    private AlertDialog mAboutDialog;
    private ActualNumberPicker mPicker;

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
        mMales = (RadioButton) findViewById(R.id.activity_main_gender_male);
        mFemales = (RadioButton) findViewById(R.id.activity_main_gender_female);
        mUseAvatars = (CheckBox) findViewById(R.id.main_avatars_checkbox);
        RadioButton bothGenders = (RadioButton) findViewById(R.id.activity_main_gender_both);
        findViewById(R.id.activity_main_button_generate).setOnClickListener(this);

        // hack-fix for the buggy RadioGroup
        bothGenders.setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Starting " + TAG + "...");

        Intent serviceIntent = new Intent(this, GeneratorService.class);
        bindService(serviceIntent, this, 0);
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
            case R.id.activity_main_button_generate: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    generateContacts();
                } else {
                    ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.WRITE_CONTACTS
                    }, PERMISSIONS_WRITE_REQUEST_CODE);
                }
                break;
            }
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    private void generateContacts() {
        Intent generatorServiceIntent = new Intent(this, GeneratorService.class);
        startService(generatorServiceIntent);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle(R.string.developer_about);
                builder.setMessage(R.string.developer_note);
                builder.setPositiveButton(R.string.developer_share, this);
                builder.setNegativeButton(R.string.developer_dont_care, this);
                builder.setNeutralButton(R.string.developer_github, this);
                mAboutDialog = builder.show();
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
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Stopping " + TAG + "...");
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

        ServiceApi serviceApi = ((GeneratorServiceBinder) binder).getService();
        Intent nextActivityIntent;
        if (serviceApi.getStats() == null) {
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
        }
    }
}
