/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Activity</code> for presenting all the {@link Part Parts} in a workspace.
 * <p>The parts are loaded asynchronously by pages of 20 items using a <code>Loader</code>.
 * <p>Layout file: {@link /res/layout/activity_element_list.xml activity_element_list}
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public class PartCompleteListActivity extends PartListActivity implements HttpGetTask.HttpGetListener, LoaderManager.LoaderCallbacks<List<Part>>{
    private static final String LOG_TAG = "com.docdoku.android.plm.client.parts.PartCompleteListActivity";

    private View footerProgressBar;
    private int numPartsAvailable;
    private int numPagesDownloaded;

    /**
     * Called when the <code>Activity</code> is created.
     * <p>Adds a footer <code>ProgressBar</code> that will be maintained while there remains more <code>Part</code>s
     * to be loaded.
     * <br>Sets an <code>setOnScrollListener</code> that loads the next page of <code>Part</code>s when this footer
     * becomes visible.
     * <p>Initializes the <code>ArrayList</code> containing the <code>Part</code>s and the <code>Adapter</code> for the
     * <code>ListView</code>, as well as the number of documents downloaded.
     * <p>Starts an {@link HttpGetTask} to download the number of parts in the workspace.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     * @see PartListActivity
     */
    @Override
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
                        getSupportLoaderManager().initLoader(numPagesDownloaded, bundle, PartCompleteListActivity.this);
                }
            }
        });

    }

    /**
     * Returns a new {@link PartLoaderByPage} for the page indicated in the <code>Bundle</code>
     *
     * @param i
     * @param bundle <code>Bundle</code> containing the <code>Part</code> page and the current workspace
     * @return The resulting <code>Loader</code>
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public Loader<List<Part>> onCreateLoader(int i, Bundle bundle) {
        return new PartLoaderByPage(this, bundle.getInt("page"), bundle.getString("workspace"));  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Adds the <code>List</code> of <code>Part</code>s obtained by the {@link PartLoaderByPage} to the
     * <code>ArrayList</code> of parts, and notifies the {@link PartAdapter} that the data has changed.
     * Updates the number of pages downloaded.
     * <p>If there are no more parts to download, removes the <code>FooterView</code>.
     *
     * @param partLoader the <code>Loader</code> that provided the result
     * @param parts the {@code List<Part>} provided by the <code>Loader</code>
     * @see LoaderManager.LoaderCallbacks
     */
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

    /**
     *
     * @param partLoader
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public void onLoaderReset(Loader<List<Part>> partLoader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Handles the result of the query for the number of parts in the workspace.
     * <p>Registers the result. Removes the <code>View</code> that indicated that a loading was taking place. Starts
     * the <code>Loader</code> for the first page of parts.
     *
     * @param result The number of <code>Part</code>s in the workspace
     * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
     */
    @Override
    public void onHttpGetResult(String result) {
        try{
            result = result.substring(0, result.length() - 1);
            numPartsAvailable = Integer.parseInt(result);
            Bundle bundle = new Bundle();
            bundle.putInt("page", 0);
            bundle.putString("workspace", getCurrentWorkspace());
            Log.i(LOG_TAG, "Loading first part page");
            getSupportLoaderManager().initLoader(0, bundle, this);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "NumberFormatException: didn't correctly download_light number of pages of parts");
            Log.e(LOG_TAG, "Number of pages result: " + result);
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     * @see com.docdoku.android.plm.client.SimpleActionBarActivity
     */
    @Override
    protected int getActivityButtonId() {
        return R.id.allParts;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Class that handles the loading of parts asynchronously.
     */
    private static class PartLoaderByPage extends Loader<List<Part>> implements HttpGetTask.HttpGetListener {

        private final int startIndex;
        private final String workspace;
        private AsyncTask asyncTask;
        private List<Part> downloadedParts;

        /**
         * Constructor called by the <code>LoaderManager.LoaderCallbacks</code> to start loading a page of parts.
         * The parts with index ranging from <code>20*page</code> to <code>20*page+19</code> will be loaded.
         *
         * @param context
         * @param page
         * @param workspace
         * @see Loader
         */
        public PartLoaderByPage(Context context, int page, String workspace) {
            super(context);
            startIndex = page*20;
            downloadedParts = new ArrayList<Part>();
            this.workspace = workspace;
        }

        /**
         * Starts an {@link HttpGetTask} to download the page of parts If the parts have already been downloaded,
         * passes them as a result.
         *
         * @see Loader
         */
        @Override
        protected void onStartLoading (){
            Log.i(LOG_TAG, "Starting PartLoader load for page " + startIndex/20);
            if (downloadedParts.size() == 0){
                asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts?start=" +  startIndex);
            } else {
                deliverResult(downloadedParts);
            }
        }

        /**
         * Cancels the {@link HttpGetTask} that was downloading the part page.
         *
         * @see Loader
         */
        @Override
        protected void onStopLoading (){
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
        }

        /**
         * Restarts the {@link HttpGetTask} that was downloading the document page.
         *
         * @see Loader
         */
        @Override
        protected void onReset (){
            Log.i(LOG_TAG, "Restarting PartLoader load for page " + startIndex/20);
            downloadedParts = new ArrayList<Part>();
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts?start=" +  startIndex);
        }

        /**
         * @see Loader
         */
        @Override
        protected void onForceLoad (){
            //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * @see Loader
         */
        @Override
        protected void onAbandon (){
            //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Handles the result of the {@link HttpGetTask} containing a <code>JSONArray</code> of parts.
         * <p>Creates <code>Part</code> instances from the result and adds them to an {@code ArrayList<Document>}
         * which is passed to the {@code LoaderManager.LoaderCallbacks} in the {@code deliverResult()} method.
         *
         * @param result the query <code>String</code> result
         * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
         * @see Loader
         */
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