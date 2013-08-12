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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.docdoku.android.plm.network.HttpGetTask;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author: martindevillers
 */
public class DocumentFoldersActivity extends DocumentListActivity implements HttpGetListener{

    private String[] folders;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new HttpGetTask(this).execute(getUrlWorkspaceApi() + "/folders/");
    }

    @Override
    protected int getActivityButtonId() {
        return R.id.documentFolders;
    }

    @Override
    public void onHttpGetResult(String result) {
        try {
            JSONArray foldersArray = new JSONArray(result);
            folders = new String[foldersArray.length()];
            for (int i = 0; i<foldersArray.length(); i++){
                folders[i] = foldersArray.getJSONObject(i).getString("name");
            }
        } catch (JSONException e) {
            Log.e("com.docdoku.android.plm", "JSONException: could not read downloaded folder names");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        FolderAdapter adapter = new FolderAdapter();
        documentListView.setAdapter(adapter);
        removeLoadingView();
    }

    private class FolderAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return folders.length;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getItem(int i) {
            return folders[i];  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getItemId(int i) {
            return i;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = getLayoutInflater().inflate(R.layout.adapter_folder, null);
            TextView folderName = (TextView) rowView.findViewById(R.id.folderName);
            folderName.setText(folders[i]);
            return rowView;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}