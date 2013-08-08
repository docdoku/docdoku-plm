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

package com.docdoku.android.plm.client;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.*;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author: Martin Devillers
 */
public class DocumentCompleteListActivity extends DocumentListActivity implements HttpGetListener, LoaderManager.LoaderCallbacks<List<Document>> {

    private static final int LOADER_ID_ALL_DOCUMENTS = 400;

    private int numDocumentsAvailable;
    private ProgressBar footerProgressBar;
    private int numPagesDownloaded;

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
                    Log.i("com.docdoku.android.plm.client", "Loading more parts. Next page: " + numPagesDownloaded);
                    Bundle bundle = new Bundle();
                    bundle.putInt("page", numPagesDownloaded);
                    bundle.putString("workspace", getCurrentWorkspace());
                    getSupportLoaderManager().initLoader(LOADER_ID_ALL_DOCUMENTS + numPagesDownloaded, bundle, DocumentCompleteListActivity.this);
                }
            }
        });
    }

    @Override
    public void onHttpGetResult(String result) {
        try{
            result = result.substring(0, result.length() - 1);
            numDocumentsAvailable = Integer.parseInt(result);
            Bundle bundle = new Bundle();
            bundle.putInt("page", 0);
            bundle.putString("workspace", getCurrentWorkspace());
            Log.i("com.docdoku.android.plm.client", "Loading first part page");
            getSupportLoaderManager().initLoader(LOADER_ID_ALL_DOCUMENTS + 0, bundle, this);
        } catch (NumberFormatException e) {
            Log.e("com.docdoku.android.plm.client", "NumberFormatException: didn't correctly download number of pages of documents");
            Log.e("com.docdoku.android.plm.client", "Number of pages result: " + result);
            e.printStackTrace();
        }
    }

    /**
     * LoaderManager.LoaderCallbacks methods
     */
    @Override
    public Loader<List<Document>> onCreateLoader(int id, Bundle bundle) {
        return new DocumentLoaderByPage(this, bundle.getInt("page"), bundle.getString("workspace"));
    }

    @Override
    public void onLoadFinished(Loader<List<Document>> loader, List<Document> data) {
        documentArray.addAll(data);
        documentAdapter.notifyDataSetChanged();
        numPagesDownloaded++;

        Log.i("com.docdoku.android.plm.client", "Finished loading a page. \nNumber of parts available: " + numDocumentsAvailable + "; \nNumber of parts downloaded: " + documentAdapter.getCount());
        if (documentAdapter.getCount() == numDocumentsAvailable){
            documentListView.removeFooterView(footerProgressBar);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Document>> loader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private static class DocumentLoaderByPage extends Loader<List<Document>> implements HttpGetListener {

        private int startIndex;
        private String workspace;
        private AsyncTask asyncTask;
        private List<Document> downloadedParts;

        public DocumentLoaderByPage(Context context, int page, String workspace) {
            super(context);
            startIndex = page*20;
            downloadedParts = new ArrayList<Document>();
            this.workspace = workspace;
        }

        @Override
        protected void onStartLoading (){
            Log.i("com.docdoku.android.plm.client", "Starting DocumentLoader load for page " + startIndex/20);
            if (downloadedParts.size() == 0){
                asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/documents?start=" +  startIndex);
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
            Log.i("com.docdoku.android.plm.client", "Restarting DocumentLoader load for page " + startIndex/20);
            downloadedParts = new ArrayList<Document>();
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/documents?start=" +  startIndex);
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
                    Document part = new Document(partJSON.getString("id"));
                    part.updateFromJSON(partJSON, getContext().getResources());
                    downloadedParts.add(part);
                }
            }catch (JSONException e){
                Log.e("com.docdoku.android.plm.client", "Error handling json array of workspace's documents");
                e.printStackTrace();
                Log.i("com.docdoku.android.plm.client", "Error message: " + e.getMessage());
            }
            deliverResult(downloadedParts);
        }
    }
}
