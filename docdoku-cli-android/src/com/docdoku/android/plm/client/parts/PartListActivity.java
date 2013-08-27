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
 * @author: martindevillers
 */
public abstract class PartListActivity extends SearchActionBarActivity {

    private static final String PREFERENCE_PART_HISTORY = "part history";

    protected NavigationHistory navigationHistory;
    protected List<Part> partsArray;
    protected PartAdapter partAdapter;
    protected ListView partListView;
    protected View loading;

    private AsyncTask searchTask;
    private List<Part> partSearchResultArray;
    private PartAdapter partSearchResultAdapter;

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

    private void onPartClick(Part part){
        if (part == null || part.getAuthor() == null){
            //Do nothing
        }else{
            navigationHistory.add(part.getKey());
            Intent intent = new Intent(PartListActivity.this, PartActivity.class);
            intent.putExtra(PartActivity.PART_EXTRA,part);
            startActivity(intent);
        }
    }

    protected class PartAdapter extends BaseAdapter {

        private List<Part> parts;
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

    /**
     * SearchActionBarActivity methods
     */
    @Override
    protected int getSearchQueryHintId() {
        return R.string.partSearchByKey;
    }

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
                        Log.e("com.docdoku.android.plm.client", "Error handling json array of workspace's parts");
                        e.printStackTrace();
                        Log.i("com.docdoku.android.plm.client", "Error message: " + e.getMessage());
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
