/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.android.plm.client.parts;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 *
 * @author: Martin Devillers
 */
public class PartCompleteListActivity extends PartListActivity implements HttpGetTask.HttpGetListener, LoaderManager.LoaderCallbacks<List<Part>>{
    private static final String LOG_TAG = "com.docdoku.android.plm.client.parts.PartCompleteListActivity";

    private static final int LOADER_ID_ALL_PARTS = 100;

    private View footerProgressBar;
    private int numPartsAvailable;
    private int numPagesDownloaded;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "Creating PartCompleteListActivity");

        ((ViewGroup) loading.getParent()).removeView(loading);

        footerProgressBar = new ProgressBar(this);
        partListView.addFooterView(footerProgressBar);

        partsArray = new ArrayList<Part>();
        partAdapter = new PartAdapter(partsArray);
        partListView.setAdapter(partAdapter);

        numPartsAvailable = 0;
        numPagesDownloaded = 0;
        new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/parts/count");

        partListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && partAdapter.getCount() < numPartsAvailable){
                        Log.i(LOG_TAG, "Loading more parts. Next page: " + numPagesDownloaded);
                        Bundle bundle = new Bundle();
                        bundle.putInt("page", numPagesDownloaded);
                        bundle.putString("workspace", getCurrentWorkspace());
                        getSupportLoaderManager().initLoader(LOADER_ID_ALL_PARTS + numPagesDownloaded, bundle, PartCompleteListActivity.this);
                }
            }
        });

    }

    @Override
    public Loader<List<Part>> onCreateLoader(int i, Bundle bundle) {
        return new PartLoaderByPage(this, bundle.getInt("page"), bundle.getString("workspace"));  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLoadFinished(Loader<List<Part>> partLoader, List<Part> parts) {
        partsArray.addAll(parts);
        partAdapter.notifyDataSetChanged();
        numPagesDownloaded++;

        Log.i(LOG_TAG, "Finished loading a page. \nNumber of parts available: " + numPartsAvailable + "; \nNumber of parts downloaded: " + partAdapter.getCount());
        if (partAdapter.getCount() == numPartsAvailable){
            partListView.removeFooterView(footerProgressBar);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Part>> partLoader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onHttpGetResult(String result) {
        try{
            result = result.substring(0, result.length() - 1);
            numPartsAvailable = Integer.parseInt(result);
            Bundle bundle = new Bundle();
            bundle.putInt("page", 0);
            bundle.putString("workspace", getCurrentWorkspace());
            Log.i(LOG_TAG, "Loading first part page");
            getSupportLoaderManager().initLoader(LOADER_ID_ALL_PARTS + 0, bundle, this);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "NumberFormatException: didn't correctly download_light number of pages of parts");
            Log.e(LOG_TAG, "Number of pages result: " + result);
            e.printStackTrace();
        }
    }

    @Override
    protected int getActivityButtonId() {
        return R.id.allParts;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static class PartLoaderByPage extends Loader<List<Part>> implements HttpGetTask.HttpGetListener {

        private int startIndex;
        private String workspace;
        private AsyncTask asyncTask;
        private List<Part> downloadedParts;

        public PartLoaderByPage(Context context, int page, String workspace) {
            super(context);
            startIndex = page*20;
            downloadedParts = new ArrayList<Part>();
            this.workspace = workspace;
        }

        @Override
        protected void onStartLoading (){
            Log.i(LOG_TAG, "Starting PartLoader load for page " + startIndex/20);
            if (downloadedParts.size() == 0){
                asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts?start=" +  startIndex);
            } else {
                deliverResult(downloadedParts);
            }
        }

        @Override
        protected void onStopLoading (){
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
        }

        @Override
        protected void onReset (){
            Log.i(LOG_TAG, "Restarting PartLoader load for page " + startIndex/20);
            downloadedParts = new ArrayList<Part>();
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts?start=" +  startIndex);
        }

        @Override
        protected void onForceLoad (){
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void onAbandon (){
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onHttpGetResult(String result) {
            try {
                JSONArray partsJSON = new JSONArray(result);
                for (int i=0; i<partsJSON.length(); i++){
                    JSONObject partJSON = partsJSON.getJSONObject(i);
                    Part part = new Part(partJSON.getString("partKey"));
                    part.updateFromJSON(partJSON, getContext().getResources());
                    downloadedParts.add(part);
                }
            }catch (JSONException e){
                Log.e(LOG_TAG, "Error handling json array of workspace's parts");
                e.printStackTrace();
                Log.i(LOG_TAG, "Error message: " + e.getMessage());
            }
            deliverResult(downloadedParts);
        }
    }
}