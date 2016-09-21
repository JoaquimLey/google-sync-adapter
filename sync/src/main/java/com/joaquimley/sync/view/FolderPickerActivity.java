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

package com.joaquimley.sync.view;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.joaquimley.sync.SyncHelper;

/**
 * {@link AppCompatActivity} used to request the user's desired Drive folder
 * to be called from the SyncAdapter only.
 */
public class FolderPickerActivity extends AppCompatActivity {

    private static final String TAG = "FolderPickerActivity";
    private static final String EXTRA_INTENT_SENDER = "extraIntentSender";
    private static final int RC_FOLDER_PICK = 9002;

    private IntentSender mFolderPickerIntentSender;

    public static Intent newStartIntent(Context context, IntentSender intentSender) {
        Intent startIntent = new Intent(context, FolderPickerActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent.putExtra(EXTRA_INTENT_SENDER, intentSender);
        return startIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mFolderPickerIntentSender = (IntentSender) getIntent().getExtras().get(EXTRA_INTENT_SENDER);
        }
        showFolderPicker(mFolderPickerIntentSender);
    }

    private void showFolderPicker(IntentSender folderPickerIntentSender) {
        try {
            startIntentSenderForResult(folderPickerIntentSender, RC_FOLDER_PICK, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Failed to show folder picker: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Unable to access folder picker, please try again later",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_FOLDER_PICK && resultCode == RESULT_OK) {
            Log.d(TAG, "FolderPicker OK");
            // Get the folder drive id
            DriveId driveFolderId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
            updateFolderIdToSharedPreferences(driveFolderId);
            // Now we can call the SyncAdapter to start syncing to the desired folder!
            SyncHelper.uploadFileToDriveFolder(this, driveFolderId.encodeToString());
        }
        finish(); // Finish picker activity
    }

    private void updateFolderIdToSharedPreferences(DriveId driveFolderId) {
        SharedPreferences sharedPreferences = getSharedPreferences(SyncHelper.SYNC_SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putString(SyncHelper.SYNC_DRIVE_FOLDER_ID, driveFolderId.encodeToString()).apply();
    }

}
