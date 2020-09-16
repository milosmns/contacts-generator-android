
package me.angrybyte.contactsgenerator;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
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
    private TextView mShortestContactViewLabel;
    private TextView mShortestContactViewValue;
    private TextView mLongestContactViewLabel;
    private TextView mLongestContactViewValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mCheckDeviceView = (TextView) findViewById(R.id.stats_check_device);
        mRequestedCountView = (TextView) findViewById(R.id.stats_requested_count);
        mGeneratedCountView = (TextView) findViewById(R.id.stats_generated_count);
        mGeneratedMalesView = (TextView) findViewById(R.id.stats_generated_males);
        mGeneratedFemalesView = (TextView) findViewById(R.id.stats_generated_females);
        mAverageTimeView = (TextView) findViewById(R.id.stats_generated_average_time_value);
        mTotalTimeView = (TextView) findViewById(R.id.stats_generated_total_time_value);
        mShortestContactViewLabel = (TextView) findViewById(R.id.stats_shortest_generated_contact_label);
        mShortestContactViewValue = (TextView) findViewById(R.id.stats_shortest_generated_contact_value);
        mLongestContactViewLabel = (TextView) findViewById(R.id.stats_longest_generated_contact_label);
        mLongestContactViewValue = (TextView) findViewById(R.id.stats_longest_generated_contact_value);

        // prepare the toolbar with title coloring
        Toolbar toolbar = (Toolbar) findViewById(R.id.stats_toolbar);
        // noinspection ConstantConditions
        toolbar.setTitleTextColor(Color.WHITE);
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
            if (!serviceApi.isForceStopped() && (stats.requested == 0 || stats.generated < stats.requested)) {
                mCheckDeviceView.setVisibility(View.VISIBLE);
            } else {
                mCheckDeviceView.setVisibility(View.GONE);
            }

            String requested = String.valueOf(stats.requested);
            String generatedTotal = String.valueOf(stats.generated);
            String malesGenerated = String.valueOf(stats.males);
            String femalesGenerated = String.valueOf(stats.females);
            String averageTimePerContact = stats.averageTimePerContact / 1000 + "s";
            String totalTimeUsed = stats.totalTime / 1000 + "s";
            String shortestTimeForContact = TextUtils.isEmpty(stats.shortestContact) ? "" : stats.shortestContact + " ("
                    + stats.shortestContactTime / 1000 + "s)";
            String longestTimeForContact = TextUtils.isEmpty(stats.longestContact) ? "" : stats.longestContact + " ("
                    + stats.longestContactTime / 1000 + "s)";

            mRequestedCountView.setText(requested);
            mGeneratedCountView.setText(generatedTotal);
            mGeneratedMalesView.setText(malesGenerated);
            mGeneratedFemalesView.setText(femalesGenerated);
            mAverageTimeView.setText(averageTimePerContact);
            mTotalTimeView.setText(totalTimeUsed);

            mShortestContactViewValue.setText(shortestTimeForContact);
            mLongestContactViewValue.setText(longestTimeForContact);

            int shortestFieldsVisibility = shortestTimeForContact.isEmpty() ? View.GONE : View.VISIBLE;
            mShortestContactViewLabel.setVisibility(shortestFieldsVisibility);
            mShortestContactViewValue.setVisibility(shortestFieldsVisibility);

            int longestFieldsVisibility = longestTimeForContact.isEmpty() ? View.GONE : View.VISIBLE;
            mLongestContactViewLabel.setVisibility(longestFieldsVisibility);
            mLongestContactViewValue.setVisibility(longestFieldsVisibility);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service disconnected from " + TAG);
    }

}
