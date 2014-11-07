/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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
import android.view.ViewGroup;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * {@code Activity} displaying the list of parts recently viewed by the user.
 * <p>
 * Since only the recently viewed parts' Id is stored in the device's permanent memory, the data for each part
 * has to be loaded from the server. {@link PartLoaderByPart}s are used to handle the asynchronous loading
 * of these parts.
 * <p>
 * While the parts are loading, the {@code ListView} rows display an indeterminate {@code ProgressBar}. Once a
 * {@link Part} is downloaded, its row is updated to show its information. If the download of a part fails (for
 * example, if the part was deleted from the server after the user viewed it), then an error row is shown.
 * <p>
 * Layout file: {@link /res/layout/activity_element_list.xml activity_element_list}
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public class PartHistoryListActivity extends PartListActivity implements LoaderManager.LoaderCallbacks<Part> {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.parts.PartHistoryListActivity";

    private static final int LOADER_ID_RECENT_PARTS = 200;

    /**
     * Called when the {@code Activity} is created.
     * <p>Removes the {@code View} indicating that a loading was taking place. Loads the {@link com.docdoku.android.plm.client.NavigationHistory}
     * from the {@code SharedPreferences} to obtain the number of parts in history and their id.
     * <br>Initializes the {@code ArrayList} of
     * parts to the size of the history with null values. The {@link PartAdapter} will show a {@code ProgressBar}s for
     * these rows while the content remains {@code null}.
     * <br>Starts a {@link PartLoaderByPart} for each {@link Part}.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     * @see PartListActivity
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ((ViewGroup) loading.getParent()).removeView(loading);

        Log.i(LOG_TAG, "navigation history_light size: " + navigationHistory.getSize());
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
        Log.i(LOG_TAG, "Part history_light list size : " + partsArray.size());
    }

    /**
     * Starts of {@link PartLoaderByPart} to load a {@link Part}.
     *
     * @param id
     * @param bundle the {@code Bundle} containing the id and the workspace of the part to be downloaded.
     * @return
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public Loader<Part> onCreateLoader(int id, Bundle bundle) {
        Log.i(LOG_TAG, "Querying information for part in history_light at position " + (id - LOADER_ID_RECENT_PARTS) + " with reference " + bundle.getString("partKey"));
        return new PartLoaderByPart(this, bundle.getString("partKey"), bundle.getString("workspace"));
    }

    /**
     * Handles the result of the {@link PartLoaderByPart}.
     * <p>The id of the {@code Loader} is used to determine its position in the {@code ListView}, which is where it is added in
     * the {@code ArrayList} of parts, replacing the null value. The {@link PartAdapter} is then notified that the
     * data has changed.
     *
     * @param loader
     * @param part
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public void onLoadFinished(Loader<Part> loader, Part part) {
        Log.i(LOG_TAG, "Received information for part in history_light at position " + (loader.getId() - LOADER_ID_RECENT_PARTS) + " with reference " + part.getKey());
        partsArray.set(loader.getId() - LOADER_ID_RECENT_PARTS, part);
        partAdapter.notifyDataSetChanged();
    }

    /**
     *
     * @param loader
     * @see LoaderManager.LoaderCallbacks
     */
    @Override
    public void onLoaderReset(Loader<Part> loader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     *
     * @return
     * @see com.docdoku.android.plm.client.SimpleActionBarActivity
     */
    @Override
    protected int getActivityButtonId() {
        return R.id.recentlyViewedParts;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@code Loader} that makes a request to the server to obtain the information about a specific part.
     */
    private static class PartLoaderByPart extends Loader<Part> implements HttpGetTask.HttpGetListener {

        private final String elementId;
        private final String workspace;
        private AsyncTask asyncTask;

        public PartLoaderByPart(Context context, String elementId, String workspace) {
            super(context);
            this.elementId = elementId;
            this.workspace = workspace;
        }

        /**
         * Start an {@link HttpGetTask} to load the information about a part.
         *
         * @see Loader
         */
        @Override
        protected void onStartLoading (){
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts/" +  elementId);
        }

        /**
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
         *
         * @see Loader
         */
        @Override
        protected void onReset (){
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts/" +  elementId);
        }

        /**
         *
         * @see Loader
         */
        @Override
        protected void onForceLoad (){
            //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         *
         * @see Loader
         */
        @Override
        protected void onAbandon (){
            //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Handles the result of the {@link HttpGetTask}. The result is read to create a new instance of
         * {@link Part} which is passed to the {@code LoaderManager.LoaderCallbacks} using {@code deliverResult()}.
         *
         * @param result the {@code JSONObject} representing the {@link Part}.
         * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
         */
        @Override
        public void onHttpGetResult(String result) {
            Part part;
            try {
                JSONObject partJSON = new JSONObject(result);
                part = new Part(partJSON.getString("partKey"));
                part.updateFromJSON(partJSON, getContext().getResources());
            }catch (JSONException e){
                Log.e(LOG_TAG, "Error handling json object of a part");
                e.printStackTrace();
                Log.i(LOG_TAG, "Error message: " + e.getMessage());
                part = new Part(elementId);
            }
            deliverResult(part);
        }
    }
}
