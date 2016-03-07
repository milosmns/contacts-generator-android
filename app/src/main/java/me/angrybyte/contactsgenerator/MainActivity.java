
package me.angrybyte.contactsgenerator;

import android.content.ComponentName;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import me.angrybyte.contactsgenerator.api.ContactOperations;
import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.api.Operations;
import me.angrybyte.contactsgenerator.parser.data.Person;
import me.angrybyte.contactsgenerator.service.GeneratorService;
import me.angrybyte.contactsgenerator.service.GeneratorServiceBinder;
import me.angrybyte.numberpicker.view.ActualNumberPicker;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener, ServiceConnection {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CheckBox mUseAvatars;
    private RadioButton mMales;
    private RadioButton mFemales;
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

    private void tryItOut() {
        final Operations operations = new Operations(this);
        final ContactOperations contacts = new ContactOperations(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Person> persons = operations.getPersons(1, Operations.BOTH);
                Person person = persons.get(0);
                try {
                    contacts.storeContact(person);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_button_generate: {
                Intent generatorIntent = new Intent(this, ProgressActivity.class);
                generatorIntent.putExtra(ProgressActivity.KEY_NUMBER, mPicker.getValue());
                generatorIntent.putExtra(ProgressActivity.KEY_IMAGES, mUseAvatars.isChecked());
                generatorIntent.putExtra(ProgressActivity.KEY_GENDER, getChosenGender());
                startActivity(generatorIntent);
                break;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rate: {
                Toast.makeText(this, "Rating action missing", Toast.LENGTH_LONG).show();
                return true;
            }
            case R.id.action_info: {
                Toast.makeText(this, "About action missing", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
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
        if (!(binder instanceof GeneratorServiceBinder)) {
            Log.d(TAG, "Cannot use service: " + name.getShortClassName());
            return;
        }

        if (((GeneratorServiceBinder) binder).getService().isInitialized()) {
            // just show the generator UI
            Intent progressIntent = new Intent(this, ProgressActivity.class);
            startActivity(progressIntent);
            finish();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service " + name.getShortClassName() + " connected to " + TAG);
    }

}
