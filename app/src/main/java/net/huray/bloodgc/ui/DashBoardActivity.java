package net.huray.bloodgc.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import net.huray.bloodgc.NFCApp;
import net.huray.bloodgc.R;
import net.huray.bloodgc.model.BloodGlucose;


public class DashBoardActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        NFCApp.getDataStore().delete(BloodGlucose.class).get().value();
        setContentView(R.layout.activity_dashboard);


        Button LogoutButton = (Button) findViewById(R.id.btn_logout);
        LogoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Logout();
            }
        });
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

    private void Logout(){
        saveLogoutState();
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                redirectLoginActivity();
            }
        });
    }

    private void saveLogoutState(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(SyncBgmActivity.LOG_IN_STATE, false);
        editor.apply();
    }

}

