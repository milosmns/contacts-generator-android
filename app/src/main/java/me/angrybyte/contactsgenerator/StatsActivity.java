
package me.angrybyte.contactsgenerator;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import me.angrybyte.contactsgenerator.api.GeneratorStats;
import me.angrybyte.contactsgenerator.service.GeneratorService;
import me.angrybyte.contactsgenerator.service.GeneratorServiceBinder;
import me.angrybyte.contactsgenerator.service.ServiceApi;

public class StatsActivity extends AppCompatActivity implements ServiceConnection {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
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
        ServiceApi serviceApi = ((GeneratorServiceBinder) service).getService();
        GeneratorStats stats = serviceApi.getStats();
        Intent serviceStopper = new Intent(this, GeneratorService.class);
        stopService(serviceStopper);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

}
