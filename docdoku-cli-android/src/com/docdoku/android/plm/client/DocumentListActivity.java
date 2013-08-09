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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: martindevillers
 */
public abstract class DocumentListActivity extends SearchActionBarActivity {

    private static final String PREFERENCE_DOCUMENT_HISTORY = "document history";

    protected NavigationHistory navigationHistory;
    protected List<Document> documentArray;
    protected DocumentAdapter documentAdapter;
    protected ListView documentListView;

    private List<Document> documentSearchResultArray;
    private DocumentAdapter documentSearchResultAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        View loading = findViewById(R.id.loading);
        ((ViewGroup) loading.getParent()).removeView(loading);

        documentListView = (ListView) findViewById(R.id.elementList);
        Log.i("com.docdoku.android.plm.client", "Loading navigation history from preference path: " + getCurrentWorkspace() + PREFERENCE_DOCUMENT_HISTORY);
        navigationHistory = new NavigationHistory(getSharedPreferences(getCurrentWorkspace() + PREFERENCE_DOCUMENT_HISTORY, MODE_PRIVATE));
    }

    /**
     * SearchActionBarActivity methods
     */
    @Override
    protected int getSearchQueryHintId() {
        return R.string.searchDocumentById;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void executeSearch(String query) {
        if (query.length()>0){
            documentSearchResultArray = new ArrayList<Document>();
            documentSearchResultAdapter = new DocumentAdapter(documentSearchResultArray);
            documentListView.setAdapter(documentSearchResultAdapter);
            HttpGetListener httpGetListener = new HttpGetListener() {
                @Override
                public void onHttpGetResult(String result) {
                    try {
                        JSONArray partsJSON = new JSONArray(result);
                        for (int i=0; i<partsJSON.length(); i++){
                            JSONObject partJSON = partsJSON.getJSONObject(i);
                            Document part = new Document(partJSON.getString("id"));
                            part.updateFromJSON(partJSON, getResources());
                            documentSearchResultArray.add(part);
                        }
                        documentSearchResultAdapter.notifyDataSetChanged();
                    }catch (JSONException e){
                        Log.e("com.docdoku.android.plm.client", "Error handling json array of workspace's documents");
                        e.printStackTrace();
                        Log.i("com.docdoku.android.plm.client", "Error message: " + e.getMessage());
                    }
                }
            };
            new HttpGetTask(httpGetListener).execute(getUrlWorkspaceApi() + "/search/id=" + query + "/documents");
        }
        else{
            documentListView.setAdapter(documentAdapter);
        }
    }

    protected class DocumentAdapter extends BaseAdapter {

        private List<Document> documents;
        private LayoutInflater inflater;

        public DocumentAdapter(List<Document> documents){
            this.documents = documents;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return documents.size();
        }

        @Override
        public Object getItem(int i) {
            return documents.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            final View documentRowView;
            final Document doc = documents.get(i);
            if (doc != null){
                documentRowView = inflater.inflate(R.layout.adapter_document, null);
                TextView identification = (TextView) documentRowView.findViewById(R.id.identification);
                identification.setText(doc.getIdentification());
                ImageView checkedInOutImage = (ImageView) documentRowView.findViewById(R.id.checkedInOutImage);
                //TextView checkOutUser = (TextView) documentRowView.findViewById(R.id.checkOutUser);
                String checkOutUserName = doc.getCheckOutUserName();
                if (checkOutUserName != null){
                    String checkOutUserLogin = doc.getCheckOutUserLogin();
                    if (checkOutUserLogin.equals(getCurrentUserLogin())){
                        checkedInOutImage.setImageResource(R.drawable.checked_out_current_user_light);
                    }
                    //checkOutUser.setText(checkOutUserName);
                }
                else{
                    //checkOutUser.setText("");
                    checkedInOutImage.setImageResource(R.drawable.checked_in_light);
                }
                TextView numAttachedFiles = (TextView) documentRowView.findViewById(R.id.numAttachedFiles);
                numAttachedFiles.setText(" " + doc.getNumberOfFiles());
                documentRowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        documentRowView.setBackgroundResource(R.drawable.clickable_item_background);
                        navigationHistory.add(doc.getIdentification());
                        Intent intent = new Intent(DocumentListActivity.this, DocumentActivity.class);
                        intent.putExtra(DocumentActivity.EXTRA_DOCUMENT, doc);
                        startActivity(intent);
                    }
                });
                } else {
                documentRowView = new ProgressBar(DocumentListActivity.this);
            }
            return documentRowView;
        }
    }
}
