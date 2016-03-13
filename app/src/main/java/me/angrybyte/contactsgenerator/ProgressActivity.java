
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import me.angrybyte.contactsgenerator.api.Operations;
import me.angrybyte.contactsgenerator.parser.data.Person;
import me.angrybyte.contactsgenerator.service.GeneratorService;
import me.angrybyte.contactsgenerator.service.GeneratorServiceBinder;
import me.angrybyte.contactsgenerator.service.OnGenerateProgressListener;
import me.angrybyte.contactsgenerator.service.OnGenerateResultListener;
import me.angrybyte.contactsgenerator.service.ServiceApi;

public class ProgressActivity extends AppCompatActivity implements ServiceConnection, OnGenerateProgressListener, OnGenerateResultListener {

    public static final String TAG = ProgressActivity.class.getSimpleName();

    public static final String KEY_NUMBER = "KEY_NUMBER";
    public static final String KEY_IMAGES = "KEY_IMAGES";
    public static final String KEY_GENDER = "KEY_GENDER";
    private ServiceApi mService;
    private ProgressBar mProgressBar;
    private int mNumber;
    private boolean mFetchImages;
    private String mGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // demo, don't look.
        String format = "Number: %s, Images: %s, Gender: %s";
        mNumber = getIntent().getIntExtra(KEY_NUMBER, 0);
        mFetchImages = getIntent().getBooleanExtra(KEY_IMAGES, false);
        mGender = getIntent().getStringExtra(KEY_GENDER);
        Log.d(TAG, "Received request for " + mNumber + " contacts " + (mFetchImages ? "with " : "without ") + "pictures.");

        // demo, also don't look.
        findViewById(R.id.activity_progress_stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.stopGenerating();
                Intent serviceStopper = new Intent(ProgressActivity.this, GeneratorService.class);
                stopService(serviceStopper);

                Intent mainActIntent = new Intent(ProgressActivity.this, MainActivity.class);
                startActivity(mainActIntent);

                finish();
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.activity_progress_progress_bar);
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
        mService.generate(15, true, Operations.MALE);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onGenerateProgress(@FloatRange(from = 0.0f, to = 1.0f) float progress, @IntRange(from = 0) int iStep,
            @IntRange(from = 0) int generated) {
        Person person = mService.getLastGeneratedPerson();
        if (person != null) {
            String personName = person.getFirstName() + " " + person.getLastName();
            ((TextView) findViewById(R.id.activity_progress_name)).setText(personName);
            ((TextView) findViewById(R.id.activity_progress_number)).setText(person.getPhone());
            ((TextView) findViewById(R.id.activity_progress_email)).setText(person.getEmail());
            ((ImageView) findViewById(R.id.activity_progress_photo)).setImageBitmap(person.getImage());
        }

        mProgressBar.setProgress((int) (progress * 100) + 10);
    }

    @Override
    public void onGenerateResult(@IntRange(from = 0) int requested, @IntRange(from = 0) int generated, boolean forced) {
        Toast.makeText(this, "Requested: " + requested + ", generated: " + generated, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
        finish();
    }

}
