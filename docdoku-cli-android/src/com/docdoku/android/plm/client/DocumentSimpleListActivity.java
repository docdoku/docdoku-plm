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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *
 * @author: Martin Devillers
 */
public class DocumentSimpleListActivity extends DocumentListActivity implements HttpGetListener {

    public static final String LIST_MODE_EXTRA = "list mode";
    public static final String SEARCH_QUERY_EXTRA = "search query";
    public static final int CHECKED_OUT_DOCUMENTS_LIST = 1;
    public static final int SEARCH_RESULTS_LIST = 2;

    private int activityIconId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("com.docdoku.android.plm.client", "DocumentSimpleListActivity starting");

        Intent intent = getIntent();
        int listType = intent.getIntExtra(LIST_MODE_EXTRA, 0);
        switch(listType){
            case CHECKED_OUT_DOCUMENTS_LIST:
                activityIconId = R.id.checkedOutDocuments;
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/checkedouts/" + getCurrentUserLogin() + "/documents/");
                break;
            case SEARCH_RESULTS_LIST:
                activityIconId = R.id.documentSearch;
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/search/" + intent.getStringExtra(SEARCH_QUERY_EXTRA) + "/documents/");
                break;
        }
    }

    @Override
    public void onHttpGetResult(String result) {
        removeLoadingView();
        ArrayList<Document> docsArray = new ArrayList<Document>();
        try {
            JSONArray docsJSON = new JSONArray(result);
            for (int i=0; i<docsJSON.length(); i++){
                JSONObject docJSON = docsJSON.getJSONObject(i);
                Document doc = new Document(docJSON.getString("id"));
                doc.setStateChangeNotification(docJSON.getBoolean("stateSubscription"));
                doc.setIterationNotification(docJSON.getBoolean("iterationSubscription"));
                doc.updateFromJSON(docJSON, getResources());
                docsArray.add(doc);
            }
            documentListView.setAdapter(new DocumentAdapter(docsArray));
        } catch (JSONException e) {
            Log.e("com.docdoku.android.plm.client", "Error handling json of workspace's documents");
            e.printStackTrace();
            Log.i("com.docdoku.android.plm.client", "Error message: " + e.getMessage());
        }
    }

    @Override
    protected int getActivityButtonId() {
        return activityIconId;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
