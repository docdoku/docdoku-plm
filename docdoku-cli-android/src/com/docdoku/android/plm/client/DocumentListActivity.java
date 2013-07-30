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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 *
 * @author: Martin Devillers
 */
public class DocumentListActivity extends SearchActionBarActivity implements HttpGetListener {

    public static final String LIST_MODE_EXTRA = "list mode";
    public static final String SEARCH_QUERY_EXTRA = "search query";
    public static final int ALL_DOCUMENTS_LIST = 0;
    public static final int RECENTLY_VIEWED_DOCUMENTS_LIST = 1;
    public static final int CHECKED_OUT_DOCUMENTS_LIST = 2;
    public static final int SEARCH_RESULTS_LIST = 3;
    private static final String HISTORY_SIZE = "Document history size";
    private static final String DOCUMENT_HISTORY_PREFERENCE = "documents";
    private static final int MAX_DOCUMENTS_IN_HISTORY = 15;

    private ListView documentListView;
    private AsyncTask documentQueryTask;
    private LinkedHashSet<String> documentReferenceHistory;
    private View loading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        Log.i("com.docdoku.android.plm.client", "DocumentListActivity starting");

        documentListView = (ListView) findViewById(R.id.elementList);
        loading = findViewById(R.id.loading);

        getDocumentHistory();

        Intent intent = getIntent();
        int listType = intent.getIntExtra(LIST_MODE_EXTRA, 0);
        switch(listType){
            case ALL_DOCUMENTS_LIST:
                documentQueryTask = new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/search/id=/documents/");
                break;
            case RECENTLY_VIEWED_DOCUMENTS_LIST:
                ((ViewGroup) loading.getParent()).removeView(loading);
                loading = null;
                ArrayList<Document> documentArray= new ArrayList<Document>();
                DocumentAdapter adapter = new DocumentAdapter(documentArray, this);
                Iterator<String> iterator = documentReferenceHistory.iterator();
                while (iterator.hasNext()){
                    Document document = new Document(iterator.next());
                    documentArray.add(document);
                    getDocumentInformation(document);
                }
                documentListView.setAdapter(adapter);
                break;
            case CHECKED_OUT_DOCUMENTS_LIST:
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/checkedouts/" + getCurrentUserLogin() + "/documents/");
                break;
            case SEARCH_RESULTS_LIST:
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/search/" + intent.getStringExtra(SEARCH_QUERY_EXTRA) + "/documents/");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(getResources().getString(R.string.documentSearchPrompt));
        final HttpGetListener httpGetListener = this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.i("com.docdoku.android.plm.client", "Document search query launched: " + s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (documentQueryTask != null){
                    documentQueryTask.cancel(true);
                }
                documentQueryTask = new HttpGetTask(httpGetListener).execute("api/workspaces/" + getCurrentWorkspace() + "/search/id=" + s + "/documents/");
                Log.i("com.docdoku.android.plm.client", "Document search query changed to: " + s);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onHttpGetResult(String result) {
        if (loading !=null){
            ((ViewGroup) loading.getParent()).removeView(loading);
            loading = null;
        }
        ArrayList<Document> docsArray = new ArrayList<Document>();
        try {
            JSONArray docsJSON = new JSONArray(result);
            for (int i=0; i<docsJSON.length(); i++){
                JSONObject docJSON = docsJSON.getJSONObject(i);
                Document doc = new Document(docJSON.getString("id"));
                doc.setStateChangeNotification(docJSON.getBoolean("stateSubscription"));
                doc.setIterationNotification(docJSON.getBoolean("iterationSubscription"));
                updateDocumentFromJSON(docJSON,doc);
                docsArray.add(doc);
            }
            documentListView.setAdapter(new DocumentAdapter(docsArray, this));
        } catch (JSONException e) {
            Log.e("com.docdoku.android.plm.client", "Error handling json of workspace's documents");
            e.printStackTrace();
            Log.i("com.docdoku.android.plm.client", "Error message: " + e.getMessage());
        }
    }

    private Document updateDocumentFromJSON(JSONObject documentJSON, Document document) throws JSONException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
        Object checkOutUser = documentJSON.get("checkOutUser");
        if (checkOutUser != JSONObject.NULL){
            document.setCheckOut(
                    ((JSONObject) checkOutUser).getString("name"),
                    ((JSONObject) checkOutUser).getString("login"),
                    dateFormat.format(new Date(Long.valueOf(documentJSON.getString("checkOutDate"))))
            );
        }
        document.setDocumentDetails(
                documentJSON.getString("path"),
                documentJSON.getJSONObject("author").getString("name"),
                dateFormat.format(new Date(Long.valueOf(documentJSON.getString("creationDate")))),
                documentJSON.getString("type"),
                documentJSON.getString("title"),
                documentJSON.getString("lifeCycleState"),
                documentJSON.getString("description")
        );
        Object lastIteration = documentJSON.get("lastIteration");
        if (!lastIteration.equals(JSONObject.NULL)){
            JSONArray attachedFiles = ((JSONObject) lastIteration).getJSONArray("attachedFiles");
            String[] files = new String[attachedFiles.length()];
            for (int i = 0; i<files.length; i++){
                files[i] = attachedFiles.getString(i);
                Log.i("com.docdoku.android.plm.client", "File found: " + files[i]);
            }
            document.setFiles(files);
            JSONArray linkedDocuments = ((JSONObject) lastIteration).getJSONArray("linkedDocuments");
            String[] documents = new String[linkedDocuments.length()];
            for (int i = 0; i<documents.length; i++){
                documents[i] = linkedDocuments.getJSONObject(i).getString("documentMasterId") + "-" + linkedDocuments.getJSONObject(i).getString("documentMasterVersion");
            }
            document.setLinkedDocuments(documents);
            document.setLastRevision(((JSONObject) lastIteration).getInt("iteration"),
                    ((JSONObject) lastIteration).getString("revisionNote"),
                    ((JSONObject) lastIteration).getJSONObject("author").getString("name"),
                    dateFormat.format(new Date(Long.valueOf(((JSONObject) lastIteration).getString("creationDate")))));
            JSONArray attributes = ((JSONObject) lastIteration).getJSONArray("instanceAttributes");
            String[] attributeNames = new String[attributes.length()];
            String[] attributeValues = new String[attributeNames.length];
            for (int i = 0; i<attributeNames.length; i++){
                JSONObject attribute = attributes.getJSONObject(i);
                attributeNames[i] = attribute.getString("name");
                attributeValues[i] = attribute.getString("value");
            }
            document.setAttributes(attributeNames, attributeValues);
        }
        return document;
    }

    private void getDocumentHistory(){
        documentReferenceHistory = new LinkedHashSet<String>();
        SharedPreferences preferences = getSharedPreferences(getCurrentWorkspace() + DOCUMENT_HISTORY_PREFERENCE, MODE_PRIVATE);
        int numDocumentsInHistory = preferences.getInt(HISTORY_SIZE, 0);
        for (int i = 0; i<numDocumentsInHistory; i++){
            Log.i("com.docdoku.android.plm.client","Retreiving document reference at position " + i + ": " + preferences.getString(Integer.toString(i),""));
            documentReferenceHistory.add(preferences.getString(Integer.toString(i), ""));
        }
    }

    private synchronized void getDocumentInformation(final Document document){
        new HttpGetTask(new HttpGetListener() {
            @Override
            public void onHttpGetResult(String result) {
                try {
                    JSONObject partJSON = new JSONObject(result);
                    updateDocumentFromJSON(partJSON, document);
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getReference());
    }

    private void updatePartHistory(String reference){
        Log.i("com.docdoku.android.plm.client", "Adding document " + reference +" to history");
        documentReferenceHistory.add(reference);
        SharedPreferences preferences = getSharedPreferences(getCurrentWorkspace() + DOCUMENT_HISTORY_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Iterator<String> iterator = documentReferenceHistory.iterator();
        while (documentReferenceHistory.size() > MAX_DOCUMENTS_IN_HISTORY){
            iterator.next();
            iterator.remove();
        }
        editor.putInt(HISTORY_SIZE, documentReferenceHistory.size());
        int i = documentReferenceHistory.size()-1;
        while (iterator.hasNext()){
            String next = iterator.next();
            Log.i("com.docdoku.android.plm.client", "Storing document reference " + next + " in preferences at position " + i);
            editor.putString(Integer.toString(i), next);
            i--;
        }
        editor.commit();
    }

    private class DocumentAdapter extends BaseAdapter {

        private ArrayList<Document> documents;
        private LayoutInflater inflater;
        private Activity activity;

        public DocumentAdapter(ArrayList<Document> documents, Activity activity){
            this.documents = documents;
            this.activity = activity;
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
            View documentRowView = inflater.inflate(R.layout.adapter_document, null);
            final Document doc = documents.get(i);
            TextView reference = (TextView) documentRowView.findViewById(R.id.reference);
            reference.setText(doc.getReference());
            TextView reservedBy = (TextView) documentRowView.findViewById(R.id.reservedBy);
            ImageView reservedDocument = (ImageView) documentRowView.findViewById(R.id.reservedDocument);
            String reservedByName = doc.getCheckOutUserName();
            if (reservedByName != null){
                String reservedByLogin = doc.getCheckOutUserLogin();
                if (reservedByLogin.equals(getCurrentUserLogin())){
                    reservedDocument.setImageResource(R.drawable.checked_out_current_user);
                }
                reservedBy.setText(reservedByName);
            }
            else{
                reservedBy.setText("");
                reservedDocument.setImageResource(R.drawable.checked_in);
            }
            View contentLink = documentRowView.findViewById(R.id.contentLink);
            contentLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatePartHistory(doc.getReference());
                    Intent intent = new Intent(getBaseContext(), DocumentActivity.class);
                    intent.putExtra(DocumentActivity.DOCUMENT_EXTRA, doc);
                    startActivity(intent);
                }
            });
            final CheckBox notifyStateChange = (CheckBox) documentRowView.findViewById(R.id.notifyStateChange);
            notifyStateChange.setChecked(doc.getStateChangeNotification());
            notifyStateChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean b = notifyStateChange.isChecked();
                    HttpPutListener httpPutListener = new HttpPutListener() {
                        @Override
                        public void onHttpPutResult(boolean result) {
                            doc.setStateChangeNotification(b);
                        }
                    };
                    if (b) {
                        subscriptionChangeRequested(R.string.confirmSubscribeToStateChangeNotification,
                                doc,
                                "stateChange/subscribe",
                                notifyStateChange,
                                b,
                                httpPutListener);
                    } else {
                        subscriptionChangeRequested(R.string.confirmUnsubscribeToStateChangeNotification,
                                doc,
                                "stateChange/unsubscribe",
                                notifyStateChange,
                                b,
                                httpPutListener);

                    }
                }
            });
            final CheckBox notifyIteration = (CheckBox) documentRowView.findViewById(R.id.notifyIteration);
            notifyIteration.setChecked(doc.getIterationNotification());
            notifyIteration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean b = notifyIteration.isChecked();
                    HttpPutListener httpPutListener = new HttpPutListener() {
                        @Override
                        public void onHttpPutResult(boolean result) {
                            doc.setIterationNotification(b);
                        }
                    };
                    if (b) {
                        subscriptionChangeRequested(R.string.confirmSubscribeToIterationChangeNotification,
                                doc,
                                "iterationChange/subscribe",
                                notifyIteration,
                                b,
                                httpPutListener);
                    } else {
                        subscriptionChangeRequested(R.string.confirmUnsubscribeToIterationChangeNotification,
                                doc,
                                "iterationChange/unsubscribe",
                                notifyIteration,
                                b,
                                httpPutListener);

                    }
                }
            });
            return documentRowView;
        }

        private void subscriptionChangeRequested(int messageId, final Document doc, final String urlCommand, final CheckBox checkBox, final boolean checkBoxState, final HttpPutListener httpPutListener){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(messageId);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i("docDoku.DocDokuPLM", "Subscribing to iteration change notification for document with reference " + doc.getReference());
                    new HttpPutTask(httpPutListener).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + doc.getReference() + "/notification/" + urlCommand);
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    checkBox.setChecked(!checkBoxState);
                }
            });
            builder.create().show();
        }
    }
}
