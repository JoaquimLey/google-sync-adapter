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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.joaquimley.core.data.sync.SyncHelper;
import com.joaquimley.googlesyncadapter.R;

public class FeedFragment extends Fragment {

    public static final String ARG_SOME = "argSome";

    private boolean mSomeExampleArg;

    private AppCompatActivity mActivity;
    private Toolbar mToolbar;

    public static FeedFragment newInstance(boolean someArg) {

        Bundle args = new Bundle();
        args.putBoolean(ARG_SOME, someArg);
        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mSomeExampleArg = getArguments().getBoolean(ARG_SOME);
        }

        Toast.makeText(getActivity().getApplicationContext(), "Passed demo arg: " + mSomeExampleArg,
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        initViews(view);
        return view;
    }


    private void initViews(View view) {
        mActivity = (AppCompatActivity) getActivity();
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mActivity.setSupportActionBar(mToolbar);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh:
                SyncHelper.syncImmediately(mActivity.getApplicationContext());
                Snackbar.make(mActivity.findViewById(R.id.feed_layout), "Hello REFRESH", Snackbar.LENGTH_LONG).show();
                return true;

            case R.id.action_settings:
                Snackbar.make(mActivity.findViewById(R.id.feed_layout), "Hello Settings", Snackbar.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
