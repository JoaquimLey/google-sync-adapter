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

package com.joaquimley.core.data.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.joaquimley.core.R;


public final class SyncHelper {

    private static final String TAG = "SyncHelper";
    // Interval at which to sync, in seconds.
    private static final int SYNC_INTERVAL = (int) (DateUtils.WEEK_IN_MILLIS / 1000);
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final int SYNCABLE_TRUE = 1;

    private SyncHelper() {
    }

    public static boolean initializeSync(Context context) {
        Account syncAccount = getSyncAccount(context);
        if (syncAccount != null) {
            onAccountCreated(context, syncAccount);
            Log.v(TAG, "Syncing initialized");
            return true;
        }
        Log.v(TAG, "Syncing failed, no account permissions.");
        return false;
    }

    /**
     * Get a dummy account for the sync adapter
     *
     * @param context The application context
     */
    private static Account getSyncAccount(Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "We do not have accounts permission, canceling sync");
            return null;
        }

        Account[] accounts = AccountManager.get(context).getAccountsByType(context.getString(R.string.sync_account_type));
        if (accounts.length == 0) {
            Log.d(TAG, "Must have a Google account installed");
            return null;
        }
        return accounts[0];
    }

    private static void onAccountCreated(Context context, Account newAccount) {
        /*
         * Inform the system that this account supports sync
         */
        ContentResolver.setIsSyncable(newAccount, context.getString(R.string.sync_authority), SYNCABLE_TRUE);
        /*
         * Enable our periodic sync.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.sync_authority), true);
        /*
         * Since we've created an account
         */
        configurePeriodicSync(context, newAccount);
        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, Account account) {
        final String authority = context.getString(R.string.sync_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // We can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().setExtras(Bundle.EMPTY).
                    syncPeriodic(SYNC_INTERVAL, SYNC_FLEXTIME).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, Bundle.EMPTY, SYNC_INTERVAL);
        }
        Log.v(TAG, "Periodic sync configured with " + SYNC_INTERVAL + " interval and " + SYNC_FLEXTIME + " flextime");
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.sync_authority), bundle);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     * @param extras  The Bundle of extra values
     */
    public static void syncImmediately(Context context, Bundle extras) {
        Bundle bundle = new Bundle(extras);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.sync_authority), bundle);
    }

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
