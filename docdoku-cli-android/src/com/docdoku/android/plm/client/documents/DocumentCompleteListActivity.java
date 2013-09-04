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

package com.docdoku.android.plm.client.documents;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
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
 * <code>Activity</code> for presenting all the {@link Document Documents} in a workspace.
 * <p>The documents are loaded asynchronously by pages of 20 items using a <code>Loader</code>.
 * <p>Layout file: {@link /res/layout/activity_element_list.xml activity_element_list}
 *
 * @author: Martin Devillers
 */
public class DocumentCompleteListActivity extends DocumentListActivity implements HttpGetTask.HttpGetListener, LoaderManager.LoaderCallbacks<List<Document>> {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.documents.DocumentCompleteListActivity";

    private int numDocumentsAvailable;
    private ProgressBar footerProgressBar;
    private int numPagesDownloaded;

    /**
     * Called when the <code>Activity</code> is created.
     * <p>Adds a footer <code>ProgressBar</code> that will be maintained while there remains more <code>Document</code>s
     * to be loaded.
     * <br>Sets an <code>setOnScrollListener</code> that loads the next page of <code>Document</code>s when this footer
     * becomes visible.
     * <p>Initializes the <code>ArrayList</code> containing the <code>Document</code>s and the <code>Adapter</code> for the
     * <code>ListView</code>, as well as the number of documents downloaded.
     * <p>Starts an {@link HttpGetTask} to download the number of documents in the workspace.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     * @see DocumentListActivity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        footerProgressBar = new ProgressBar(this);
        documentListView.addFooterView(footerProgressBar);

        documentArray = new ArrayList<Document>();
        documentAdapter = new DocumentAdapter(documentArray);
        documentListView.setAdapter(documentAdapter);

        numDocumentsAvailable = 0;
        numPagesDownloaded = 0;

        new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/count");

        documentListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && documentAdapter.getCount() < numDocumentsAvailable) {
                    Log.i(LOG_TAG, "Loading more parts. Next page: " + numPagesDownloaded);
                    Bundle bundle = new Bundle();
                    bundle.putInt("page", numPagesDownloaded);
                    bundle.putString("workspace", getCurrentWorkspace());
                    getSupportLoaderManager().initLoader(numPagesDownloaded, bundle, DocumentCompleteListActivity.this);
                }
            }
        });
    }

    /**
     * Handles the result of the query for the number of documents in the workspace.
     * <p>Registers the result. Removes the <code>View</code> that indicated that a loading was taking place. Starts
     * the <code>Loader</code> for the first page of documents.
     *
     * @param result The number of <code>Document</code>s in the workspace
     * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
     */
    @Override
    public void onHttpGetResult(String result) {
        try{
            result = result.substring(0, result.length() - 1);
            numDocumentsAvailable = Integer.parseInt(result);
            removeLoadingView();
            Bundle bundle = new Bundle();
            bundle.putInt("page", 0);
            bundle.putString("workspace", getCurrentWorkspace());
            Log.i(LOG_TAG, "Loading first part page");
            getSupportLoaderManager().initLoader(0, bundle, this);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "NumberFormatException: didn't correctly download number of pages of documents");
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
        return R.id.allDocuments;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns a new {@link DocumentLoaderByPage} for the page indicated in the <code>Bundle</code>
     *
     * @param id
     * @param bundle <code>Bundle</code> containing the <code>Document</code> page and the current workspace
     * @return The resulting <code>Loader</code>
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public Loader<List<Document>> onCreateLoader(int id, Bundle bundle) {
        return new DocumentLoaderByPage(this, bundle.getInt("page"), bundle.getString("workspace"));
    }

    /**
     * Adds the <code>List</code> of <code>Document</code>s obtained by the {@link DocumentLoaderByPage} to the
     * <code>ArrayList</code> of documents, and notifies the {@link DocumentAdapter} that the data has changed.
     * Updates the number of pages downloaded.
     * <p>If there are no more documents to download, removes the <code>FooterView</code>.
     *
     * @param loader the <code>Loader</code> that provided the result
     * @param data the {@code List<Document>} provided by the <code>Loader</code>
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public void onLoadFinished(Loader<List<Document>> loader, List<Document> data) {
        documentArray.addAll(data);
        documentAdapter.notifyDataSetChanged();
        numPagesDownloaded++;

        Log.i(LOG_TAG, "Finished loading a page. \nNumber of parts available: " + numDocumentsAvailable + "; \nNumber of parts downloaded: " + documentAdapter.getCount());
        if (documentAdapter.getCount() == numDocumentsAvailable){
            documentListView.removeFooterView(footerProgressBar);
        }
    }

    /**
     *
     * @param loader
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public void onLoaderReset(Loader<List<Document>> loader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Class that handles the loading of documents asynchronously.
     */
    private static class DocumentLoaderByPage extends Loader<List<Document>> implements HttpGetTask.HttpGetListener {

        private int startIndex;
        private String workspace;
        private AsyncTask asyncTask;
        private List<Document> downloadedParts;

        /**
         * Constructor called by the <code>LoaderManager.LoaderCallbacks</code> to start loading a page of documents.
         * The documents with index ranging from <code>20*page</code> to <code>20*page+19</code> will be loaded.
         *
         * @param context
         * @param page
         * @param workspace
         * @see Loader
         */
        public DocumentLoaderByPage(Context context, int page, String workspace) {
            super(context);
            startIndex = page*20;
            downloadedParts = new ArrayList<Document>();
            this.workspace = workspace;
        }

        /**
         * Starts an {@link HttpGetTask} to download the page of documents If the documents have already been downloaded,
         * passes them as a result.
         *
         * @see Loader
         */
        @Override
        protected void onStartLoading (){
            Log.i(LOG_TAG, "Starting DocumentLoader load for page " + startIndex/20);
            if (downloadedParts.size() == 0){
                asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/documents?start=" +  startIndex);
            } else {
                deliverResult(downloadedParts);
            }
        }

        /**
         * Cancels the {@link HttpGetTask} that was downloading the document page.
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
            Log.i(LOG_TAG, "Restarting DocumentLoader load for page " + startIndex/20);
            downloadedParts = new ArrayList<Document>();
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/documents?start=" +  startIndex);
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
         * Handles the result of the {@link HttpGetTask} containing a <code>JSONArray</code> of documents.
         * <p>Creates <code>Document</code> instances from the result and adds them to an {@code ArrayList<Document>}
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
                    Document part = new Document(partJSON.getString("id"));
                    part.updateFromJSON(partJSON, getContext().getResources());
                    downloadedParts.add(part);
                }
            }catch (JSONException e){
                Log.e(LOG_TAG, "Error handling json array of workspace's documents");
                e.printStackTrace();
                Log.i(LOG_TAG, "Error message: " + e.getMessage());
            }
            deliverResult(downloadedParts);
        }
    }
}
