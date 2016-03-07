
package me.angrybyte.contactsgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import me.angrybyte.contactsgenerator.service.GeneratorService;

public class ProgressActivity extends AppCompatActivity {

    public static final String TAG = ProgressActivity.class.getSimpleName();

    public static final String KEY_NUMBER = "KEY_NUMBER";
    public static final String KEY_IMAGES = "KEY_IMAGES";
    public static final String KEY_GENDER = "KEY_GENDER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // demo, don't look.
        String format = "Number: %s, Images: %s, Gender: %s";
        int number = getIntent().getIntExtra(KEY_NUMBER, 0);
        boolean images = getIntent().getBooleanExtra(KEY_IMAGES, false);
        String gender = getIntent().getStringExtra(KEY_GENDER);
        ((TextView) findViewById(R.id.demo_text)).setText(String.format(format, number, images, gender));

        Intent serviceIntent = new Intent(ProgressActivity.this, GeneratorService.class);
        startService(serviceIntent);

        // demo, also don't look.
        findViewById(R.id.stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceStopper = new Intent(ProgressActivity.this, GeneratorService.class);
                stopService(serviceStopper);

                Intent statsIntent = new Intent(ProgressActivity.this, StatsActivity.class);
                startActivity(statsIntent);

                finish();
            }
        });
    }

}
