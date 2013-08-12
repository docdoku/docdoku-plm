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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.docdoku.android.plm.network.HttpGetTask;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author: martindevillers
 */
public class DocumentFoldersActivity extends DocumentListActivity implements HttpGetListener{

    private static final String INTENT_KEY_FOLDER = "folder";

    private Folder[] folders;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String folder = intent.getStringExtra(INTENT_KEY_FOLDER);

        if (folder == null){
            new HttpGetTask(this).execute(getUrlWorkspaceApi() + "/folders/");
        } else {
            new HttpGetTask(this).execute(getUrlWorkspaceApi() + "/folders/" + folder + "/folders/");
        }
    }

    @Override
    protected int getActivityButtonId() {
        return R.id.documentFolders;
    }

    @Override
    public void onHttpGetResult(String result) {
        try {
            JSONArray foldersArray = new JSONArray(result);
            folders = new Folder[foldersArray.length()];
            for (int i = 0; i<foldersArray.length(); i++){
                JSONObject folderObject = foldersArray.getJSONObject(i);
                folders[i] = new Folder(folderObject.getString("name"),folderObject.getString("id"));
            }
        } catch (JSONException e) {
            Log.e("com.docdoku.android.plm", "JSONException: could not read downloaded folder names");
            folders = new Folder[0];
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        FolderAdapter adapter = new FolderAdapter();
        documentListView.setAdapter(adapter);
        documentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(DocumentFoldersActivity.this, DocumentFoldersActivity.class);
                intent.putExtra(INTENT_KEY_FOLDER, folders[i].getId());
                startActivity(intent);
            }
        });
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
            final Folder folder = folders[i];
            View rowView = getLayoutInflater().inflate(R.layout.adapter_folder, null);
            TextView folderName = (TextView) rowView.findViewById(R.id.folderName);
            folderName.setText(folder.getName());
            /*rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DocumentFoldersActivity.this, DocumentFoldersActivity.class);
                    intent.putExtra(INTENT_KEY_FOLDER, folder.getId());
                    startActivity(intent);
                }
            });*/
            return rowView;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private class Folder{

        private String name;
        private String id;

        public Folder(String name, String id){
            this.name = name;
            this.id = id;
        }

        public String getName(){
            return name;
        }

        private String getId() {
            return id;
        }
    }
}