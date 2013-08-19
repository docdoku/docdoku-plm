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
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.docdoku.android.plm.client.NavigationHistory;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.SearchActionBarActivity;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

        documentListView = (ListView) findViewById(R.id.elementList);
        Log.i("com.docdoku.android.plm.client", "Loading navigation history from preference path: " + getCurrentWorkspace() + PREFERENCE_DOCUMENT_HISTORY);
        navigationHistory = new NavigationHistory(getSharedPreferences(getCurrentWorkspace() + PREFERENCE_DOCUMENT_HISTORY, MODE_PRIVATE));
    }

    protected void removeLoadingView(){
        View loading = findViewById(R.id.loading);
        if (loading != null){
            ((ViewGroup) loading.getParent()).removeView(loading);
        }
    }

    /**
     * SearchActionBarActivity methods
     */
    @Override
    protected int getSearchQueryHintId() {
        return R.string.documentSearchById;  //To change body of implemented methods use File | Settings | File Templates.
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

        protected List<Document> documents;
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
                String checkOutUserName = doc.getCheckOutUserName();
                if (checkOutUserName != null){
                    String checkOutUserLogin = doc.getCheckOutUserLogin();
                    if (checkOutUserLogin.equals(getCurrentUserLogin())){
                        checkedInOutImage.setImageResource(R.drawable.checked_out_current_user_light);
                    }
                }
                else{
                    checkedInOutImage.setImageResource(R.drawable.checked_in_light);
                }
                int docNumAttachedFiles = doc.getNumberOfFiles();
                if (docNumAttachedFiles == 0){
                    View numAttachedFiles = documentRowView.findViewById(R.id.attachedFilesIndicator);
                    ((ViewGroup) numAttachedFiles.getParent()).removeView(numAttachedFiles);
                }else{
                    TextView numAttachedFiles = (TextView) documentRowView.findViewById(R.id.numAttachedFiles);
                    numAttachedFiles.setText(" " + docNumAttachedFiles);
                }
                TextView iterationNumber = (TextView) documentRowView.findViewById(R.id.iterationNumber);
                iterationNumber.setText("" + doc.getIterationNumber());
                TextView lastIteration = (TextView) documentRowView.findViewById(R.id.lastIteration);
                try {
                    lastIteration.setText(String.format(getResources().getString(R.string.documentIterationPhrase, simplifyDate(doc.getLastIterationDate()), doc.getLastIterationAuthorName())));
                } catch (ParseException e) {
                    lastIteration.setText(" ");
                    Log.i("com.docdoku.android.plm", "Unable to correctly get a date for document (ParseException)" + doc.getIdentification());
                }catch(NullPointerException e){
                    lastIteration.setText(" ");
                    Log.i("com.docdoku.android.plm", "Unable to correctly get a date for document (NullPointerException)" + doc.getIdentification());
                }
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

    protected String simplifyDate(String dateString) throws ParseException, NullPointerException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
        Calendar date = Calendar.getInstance();
        date.setTime(simpleDateFormat.parse(dateString));
        Calendar currentTime = Calendar.getInstance();
        if (currentTime.get(Calendar.YEAR) == date.get(Calendar.YEAR)){
            int dayDifference = currentTime.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR);
            if (dayDifference == 0){
                return getResources().getString(R.string.today);
            }
            if (dayDifference == 1){
                return getResources().getString(R.string.yesterday);
            }
        }
        String timeDifference = DateUtils.getRelativeTimeSpanString(this, date.getTimeInMillis(), true).toString();
        return timeDifference;
    }
}
