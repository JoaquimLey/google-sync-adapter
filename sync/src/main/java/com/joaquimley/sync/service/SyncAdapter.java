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

package com.joaquimley.sync.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentSender;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.joaquimley.sync.SyncHelper;
import com.joaquimley.sync.drive.UploadToFolderTask;
import com.joaquimley.sync.view.FolderPickerActivity;
import com.joaquimley.sync.view.SignInResolutionActivity;

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
    private boolean mIsUploadToFolder;
    private String mFolderId;

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

        Log.e(TAG, "on perform sync");
        Log.e("onPerformSync", "BUNDLE isFolder: " + bundle.getBoolean(SyncHelper.SYNC_IS_TO_UPLOAD_TO_FOLDER, false));
        Log.e("onPerformSync", "BUNDLE FolderId: " + bundle.getString(SyncHelper.SYNC_DRIVE_FOLDER_ID, ""));
        mIsUploadToFolder = bundle.getBoolean(SyncHelper.SYNC_IS_TO_UPLOAD_TO_FOLDER, false);
        mFolderId = bundle.getString(SyncHelper.SYNC_DRIVE_FOLDER_ID, "");

        if (mIsUploadToFolder) {
            if (TextUtils.isEmpty(mFolderId) && mGoogleApiClient.isConnected()) {
                startPickerActivity();
                Log.e(TAG, "onPerformSync(): startPickerActivity");
                return;
            }

            new UploadToFolderTask("blabla", mFolderId, mGoogleApiClient, null);
            Log.e(TAG, "onPerformSync(): uploadingToFolder");
            return;
        }
        Log.e(TAG, "onPerformSync(): not to upload to folder");

//        if (bundle.getBoolean(SyncHelper.SYNC_IS_TO_UPLOAD_TO_FOLDER)) {
//            new UploadToFolderTask("downloads/cenas", bundle.getString(SyncHelper.SYNC_SHARED_PREFERENCES_KEY_FOLDER_ID), mGoogleApiClient, new DriveTaskCallback() {
//                public void onTaskStarted() {
//                    Log.e("DriveTaskCallback", "onTaskStarted()");
//                }
//
//                @Override
//                public void onTaskInProgress() {
//                    Log.e("DriveTaskCallback", "onTaskInProgress()");
//                }
//
//                @Override
//                public void onTaskSuccess(String returnText) {
//                    Log.e("DriveTaskCallback", "onTaskSuccess() " + returnText);
//                }
//
//                @Override
//                public void onTaskError(String errorMessage) {
//                    Log.e("DriveTaskCallback", "onTaskError() " + errorMessage);
//                }
//            });
//            return;
//        }


//        new CreateFileTask("This Callbacks FileTask", "Hello world baby " + System.currentTimeMillis(), mGoogleApiClient, new
//                DriveTaskCallback() {
//                    @Override
//                    public void onTaskStarted() {
//                        Log.e("DriveTaskCallback", "onTaskStarted()");
//                    }
//
//                    @Override
//                    public void onTaskInProgress() {
//                        Log.e("DriveTaskCallback", "onTaskInProgress()");
//                    }
//
//                    @Override
//                    public void onTaskSuccess(String returnText) {
//                        Log.e("DriveTaskCallback", "onTaskSuccess() " + returnText);
//                    }
//
//                    @Override
//                    public void onTaskError(String errorMessage) {
//                        Log.e("DriveTaskCallback", "onTaskError() " + errorMessage);
//                    }
//                });
    }


    private void initGoogleApiClient(Context context, String accountName) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .setAccountName(accountName).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void startPickerActivity() {
        IntentSender folderPickerIntent = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{DriveFolder.MIME_TYPE})
                .build(mGoogleApiClient);

        getContext().startActivity(FolderPickerActivity.newStartIntent(getContext(), folderPickerIntent));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");
        if (mIsUploadToFolder && TextUtils.isEmpty(mFolderId)) {
            startPickerActivity();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());
        if (connectionResult.hasResolution()) {
            getContext().startActivity(SignInResolutionActivity.newStartIntent(getContext(), connectionResult));
        } else {
            Log.e(TAG, "onConnectionFAiled() no resolution");
        }
    }
}
