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
import android.os.Bundle;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.joaquimley.core.ui.ApiClientAsyncTask;
import com.joaquimley.core.ui.BaseAuthActivity;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An activity to illustrate making synchronous requests to the Drive service
 * back-end.
 */
public class SyncRequestsActivity extends BaseAuthActivity {

    private static final String EXTRA_FILE_NAME = "extraFileName";

    public static Intent newStartIntent(Context context, String fileName) {
        Intent syncRequestActivityIntent = new Intent(context, SyncRequestsActivity.class);
        syncRequestActivityIntent.putExtra(EXTRA_FILE_NAME, fileName);
        return syncRequestActivityIntent;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        new CreateFileAsyncTask(this, getIntent().getStringExtra(EXTRA_FILE_NAME)).execute();
    }

    /**
     * An async task that creates a new text file by creating new contents and
     * metadata entities on user's root folder. A number of blocking tasks are
     * performed serially in a thread. Each time, await() is called on the
     * result which blocks until the request has been completed.
     */
    public class CreateFileAsyncTask extends ApiClientAsyncTask<Void, Void, Metadata> {

        public static final String MIME_TYPE = "text/plain";
        private final String mFileName;

        public CreateFileAsyncTask(Context context, String fileName) {
            super(context);
            mFileName = fileName;
        }

        @Override
        protected Metadata doInBackgroundConnected(Void... arg0) {

            // First we start by creating a new contents, and blocking on the
            // result by calling await().
            DriveApi.DriveContentsResult driveContentsResult =
                    Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                // We failed, stop the task and return.
                return null;
            }

            // Read the contents and open its output stream for writing, then
            // write a short message.
            DriveContents originalContents = driveContentsResult.getDriveContents();
            OutputStream os = originalContents.getOutputStream();
            try {
                os.write("Hello world!\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            // Create the metadata for the new file including title and MIME
            // type.
            MetadataChangeSet originalMetadata = new MetadataChangeSet.Builder()
                    .setTitle(mFileName)
                    .setMimeType(MIME_TYPE).build();

            // Create the file in the root folder, again calling await() to
            // block until the request finishes.
            DriveFolder rootFolder = Drive.DriveApi.getRootFolder(getGoogleApiClient());
            DriveFolder.DriveFileResult fileResult = rootFolder.createFile(
                    getGoogleApiClient(), originalMetadata, originalContents).await();
            if (!fileResult.getStatus().isSuccess()) {
                // We failed, stop the task and return.
                return null;
            }

            // Finally, fetch the metadata for the newly created file, again
            // calling await to block until the request finishes.
            DriveResource.MetadataResult metadataResult = fileResult.getDriveFile()
                    .getMetadata(getGoogleApiClient())
                    .await();
            if (!metadataResult.getStatus().isSuccess()) {
                // We failed, stop the task and return.
                return null;
            }
            // We succeeded, return the newly created metadata.
            return metadataResult.getMetadata();
        }

        @Override
        protected void onPostExecute(Metadata result) {
            super.onPostExecute(result);
            if (result == null) {
                // The creation failed somehow, so show a message.
                showMessage("Error while creating the file.");
                return;
            }
            // The creation succeeded, show a message.
            showMessage("File created: " + result.getDriveId());
        }
    }
}