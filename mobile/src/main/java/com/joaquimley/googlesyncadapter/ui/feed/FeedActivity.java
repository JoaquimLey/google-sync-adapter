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
import android.view.Menu;

import com.joaquimley.core.data.sync.SyncHelper;
import com.joaquimley.googlesyncadapter.R;
import com.joaquimley.googlesyncadapter.ui.base.BaseActivity;

public class FeedActivity extends BaseActivity {

    private static final int RCQ_GET_ACCOUNTS = 1337;
    private static String EXTRA_USER_NAME = "extraUserName";

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
            // Initialize sync adapter
            SyncHelper.initializeSync(this);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.feed_container, FeedFragment.newInstance(true))
                    .commit();
        }
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
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO: 13/09/16 Inform the user no background sync will happen
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
}
