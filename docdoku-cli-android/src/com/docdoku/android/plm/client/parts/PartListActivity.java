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
 * Base class for {@code Activities} representing a list of {@link Part Parts}. This class contains:
 * <br>The {@link PartAdapter} used to represent the parts in a {@code ListView}
 * <br>The {@link #onPartClick(Part) onPartClick()} method used to handle click events on parts
 * <br>The {@link #getSearchQueryHintId()} and {@link #executeSearch(String) executeSearch()} methods used to handle
 * searches made by the user in the {@code ActionBar}.
 *
 * @author: martindevillers
 */
public abstract class PartListActivity extends SearchActionBarActivity {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.parts.PartListActivity";

    private static final String PREFERENCE_PART_HISTORY = "part history";

    NavigationHistory navigationHistory;
    List<Part> partsArray;
    PartAdapter partAdapter;
    ListView partListView;
    View loading;

    private AsyncTask searchTask;
    private List<Part> partSearchResultArray;
    private PartAdapter partSearchResultAdapter;

    /**
     * Called when this {@code Activity} is created.
     * <p>Initializes the {@code partListView} and {@link NavigationHistory}
     * <br>Sets the {@code OnItemClickListener} on the {@code ListView}
     *
     * @param savedInstanceState
     * @see android.app.Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        loading = findViewById(R.id.loading);

        partListView = (ListView) findViewById(R.id.elementList);
        navigationHistory = new NavigationHistory(getSharedPreferences(getCurrentWorkspace() + PREFERENCE_PART_HISTORY, MODE_PRIVATE));
        partListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onPartClick((Part) partListView.getAdapter().getItem(i));
            }
        });
    }

    /**
     * Handles a click on a {@link Part}. Adds its id to the {@link NavigationHistory} and create an {@code Intent}
     * to start a new {@link PartActivity}.
     *
     * @param part the part whose row was clicked
     */
    private void onPartClick(Part part){
        navigationHistory.add(part.getKey());
        Intent intent = new Intent(PartListActivity.this, PartActivity.class);
        intent.putExtra(PartActivity.PART_EXTRA,part);
        startActivity(intent);
    }

    /**
     * {@code BaseAdapter} implementation for handling the representation of {@link Part} rows.
     */
    protected class PartAdapter extends BaseAdapter {

        private final List<Part> parts;
        private LayoutInflater inflater;

        public PartAdapter(List<Part> parts){
            this.parts = parts;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return parts.size();
        }

        @Override
        public Object getItem(int i) {
            return parts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        /**
         * Returns if the row at {@code position} is clickable
         * <br>If the {@code Part} at {@code position} is not {@code null} and its {@code author} is not {@code null},
         * then it is clickable.
         *
         * @param position
         * @return
         * @see BaseAdapter
         */
        @Override
        public boolean isEnabled(int position){
            try{
                Part part = parts.get(position);
                return !(part == null || part.getAuthor() == null);
            }catch (IndexOutOfBoundsException e){
                return false;
            }
        }

        /**
         * Generates the {@code View} for a row representing a {@link Part}
         * <p>If the part at position {@code i} is {@code null}, then a row with a {@code ProgressBar} is returned to
         * indicate that the {@code Part} is still being loaded.
         * <p>If the {@code Part} is not {@code null} but it's {@code author} is {@code null}, the the part loading is
         * assumed to have failed, and a row indicating an error is created.
         * <p>If the {@code Part} is correctly available:
         * <br>Its reference and last revision {@code TextView}s are set
         * <br>Its check in/out {@code ImageView} is set by comparing its {@code checkoutUserLogin} to the current user's login
         * <br>Its iteration number {@code TextView} is set
         *
         * @param i
         * @param view
         * @param viewGroup
         * @return
         * @see BaseAdapter
         */
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final View partRowView;
            final Part part = parts.get(i);
            if (part == null){
                partRowView = new ProgressBar(PartListActivity.this);
            }else if(part.getAuthor() == null){
                partRowView = inflater.inflate(R.layout.adapter_part, null);
                TextView identification = (TextView) partRowView.findViewById(R.id.identification);
                identification.setText(part.getKey());
                ImageView checkedInOutImage = (ImageView) partRowView.findViewById(R.id.checkedInOutImage);
                checkedInOutImage.setImageResource(R.drawable.error_light);
                View iterationNumberBox = partRowView.findViewById(R.id.iterationNumberBox);
                ((ViewGroup) iterationNumberBox.getParent()).removeView(iterationNumberBox);
            }else{
                partRowView = inflater.inflate(R.layout.adapter_part, null);
                TextView reference = (TextView) partRowView.findViewById(R.id.identification);
                reference.setText(part.getKey());
                ImageView reservedPart = (ImageView) partRowView.findViewById(R.id.checkedInOutImage);
                String reservedByName = part.getCheckOutUserName();
                if (reservedByName != null){
                    String reservedByLogin = part.getCheckOutUserLogin();
                    if (reservedByLogin.equals(getCurrentUserLogin())){
                        reservedPart.setImageResource(R.drawable.checked_out_current_user_light);
                    }
                }
                else{
                    reservedPart.setImageResource(R.drawable.checked_in_light);
                }
                TextView lastIteration = (TextView) partRowView.findViewById(R.id.lastIteration);
                try {
                    lastIteration.setText(String.format(getResources().getString(R.string.documentIterationPhrase, simplifyDate(part.getLastIterationDate()), part.getLastIterationAuthorName())));
                }catch (ParseException e) {
                    lastIteration.setText("");
                }catch (NullPointerException e){
                    lastIteration.setText("");
                }
            }
            return partRowView;
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

    /**
     *
     * @return
     * @see SearchActionBarActivity#getSearchQueryHintId()
     */
    @Override
    protected int getSearchQueryHintId() {
        return R.string.partSearchByKey;
    }

    /**
     * Handles user part searches by id.
     * <p>Cancels the {@link #searchTask} to stop a search query that may be running.
     * <p>If the search query is empty, the {@code Adapter} for the {@code ListView} is set to the default one contained
     * in this class: {@link #partAdapter}.
     * <p>If it isn't empty, starts an {@link HttpGetTask} to send a part search by id request to server. Once the result
     * is obtained, a new {@link PartAdapter} is created with the {@link Part Parts} found in the resulting {@code JSONArray},
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
            partSearchResultArray = new ArrayList<Part>();
            partSearchResultAdapter = new PartAdapter(partSearchResultArray);
            partListView.setAdapter(partSearchResultAdapter);
            HttpGetTask.HttpGetListener httpGetListener = new HttpGetTask.HttpGetListener() {
                @Override
                public void onHttpGetResult(String result) {
                    try {
                        JSONArray partsJSON = new JSONArray(result);
                        for (int i=0; i<partsJSON.length(); i++){
                            JSONObject partJSON = partsJSON.getJSONObject(i);
                            Part part = new Part(partJSON.getString("partKey"));
                            part.updateFromJSON(partJSON, getResources());
                            partSearchResultArray.add(part);
                        }
                        partSearchResultAdapter.notifyDataSetChanged();
                    }catch (JSONException e){
                        Log.e(LOG_TAG, "Error handling json array of workspace's parts");
                        e.printStackTrace();
                        Log.i(LOG_TAG, "Error message: " + e.getMessage());
                    }
                }
            };
            searchTask = new HttpGetTask(httpGetListener).execute(getUrlWorkspaceApi() + "/parts/search/number=" + query);
        }
        else{
            partListView.setAdapter(partAdapter);
        }
    }
}
