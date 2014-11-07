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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * {@code Activity} for displaying list of results of an advanced part search.
 * <br>The type of result to display is specified in the {@code Intent} extra with key {@link #LIST_MODE_EXTRA}.
 * This should always indicate a part search query results.
 * <p>Layout file: {@link /res/layout/activity_element_list.xml activity_element_list}
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public class PartSimpleListActivity extends PartListActivity implements HttpGetTask.HttpGetListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.parts.PartSimpleListActivity";

    /**
     * {@code Intent} extra key to find the type of result to display
     */
    public static final String  LIST_MODE_EXTRA = "list mode";
    /**
     * Value of {@code Intent} extra with key {@link #LIST_MODE_EXTRA} indicating that the list of all parts should be shown
     * @deprecated use a {@link PartCompleteListActivity}
     */
    public static final int ALL_PARTS_LIST = 0;
    /**
     * Value of {@code Intent} extra with key {@link #LIST_MODE_EXTRA} indicating that the list of search results should be shown
     */
    public static final int PART_SEARCH = 2;
    /**
     * {@code Intent} extra key to find the search query to execute
     */
    public static final String SEARCH_QUERY_EXTRA = "search query";

    /**
     * Called when the {@code Activity} is created.
     * <br>Reads from the {@code Intent} the which parts should be displayed, and starts an {@link HttpGetTask} to query
     * the server for the results.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     * @see PartListActivity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int listCode = intent.getIntExtra(LIST_MODE_EXTRA, ALL_PARTS_LIST);
        switch (listCode){
            case ALL_PARTS_LIST: //UNUSED, replaced by PartCompleteListActivity to implement loader
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/parts/");
                break;
            case PART_SEARCH:
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/parts/search/" + intent.getStringExtra(SEARCH_QUERY_EXTRA));
                break;
        }
    }

    /**
     * Called when the query result is obtained.
     * <br>Reads the array and adds the parts to the {@code Adapter}, then notifies it that its data set has changed.
     *
     * @param result a {@code JSONArray} of {@link Part Parts}
     * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
     */
    @Override
    public void onHttpGetResult(String result) {
        if (loading !=null){
            ((ViewGroup) loading.getParent()).removeView(loading);
            loading = null;
        }
        partsArray = new ArrayList<Part>();
        try {
            JSONArray partsJSON = new JSONArray(result);
            for (int i=0; i<partsJSON.length(); i++){
                JSONObject partJSON = partsJSON.getJSONObject(i);
                Part part = new Part(partJSON.getString("partKey"));
                partsArray.add(part.updateFromJSON(partJSON, getResources()));
            }
            partAdapter = new PartAdapter(partsArray);
            partListView.setAdapter(partAdapter);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error handling json of workspace's parts");
            e.printStackTrace();
            Log.i(LOG_TAG, "Error message: " + e.getMessage());
        }
    }

    /**
     * @return
     * @see com.docdoku.android.plm.client.SimpleActionBarActivity#getActivityButtonId()
     */
    @Override
    protected int getActivityButtonId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}