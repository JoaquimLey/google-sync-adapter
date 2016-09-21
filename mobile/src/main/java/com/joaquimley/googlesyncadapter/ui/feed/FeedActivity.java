/*
 * Copyright (c) Joaquim Ley 2016. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joaquimley.googlesyncadapter.ui.feed;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.joaquimley.sync.SyncHelper;
import com.joaquimley.googlesyncadapter.R;

public class FeedActivity extends AppCompatActivity {

    private static String EXTRA_USER_NAME = "extraUserName";
    private static final int RCQ_GET_ACCOUNTS = 1337;

    public static Intent newStartIntent(Context context, String userName) {
        Intent startIntent = new Intent(context, FeedActivity.class);
        startIntent.putExtra(EXTRA_USER_NAME, userName);
        return startIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme); // Branded launch
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        if (checkForGoogleAccountPermission()) {
            SyncHelper.initializeSync(this);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.feed_container, FeedFragment.newInstance())
                    .commit();
        }

        Toast.makeText(getApplicationContext(), "Hello " + getIntent().getExtras()
                .getString(EXTRA_USER_NAME), Toast.LENGTH_SHORT).show();
    }

    private boolean checkForGoogleAccountPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, RCQ_GET_ACCOUNTS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case RCQ_GET_ACCOUNTS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    SyncHelper.initializeSync(this);
                } else {
                    Toast.makeText(getApplicationContext(), "Can't sync without permission",
                            Toast.LENGTH_LONG).show();
                }
                break;

            default:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
