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

package com.joaquimley.core.ui;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An async task that creates a new text file by creating new contents and
 * metadata entities on user's root folder. A number of blocking tasks are
 * performed serially in a thread. Each time, await() is called on the
 * result which blocks until the request has been completed.
 */
public class CreateFileAsyncTask extends ApiClientAsyncTask<Void, Void, Metadata> {

    public static final String MIME_TYPE = "text/plain";

    private final String mFileName;
    private final AsyncTaskCallbacks mListener;

    public CreateFileAsyncTask(Context context, String fileName, AsyncTaskCallbacks listener) {
        super(context);
        mFileName = fileName;
        mListener = listener;
    }

    @Override
    protected Metadata doInBackgroundConnected(Void... arg0) {
        mListener.onTaskInProgress();
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
            Log.e("createFileAsyncTask", "Error while creating the file.");
            mListener.onPostExecute(null);
            return;
        }
        // The creation succeeded, show a message.
        mListener.onPostExecute(result);
    }
}
