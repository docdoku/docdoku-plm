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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: martindevillers
 */
public class DocumentFoldersActivity extends DocumentListActivity implements HttpGetTask.HttpGetListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.documents.DocumentFoldersActivity";

    private static final String INTENT_KEY_FOLDER = "folder";

    private Folder[] folders;
    private String currentFolderId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentFolderId = intent.getStringExtra(INTENT_KEY_FOLDER);

        if (currentFolderId == null){
            new HttpGetTask(this).execute(getUrlWorkspaceApi() + "/folders/");
        } else {
            new HttpGetTask(this).execute(getUrlWorkspaceApi() + "/folders/" + currentFolderId + "/folders/");
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
            if (currentFolderId == null){
                folders = new Folder[foldersArray.length()+1];
                folders[0] = new Folder(getCurrentUserLogin(), getCurrentWorkspace() + ":~" + getCurrentUserLogin());
            }else{
                folders = new Folder[foldersArray.length()];
            }
            for (int i = 0; i<foldersArray.length(); i++){
                JSONObject folderObject = foldersArray.getJSONObject(i);
                if (currentFolderId == null){
                    folders[i+1] = new Folder(folderObject.getString(Folder.JSON_KEY_FOLDER_NAME),folderObject.getString(Folder.JSON_KEY_FOLDER_ID));
                }else{
                    folders[i] = new Folder(folderObject.getString(Folder.JSON_KEY_FOLDER_NAME),folderObject.getString(Folder.JSON_KEY_FOLDER_ID));
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: could not read downloaded folder names");
            folders = new Folder[0];
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        documentAdapter = new FolderAdapter(new ArrayList<Document>());
        final ProgressBar progressBar = new ProgressBar(this);
        documentListView.addFooterView(progressBar);
        documentListView.setAdapter(documentAdapter);
        documentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object object = documentListView.getAdapter().getItem(i);
                if (!object.getClass().equals(Document.class)){
                    Intent intent = new Intent(DocumentFoldersActivity.this, DocumentFoldersActivity.class);
                    intent.putExtra(INTENT_KEY_FOLDER, folders[i].getId());
                    startActivity(intent);
                }else{
                    onDocumentClick((Document) documentListView.getAdapter().getItem(i));
                }
            }
        });
        HttpGetTask.HttpGetListener httpGetListener = new HttpGetTask.HttpGetListener() {
            @Override
            public void onHttpGetResult(String result) {
                try {
                    JSONArray documentJSONArray = new JSONArray(result);
                    ArrayList<Document> documents = new ArrayList<Document>();
                    for (int i = 0; i<documentJSONArray.length(); i++){
                        JSONObject documentJSON = documentJSONArray.getJSONObject(i);
                        Document document = new Document(documentJSON.getString("id"));
                        document.updateFromJSON(documentJSON, getResources());
                        documents.add(document);
                    }
                    ((FolderAdapter) documentAdapter).addDocuments(documents);
                    documentAdapter.notifyDataSetChanged();
                    documentListView.removeFooterView(progressBar);
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        };
        if (currentFolderId == null){
            new HttpGetTask(httpGetListener).execute(getUrlWorkspaceApi() + "/folders/" + getCurrentWorkspace() + "/documents/");
        } else {
            new HttpGetTask(httpGetListener).execute(getUrlWorkspaceApi() + "/folders/" + currentFolderId + "/documents/");
        }
        removeLoadingView();
    }

    private class FolderAdapter extends DocumentAdapter{

        public FolderAdapter(List<Document> documents) {
            super(documents);
        }

        @Override
        public int getCount() {
            return folders.length + documents.size();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isEnabled(int position){
            return true;
        }

        @Override
        public Object getItem(int i) {
            if (i<folders.length){
                return folders[i];
            } else {
                return super.getItem(i-folders.length);
            }
        }

        @Override
        public long getItemId(int i) {
            return i;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (i<folders.length){
                final Folder folder = folders[i];
                View rowView = getLayoutInflater().inflate(R.layout.adapter_folder, null);
                TextView folderName = (TextView) rowView.findViewById(R.id.folderName);
                folderName.setText(folder.getName());
                return rowView;  //To change body of implemented methods use File | Settings | File Templates.
            } else{
                return super.getView(i-folders.length, view, viewGroup);
            }
        }

        private void addDocuments(List<Document> documents){
            this.documents = documents;
        }
    }

    private class Folder{

        public static final String JSON_KEY_FOLDER_NAME = "name";
        public static final String JSON_KEY_FOLDER_ID = "id";

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