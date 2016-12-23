package net.huray.bloodgc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import net.huray.bloodgc.NFCApp;
import net.huray.bloodgc.R;
import net.huray.bloodgc.model.BloodGlucose;


public class DashBoardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        NFCApp.getDataStore().delete(BloodGlucose.class).get().value();
        setContentView(R.layout.activity_dashboard);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.dash_board_screen);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.move_see_record:
                Intent intent = new Intent(this, RecordListActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}

