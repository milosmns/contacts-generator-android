
package me.angrybyte.contactsgenerator;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import me.angrybyte.contactsgenerator.parser.data.User;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_rate) {
                    Toast.makeText(MainActivity.this, "Rating action missing", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        });
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void tryItOut() {
        final RandomApi randomApi = new RandomApi(this);
        final ContactPersister contactPersister = new ContactPersister(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<User> users = randomApi.getUsersForQuery(1, RandomApi.BOTH);
                User user = users.get(0);
                contactPersister.storeContact(user);
            }
        }).start();
    }
}
