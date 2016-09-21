package com.joaquimley.sync.drive;

import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Creates a file to GoogleApiClient's Drive account
 */
public class CreateFileTask {

    private static final String MIME_TYPE = "text/plain";

    public CreateFileTask(String title, String text, GoogleApiClient googleApiClient) {
        new CreateFileTask(title, text, googleApiClient, null);
    }

    public CreateFileTask(String title, String text, GoogleApiClient googleApiClient, @Nullable DriveTaskCallback listener) {

        if (listener != null) {
            listener.onTaskStarted();
        }

        DriveApi.DriveContentsResult driveContentsResult =
                Drive.DriveApi.newDriveContents(googleApiClient).await();
        if (!driveContentsResult.getStatus().isSuccess()) {
            // We failed, stop the task and return.
            if (listener != null) {
                listener.onTaskError(driveContentsResult.getStatus().getStatusMessage());
            }
            return;
        }

        if (listener != null) {
            listener.onTaskInProgress();
        }

        // Read the contents and open its output stream for writing, then
        // write a @param text.
        DriveContents originalContents = driveContentsResult.getDriveContents();
        OutputStream os = originalContents.getOutputStream();
        try {
            os.write(text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onTaskError("IO error: " + e.getMessage());
            }
            return;
        }

        // Create the metadata for the new file including title and MIME type.
        MetadataChangeSet originalMetadata = new MetadataChangeSet.Builder()
                .setTitle(title)
                .setMimeType(MIME_TYPE).build();

        // Create the file in the root folder, again calling await() to
        // block until the request finishes.
        // TODO: 21/09/16 Pass folder
        DriveFolder rootFolder = Drive.DriveApi.getRootFolder(googleApiClient);
        DriveFolder.DriveFileResult fileResult = rootFolder.createFile(
                googleApiClient, originalMetadata, originalContents).await();

        if (!fileResult.getStatus().isSuccess()) {
            // We failed, stop the task and return.
            if (listener != null) {
                listener.onTaskError(fileResult.getStatus().getStatusMessage());
            }
            return;
        }

        // Finally, fetch the metadata for the newly created file, again
        // calling await to block until the request finishes.
        DriveResource.MetadataResult metadataResult = fileResult.getDriveFile()
                .getMetadata(googleApiClient)
                .await();
        if (!metadataResult.getStatus().isSuccess()) {
            if (listener != null) {
                listener.onTaskError(metadataResult.getStatus().getStatusMessage());
            }
            // We failed, stop the task and return.
            return;
        }
        // We succeeded, return the newly created metadata.
        if (listener != null) {
            listener.onTaskSuccess(title + " uploaded!");
        }
    }
}
