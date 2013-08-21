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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.SearchActionBarActivity;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 *
 * @author: Martin Devillers
 */
public class PartSimpleListActivity extends PartListActivity implements HttpGetListener {

    public static final String  LIST_MODE_EXTRA = "list mode";
    public static final int ALL_PARTS_LIST = 0;
    public static final int PART_SEARCH = 2;
    public static final String SEARCH_QUERY_EXTRA = "search query";

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
            Log.e("docdoku.DocDokuPLM", "Error handling json of workspace's parts");
            e.printStackTrace();
            Log.i("docdoku.DocDokuPLM", "Error message: " + e.getMessage());
        }
    }

    @Override
    protected int getActivityButtonId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}