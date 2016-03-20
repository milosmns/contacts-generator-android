
package me.angrybyte.contactsgenerator;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import me.angrybyte.contactsgenerator.api.GeneratorStats;
import me.angrybyte.contactsgenerator.service.GeneratorService;
import me.angrybyte.contactsgenerator.service.GeneratorServiceBinder;
import me.angrybyte.contactsgenerator.service.ServiceApi;

public class StatsActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = StatsActivity.class.getSimpleName();

    private TextView mCheckDeviceView;
    private TextView mRequestedCountView;
    private TextView mGeneratedCountView;
    private TextView mGeneratedMalesView;
    private TextView mGeneratedFemalesView;
    private TextView mAverageTimeView;
    private TextView mTotalTimeView;
    private TextView mLongestContactView;
    private TextView mShortestContactView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mCheckDeviceView = (TextView) findViewById(R.id.stats_check_device);
        mGeneratedMalesView = (TextView) findViewById(R.id.stats_generated_males);
        mGeneratedFemalesView = (TextView) findViewById(R.id.stats_generated_females);
        mTotalTimeView = (TextView) findViewById(R.id.stats_generated_total_time);
        mAverageTimeView = (TextView) findViewById(R.id.stats_generated_average_time);
        mGeneratedCountView = (TextView) findViewById(R.id.stats_generated_count);
        mRequestedCountView = (TextView) findViewById(R.id.stats_requested_count);
        mLongestContactView = (TextView) findViewById(R.id.stats_longest_generated_contact);
        mShortestContactView = (TextView) findViewById(R.id.stats_shortest_generated_contact);
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
        Intent serviceStopper = new Intent(this, GeneratorService.class);
        stopService(serviceStopper);

        Intent backToMain = new Intent(this, MainActivity.class);
        startActivity(backToMain);

        finish();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "Service connected to " + TAG);

        ServiceApi serviceApi = ((GeneratorServiceBinder) service).getService();
        GeneratorStats stats = serviceApi.getStats();

        if (stats != null) {
            if (stats.requested == 0 || stats.generated < stats.requested) {
                mCheckDeviceView.setVisibility(View.VISIBLE);
            } else {
                mCheckDeviceView.setVisibility(View.GONE);
            }

            String requested = getString(R.string.stat_total_requested);
            requested = String.format(requested, stats.requested);

            String generatedTotal = getString(R.string.stat_total_generated);
            generatedTotal = String.format(generatedTotal, stats.generated);

            mRequestedCountView.setText(requested);
            mGeneratedCountView.setText(String.format(mGeneratedCountView.getText().toString(), stats.generated));
            mAverageTimeView.setText(String.format(mAverageTimeView.getText().toString(), stats.averageTimePerContact));
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service disconnected from " + TAG);
    }

}
