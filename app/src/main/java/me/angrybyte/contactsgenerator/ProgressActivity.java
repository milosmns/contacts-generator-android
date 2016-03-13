package me.angrybyte.contactsgenerator;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.parser.data.Person;
import me.angrybyte.contactsgenerator.service.GeneratorService;
import me.angrybyte.contactsgenerator.service.GeneratorServiceBinder;
import me.angrybyte.contactsgenerator.service.OnGenerateProgressListener;
import me.angrybyte.contactsgenerator.service.OnGenerateResultListener;
import me.angrybyte.contactsgenerator.service.ServiceApi;

public class ProgressActivity extends AppCompatActivity implements ServiceConnection, OnGenerateProgressListener, OnGenerateResultListener, View.OnClickListener {

    public static final String TAG = ProgressActivity.class.getSimpleName();

    public static final String KEY_NUMBER = "KEY_NUMBER";
    public static final String KEY_IMAGES = "KEY_IMAGES";
    public static final String KEY_GENDER = "KEY_GENDER";

    private ServiceApi mService;

    @Gender
    private String mGender;
    private boolean mFetchImages;
    private int mRequestedNumber;

    private TextView mContactDisplayNameView;
    private TextView mContactPhoneNumberView;
    private TextView mContactEmailView;
    private ImageView mContactPhotoView;
    private ProgressBar mProgressBar;
    private Button mStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        assignViews();
        readIntentData();

        mStopButton.setOnClickListener(this);
    }

    private void readIntentData() {
        // demo, don't look.
        mRequestedNumber = getIntent().getIntExtra(KEY_NUMBER, 0);
        mFetchImages = getIntent().getBooleanExtra(KEY_IMAGES, false);
        //noinspection WrongConstant
        mGender = getIntent().getStringExtra(KEY_GENDER);
        Log.d(TAG, "Received request for " + mRequestedNumber + " contacts " + (mFetchImages ? "with " : "without ") + "pictures. Gender: " + mGender);
    }

    private void assignViews() {
        mContactDisplayNameView = ((TextView) findViewById(R.id.activity_progress_name));
        mContactPhoneNumberView = ((TextView) findViewById(R.id.activity_progress_number));
        mContactEmailView = ((TextView) findViewById(R.id.activity_progress_email));
        mContactPhotoView = ((ImageView) findViewById(R.id.activity_progress_photo));
        mProgressBar = (ProgressBar) findViewById(R.id.activity_progress_progress_bar);
        mStopButton = (Button) findViewById(R.id.activity_progress_stop_service);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent serviceIntent = new Intent(this, GeneratorService.class);
        bindService(serviceIntent, this, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mService = ((GeneratorServiceBinder) iBinder).getService();
        mService.setOnGenerateProgressListener(this);
        mService.setOnGenerateResultListener(this);
        mService.generate(mRequestedNumber, mFetchImages, mGender);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

    @Override
    public void onGenerateProgress(@FloatRange(from = 0.0f, to = 1.0f) float progress) {
        Person person = mService.getLastGeneratedPerson();
        if (person != null) {
            mContactDisplayNameView.setText(person.getDisplayName());
            mContactPhoneNumberView.setText(person.getPhone());
            mContactEmailView.setText(person.getEmail());
            mContactPhotoView.setImageBitmap(person.getImage());
        }

        mProgressBar.setProgress((int) (progress * 100) + 10);
    }

    @Override
    public void onGenerateResult(@IntRange(from = 0) int requested, @IntRange(from = 0) int generated) {
        Toast.makeText(this, "Requested: " + requested + ", generated: " + generated, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_progress_stop_service:
                if (mService != null) {
                    mService.interruptGeneration();
                }

                Intent serviceStopper = new Intent(ProgressActivity.this, GeneratorService.class);
                stopService(serviceStopper);

                Intent goToStartScreen = new Intent(ProgressActivity.this, MainActivity.class);
                startActivity(goToStartScreen);

                finish();
                break;

            default:
                Log.d(TAG, "Unprocessed click.");
        }
    }
}
