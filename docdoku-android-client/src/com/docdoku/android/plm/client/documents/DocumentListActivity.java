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

package com.docdoku.android.plm.client.documents;

import android.content.Intent;
import android.os.AsyncTask;
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
 * Base class for {@code Activities} representing a list of {@link Document Documents}. This class contains:
 * <br>The {@link DocumentAdapter} used to represent the documents in a {@code ListView}
 * <br>The {@link #onDocumentClick(Document) onDocumentClick()} method used to handle click events on documents
 * <br>The {@link #getSearchQueryHintId()} and {@link #executeSearch(String) executeSearch()} methods used to handle
 * searches made by the user in the {@code ActionBar}.
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public abstract class DocumentListActivity extends SearchActionBarActivity {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.documents.DocumentListActivity";

    private static final String PREFERENCE_DOCUMENT_HISTORY = "document history";

    NavigationHistory navigationHistory;
    List<Document> documentArray;
    BaseAdapter documentAdapter;
    ListView documentListView;

    private AsyncTask searchTask;
    private List<Document> documentSearchResultArray;
    private DocumentAdapter documentSearchResultAdapter;

    /**
     * Called when this {@code Activity} is created.
     * <p>Initializes the {@code documentListView} and {@link NavigationHistory}
     * <br>Sets the {@code OnItemClickListener} on the {@code ListView}
     *
     * @param savedInstanceState
     * @see android.app.Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        documentListView = (ListView) findViewById(R.id.elementList);
        Log.i(LOG_TAG, "Loading navigation history from preference path: " + getCurrentWorkspace() + PREFERENCE_DOCUMENT_HISTORY);
        navigationHistory = new NavigationHistory(getSharedPreferences(getCurrentWorkspace() + PREFERENCE_DOCUMENT_HISTORY, MODE_PRIVATE));

        documentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onDocumentClick((Document) documentListView.getAdapter().getItem(i));
            }
        });
    }

    void removeLoadingView(){
        View loading = findViewById(R.id.loading);
        if (loading != null){
            ((ViewGroup) loading.getParent()).removeView(loading);
        }
    }

    /**
     * Handles a click on a {@link Document}. Adds its id to the {@link NavigationHistory} and create an {@code Intent}
     * to start a new {@link DocumentActivity}.
     *
     * @param document the document whose row was clicked
     */
    void onDocumentClick(Document document){
        navigationHistory.add(document.getIdentification());
        Intent intent = new Intent(DocumentListActivity.this, DocumentActivity.class);
        intent.putExtra(DocumentActivity.EXTRA_DOCUMENT, document);
        startActivity(intent);
    }

    /**
     *
     * @return
     * @see SearchActionBarActivity#getSearchQueryHintId()
     */
    @Override
    protected int getSearchQueryHintId() {
        return R.string.documentSearchById;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Handles user document searches by id.
     * <p>Cancels the {@link #searchTask} to stop a search query that may be running.
     * <p>If the search query is empty, the {@code Adapter} for the {@code ListView} is set to the default one contained
     * in this class: {@link #documentAdapter}.
     * <p>If it isn't empty, starts an {@link HttpGetTask} to send a part search by id request to server. Once the result
     * is obtained, a new {@link DocumentAdapter} is created with the {@link Document Documents} found in the resulting {@code JSONArray},
     * and is set for the {@code ListView} for this {@code Activity}.
     *
     * @param query the text entered in the <code>SearchActionBar</code>
     * @see SearchActionBarActivity#executeSearch(String)
     */
    @Override
    protected void executeSearch(String query) {
        if (searchTask != null){
            searchTask.cancel(true);
        }
        if (query.length()>0){
            documentSearchResultArray = new ArrayList<Document>();
            documentSearchResultAdapter = new DocumentAdapter(documentSearchResultArray);
            documentListView.setAdapter(documentSearchResultAdapter);
            HttpGetTask.HttpGetListener httpGetListener = new HttpGetTask.HttpGetListener() {
                @Override
                public void onHttpGetResult(String result) {
                    try {
                        JSONArray documentJSONArray = new JSONArray(result);
                        for (int i=0; i<documentJSONArray.length(); i++){
                            JSONObject documentJSON = documentJSONArray.getJSONObject(i);
                            Document document = new Document(documentJSON.getString("id"));
                            document.updateFromJSON(documentJSON, getResources());
                            documentSearchResultArray.add(document);
                        }
                        documentSearchResultAdapter.notifyDataSetChanged();
                    }catch (JSONException e){
                        Log.e(LOG_TAG, "Error handling json array of workspace's documents");
                        e.printStackTrace();
                        Log.i(LOG_TAG, "Error message: " + e.getMessage());
                    }
                }
            };
            searchTask = new HttpGetTask(httpGetListener).execute(getUrlWorkspaceApi() + "/search/id=" + query + "/documents");
        }
        else{
            documentListView.setAdapter(documentAdapter);
        }
    }

    /**
     * {@code BaseAdapter} implementation for handling the representation of {@link Document} rows.
     */
    class DocumentAdapter extends BaseAdapter {

        List<Document> documents;
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

        /**
         * Returns if the row at {@code position} is clickable
         * <br>If the {@code Document} at {@code position} is not {@code null} and its {@code author} is not {@code null},
         * then it is clickable.
         *
         * @param position
         * @return
         * @see BaseAdapter
         */
        @Override
        public boolean isEnabled(int position){
            try{
                Document document = documents.get(position);
                return !(document == null || document.getAuthor() == null);
            }catch(IndexOutOfBoundsException e){
                return false;
            }
        }

        /**
         * Generates the {@code View} for a row representing a {@link Document}
         * <p>If the document at position {@code i} is {@code null}, then a row with a {@code ProgressBar} is returned to
         * indicate that the {@code Document} is still being loaded.
         * <p>If the {@code Document} is not {@code null} but it's {@code author} is {@code null}, the the document loading is
         * assumed to have failed, and a row indicating an error is created.
         * <p>If the {@code Document} is correctly available:
         * <br>Its reference and last revision {@code TextView}s are set
         * <br>Its check in/out {@code ImageView} is set by comparing its {@code checkoutUserLogin} to the current user's login
         * <br>Its iteration number {@code TextView} is set
         * <br>Its number of linked files {@code TextView} is set. If that number is equal to 0, then the {@code ViewGroup}
         * indicating the number of linked files is removed.
         *
         * @param i
         * @param view
         * @param viewGroup
         * @return
         * @see BaseAdapter
         */
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            final View documentRowView;
            final Document doc = documents.get(i);
            if (doc == null){ //Document is still being loaded
                documentRowView = new ProgressBar(DocumentListActivity.this);
            }else if (doc.getAuthor() == null){ //Document load failed
                documentRowView = inflater.inflate(R.layout.adapter_document, null);
                TextView identification = (TextView) documentRowView.findViewById(R.id.identification);
                identification.setText(doc.getIdentification());
                ImageView checkedInOutImage = (ImageView) documentRowView.findViewById(R.id.checkedInOutImage);
                checkedInOutImage.setImageResource(R.drawable.error_light);
                View iterationNumberBox = documentRowView.findViewById(R.id.iterationNumberBox);
                ((ViewGroup) iterationNumberBox.getParent()).removeView(iterationNumberBox);
                View numAttachedFiles = documentRowView.findViewById(R.id.attachedFilesIndicator);
                ((ViewGroup) numAttachedFiles.getParent()).removeView(numAttachedFiles);
            }else{ //Document was loaded successfully
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
                    Log.i(LOG_TAG, "Unable to correctly get a date for document (ParseException)" + doc.getIdentification());
                }catch(NullPointerException e){
                    lastIteration.setText(" ");
                    Log.i(LOG_TAG, "Unable to correctly get a date for document (NullPointerException)" + doc.getIdentification());
                }
            }
            return documentRowView;
        }
    }

    /**
     * Method that converts a date into a {@code String} that is easier to read for the user. The possible scenarios are:
     * today, yesterday, and the date for a previous event.
     * <p>The {@code currentTime} is created, and compared to {@code date}, the date parsed from {@code dateString}:
     * <br>If they are the same year and the same day of the year, then the resource at {@code R.string.today} is returned.
     * <br>If they are the same year and {@code date} is one day before {@code currentTime}, then the resource at
     * {@code R.string.yesterday} is returned.
     * <br>Otherwise, {@code DateUtils.getRelativeTimeSpanString} is used to generate a {@code String} easily readable by the user.
     * <p>Note: if we are the first day of a new year and the {@code dateString} indicates the last day of previous year,
     * then this method will not return yesterday but instead the date. But nobody really cares.
     * @param dateString
     * @return the {@code String} to be displayed to the user
     * @throws ParseException if the {@code dateString} could not be parsed into a {@code Calendar}
     * @throws NullPointerException
     */
    String simplifyDate(String dateString) throws ParseException, NullPointerException {
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
