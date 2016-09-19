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

package com.joaquimley.core.data.sync.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.joaquimley.core.data.sync.SyncHelper;

/**
 * Created by joaquimley on 19/09/16.
 */
public class GoogleSignInResolutionActivity extends AppCompatActivity {

    public static final String TAG = "GoogleResolution";
    public static final String EXTRA_CONNECTION_RESULT = "extraConnectionResult";
    public static final String EXTRA_ACCOUNT_NAME = "extraAccountName";
    private static final int REQUEST_CODE_RESOLUTION = 9001;

    private String mAccountName;
    private ConnectionResult mConnectionResult;

    public static Intent newStartIntent(Context context, ConnectionResult result) {
        Intent startIntent = new Intent(context, GoogleSignInResolutionActivity.class);
//        googleSignInResolutionIntent.putExtra(EXTRA_ACCOUNT_NAME, accountName);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent.putExtra(EXTRA_CONNECTION_RESULT, result);
        return startIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mAccountName = getIntent().getStringExtra(EXTRA_ACCOUNT_NAME);
            mConnectionResult = (ConnectionResult) getIntent().getExtras().get(EXTRA_CONNECTION_RESULT);
        }
        Log.e(TAG, "connectionREsult null " + (mConnectionResult == null));
        showResolutionDialog(mConnectionResult);
    }

    private void showResolutionDialog(ConnectionResult result) {
        GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);

        }
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            Log.e(TAG, "Resolution OK");
            SyncHelper.initializeSync(getApplicationContext());
            finish();
        }
    }

}
