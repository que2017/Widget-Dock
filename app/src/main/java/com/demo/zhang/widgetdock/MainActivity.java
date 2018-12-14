package com.demo.zhang.widgetdock;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Checks if the specified context can draw on top of other apps. As of API
         * level 23, an app cannot draw on top of other apps unless it declares the
         * {@link Manifest.permission#SYSTEM_ALERT_WINDOW} permission in its
         * manifest, <em>and</em> the user specifically grants the app this
         * capability. To prompt the user to grant this approval, the app must send an
         * intent with the action
         * {@link Settings#ACTION_MANAGE_OVERLAY_PERMISSION}, which
         * causes the system to display a permission management screen.
         *
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(MainActivity.this, MainService.class);
                Log.i(TAG, "Toucher had opened.");
                startService(intent);
                finish();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "请开启该权限，否则Widget Dock将无法使用！", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Intent intent = new Intent(MainActivity.this, MainService.class);
            startService(intent);
            finish();
        }
    }
}
