
package me.angrybyte.contactsgenerator;

import android.content.OperationApplicationException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.List;

import me.angrybyte.contactsgenerator.parser.data.Person;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private RadioButton mMales;
    private RadioButton mFemales;
    private ImageButton mGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // prepare the toolbar with title coloring
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_main);

        // initialize views
        mMales = (RadioButton) findViewById(R.id.activity_main_gender_male);
        mFemales = (RadioButton) findViewById(R.id.activity_main_gender_female);
        RadioButton bothGenders = (RadioButton) findViewById(R.id.activity_main_gender_both);
        findViewById(R.id.activity_main_button_generate).setOnClickListener(this);

        // hack-fix for the buggy RadioGroup
        bothGenders.setChecked(true);
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
                Toast.makeText(this, "Generate action missing", Toast.LENGTH_SHORT).show();
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

}
