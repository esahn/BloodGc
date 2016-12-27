package net.huray.bloodgc.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by ahn on 2016-12-27.
 */

public class BaseActivity extends AppCompatActivity {
    protected void redirectLoginActivity() {
        final Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    protected void redirectSignupActivity() {
        final Intent intent = new Intent(this, SignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}
