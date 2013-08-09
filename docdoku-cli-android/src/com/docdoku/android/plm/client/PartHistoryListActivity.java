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
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author: martindevillers
 */
public class PartHistoryListActivity extends PartListActivity implements LoaderManager.LoaderCallbacks<Part> {

    private static final int LOADER_ID_RECENT_PARTS = 200;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Log.i("com.docdoku.android.plm.client", "navigation history_light size: " + navigationHistory.getSize());
        partsArray= new ArrayList<Part>();
        partAdapter = new PartAdapter(partsArray);
        partListView.setAdapter(partAdapter);

        Iterator<String> iterator = navigationHistory.getKeyIterator();
        int i = 0;
        while (iterator.hasNext()){
            Bundle bundle = new Bundle();
            bundle.putString("partKey", iterator.next());
            bundle.putString("workspace", getCurrentWorkspace());
            partsArray.add(null);
            getSupportLoaderManager().initLoader(LOADER_ID_RECENT_PARTS + i, bundle, this);
            i++;
        }
        Log.i("com.docdoku.android.plm.client", "Part history_light list size : " + partsArray.size());
    }

/**
 * LoaderManager.LoaderCallbacks Methods
 */

    @Override
    public Loader<Part> onCreateLoader(int id, Bundle bundle) {
        Log.i("com.docdoku.android.plm.client", "Querying information for part in history_light at position " + (id - LOADER_ID_RECENT_PARTS) + " with reference " + bundle.getString("partKey"));
        return new PartLoaderByPart(this, bundle.getString("partKey"), bundle.getString("workspace"));
    }

    @Override
    public void onLoadFinished(Loader<Part> loader, Part part) {
        Log.i("com.docdoku.android.plm.client", "Received information for part in history_light at position " + (loader.getId() - LOADER_ID_RECENT_PARTS) + " with reference " + part.getKey());
        partsArray.set(loader.getId() - LOADER_ID_RECENT_PARTS, part);
        partAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Part> loader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

/**
 * SearchActionBarActivity methods
 */
    @Override
    protected int getSearchQueryHintId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void executeSearch(String query) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected int getActivityButtonId() {
        return R.id.recentlyViewedParts;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static class PartLoaderByPart extends Loader<Part> implements HttpGetListener {

        private String elementId;
        private String workspace;
        private AsyncTask asyncTask;

        public PartLoaderByPart(Context context, String elementId, String workspace) {
            super(context);
            this.elementId = elementId;
            this.workspace = workspace;
        }

        @Override
        protected void onStartLoading (){
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts/" +  elementId);
        }

        @Override
        protected void onStopLoading (){
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
        }

        @Override
        protected void onReset (){
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts/" +  elementId);
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
            Part part = null;
            try {
                JSONObject partJSON = new JSONObject(result);
                part = new Part(partJSON.getString("partKey"));
                part.updateFromJSON(partJSON, getContext().getResources());
            }catch (JSONException e){
                Log.e("docdoku.DocDokuPLM", "Error handling json object of a part");
                e.printStackTrace();
                Log.i("docdoku.DocDokuPLM", "Error message: " + e.getMessage());
            }
            deliverResult(part);
        }
    }
}
