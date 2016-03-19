
package me.angrybyte.contactsgenerator;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import me.angrybyte.contactsgenerator.api.GeneratorStats;
import me.angrybyte.contactsgenerator.service.GeneratorService;
import me.angrybyte.contactsgenerator.service.GeneratorServiceBinder;
import me.angrybyte.contactsgenerator.service.ServiceApi;

public class StatsActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = StatsActivity.class.getSimpleName();

    private TextView mRequestedCountView;
    private TextView mGeneratedCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mRequestedCountView = (TextView) findViewById(R.id.stats_requested_count);
        mGeneratedCountView = (TextView) findViewById(R.id.stats_generated_count);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent binderIntent = new Intent(this, GeneratorService.class);
        bindService(binderIntent, this, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(this);
    }

    @Override
    public void onBackPressed() {
        Intent backToMain = new Intent(this, MainActivity.class);
        startActivity(backToMain);
        finish();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "Service connected to " + TAG);

        ServiceApi serviceApi = ((GeneratorServiceBinder) service).getService();
        GeneratorStats stats = serviceApi.getStats();
        Intent serviceStopper = new Intent(this, GeneratorService.class);
        stopService(serviceStopper);

        if (stats != null) {
            mRequestedCountView.setText(String.format(mRequestedCountView.getText().toString(), stats.requested));
            mGeneratedCountView.setText(String.format(mGeneratedCountView.getText().toString(), stats.generated));
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service disconnected from " + TAG);
    }

}
