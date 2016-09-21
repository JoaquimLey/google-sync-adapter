package com.joaquimley.sync.drive;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Creates a file to GoogleApiClient's Drive account
 */
public class UploadToFolderTask implements ResultCallback<DriveApi.DriveContentsResult>, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "UploadToFolderTask";
    private static final String MIME_TYPE = "text/plain";

    private DriveTaskCallback mListener;

    private GoogleApiClient mGoogleApiClient;
    private DriveFolder mDriveFolder;
    private String mLocalFilePath;
    private String mFileTitle;

    public UploadToFolderTask(String driveFileTitle, String localFilePath, String folderDriveId, GoogleApiClient googleApiClient, @Nullable DriveTaskCallback listener) {

        if (listener != null) {
            mListener = listener;
        }

        mGoogleApiClient = googleApiClient;
        if (TextUtils.isEmpty(folderDriveId)) {
            if (mListener != null) {
                mListener.onTaskError("Folder id is null or empty");
            }
            return;
        }

        mDriveFolder = DriveId.decodeFromString(folderDriveId).asDriveFolder();
        mFileTitle = driveFileTitle;
        mLocalFilePath = localFilePath;

        if(!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.registerConnectionCallbacks(this);
            return;
        }
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull DriveApi.DriveContentsResult result) {
        if (!result.getStatus().isSuccess()) {
            if (mListener != null) {
                mListener.onTaskError("Error while trying to create new file contents");
            }
            return;
        }

        final DriveContents driveContents = result.getDriveContents();
        // write content to DriveContents
        OutputStream outputStream = driveContents.getOutputStream();

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(mLocalFilePath));
        } catch (FileNotFoundException e) {
            if (mListener != null) {
                mListener.onTaskError("Error uploading backup from drive, file not found");
                Log.e(TAG, "FileNotFound: " + e.getMessage());
            }
        }

        byte[] buf = new byte[1024];
        int bytesRead;
        try {
            if (inputStream != null) {
                while ((bytesRead = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            if (mListener != null) {
                mListener.onTaskError("Error writing inputStream: " + e.getMessage());
                Log.e(TAG, "inputStream: " + e.getMessage());
            }
        }


        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(mFileTitle)
                .setMimeType(MIME_TYPE)
                .build();

        // create a file in selected mDriveFolder
        mDriveFolder.createFile(mGoogleApiClient, changeSet, driveContents)
                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(@NonNull DriveFolder.DriveFileResult result) {
                        if (!result.getStatus().isSuccess()) {
                            if (mListener != null) {
                                mListener.onTaskError("Error while trying to create the file");
                                Log.d(TAG, "Error while trying to create the file");
                            }
                            return;
                        }
                        if (mListener != null) {
                            mListener.onTaskSuccess("File created!");
                        }
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() " + i);
    }
}