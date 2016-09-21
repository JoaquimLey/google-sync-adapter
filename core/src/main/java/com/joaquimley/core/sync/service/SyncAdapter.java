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

package com.joaquimley.core.sync.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.joaquimley.core.drive.CreateFileTask;
import com.joaquimley.core.drive.DriveTaskCallback;
import com.joaquimley.core.sync.view.SignInResolutionActivity;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private String TAG = "SyncAdapter";

    // Global variables
    // Define a variable to contain a content resolver instance
    private ContentResolver mContentResolver;
    private GoogleApiClient mGoogleApiClient;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            initGoogleApiClient(getContext(), account.name);
            mGoogleApiClient.connect();
        }

        new CreateFileTask("This Callbacks FileTask", "Hello world baby " + System.currentTimeMillis(), mGoogleApiClient, new DriveTaskCallback() {
            @Override
            public void onTaskStarted() {
                Log.e(TAG, "DriveTaskCallback onTaskStarted");
            }

            @Override
            public void onTaskInProgress() {
                Log.e(TAG, "DriveTaskCallback onTaskInProgress");
            }

            @Override
            public void onTaskSuccess(String returnText) {
                Log.e(TAG, "DriveTaskCallback onTaskSuccess " + returnText);
            }

            @Override
            public void onTaskError(String errorMessage) {
                Log.e(TAG, "DriveTaskCallback onTaskError " + errorMessage);
            }
        });
        Log.e(TAG, "on perform sync");
    }


    private void initGoogleApiClient(Context context, String accountName) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .setAccountName(accountName).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended() " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());
        if (connectionResult.hasResolution()) {
            getContext().startActivity(SignInResolutionActivity.newStartIntent(getContext(), connectionResult));
        } else {
            Log.e(TAG, "onConnectionFAiled() no resolution");
        }
    }
}
