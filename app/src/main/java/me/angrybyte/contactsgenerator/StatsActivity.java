
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
    private TextView mShortestContactView;
    private TextView mLongestContactView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mCheckDeviceView = (TextView) findViewById(R.id.stats_check_device);
        mRequestedCountView = (TextView) findViewById(R.id.stats_requested_count);
        mGeneratedCountView = (TextView) findViewById(R.id.stats_generated_count);
        mGeneratedMalesView = (TextView) findViewById(R.id.stats_generated_males);
        mGeneratedFemalesView = (TextView) findViewById(R.id.stats_generated_females);
        mAverageTimeView = (TextView) findViewById(R.id.stats_generated_average_time);
        mTotalTimeView = (TextView) findViewById(R.id.stats_generated_total_time);
        mShortestContactView = (TextView) findViewById(R.id.stats_shortest_generated_contact);
        mLongestContactView = (TextView) findViewById(R.id.stats_longest_generated_contact);
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

            String malesGenerated = getString(R.string.stat_males);
            malesGenerated = String.format(malesGenerated, stats.males);

            String femalesGenerated = getString(R.string.stat_females);
            femalesGenerated = String.format(femalesGenerated, stats.females);

            String averageTimePerContact = getString(R.string.stat_average_time);
            averageTimePerContact = String.format(averageTimePerContact, stats.averageTimePerContact);

            String totalTimeUsed = getString(R.string.stat_total_time);
            totalTimeUsed = String.format(totalTimeUsed, stats.totalTime);

            String shortestTimeForContact = getString(R.string.stat_shortest_contacts);
            shortestTimeForContact = String.format(shortestTimeForContact, stats.shortestContact, stats.shortestContactTime);

            String longestTimeForContact = getString(R.string.stat_longest_contact);
            longestTimeForContact = String.format(longestTimeForContact, stats.longestContact, stats.longestContactTime);

            mRequestedCountView.setText(requested);
            mGeneratedCountView.setText(generatedTotal);
            mGeneratedMalesView.setText(malesGenerated);
            mGeneratedFemalesView.setText(femalesGenerated);
            mAverageTimeView.setText(averageTimePerContact);
            mTotalTimeView.setText(totalTimeUsed);
            mShortestContactView.setText(shortestTimeForContact);
            mLongestContactView.setText(longestTimeForContact);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service disconnected from " + TAG);
    }

}
