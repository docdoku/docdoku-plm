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

import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 *
 * @author: Martin Devillers
 */
public class PartListActivity extends SearchActionBarActivity implements HttpGetListener, LoaderManager.LoaderCallbacks<List<Part>> {

    private View loading;
    private ArrayList<Part> partsArray;
    private PartAdapter partAdapter;
    private ListView partListView;
    View footerProgressBar;
    private final static int PART_LOADER_ID_BASE = 100;
    private int numPartsAvailable;
    private int numPagesDownloaded;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        partListView = (ListView) findViewById(R.id.elementList);
        loading = findViewById(R.id.loading);
        ((ViewGroup) loading.getParent()).removeView(loading);

        footerProgressBar = new ProgressBar(this);
        partListView.addFooterView(footerProgressBar);

        partsArray = new ArrayList<Part>();
        partAdapter = new PartAdapter(partsArray);
        partListView.setAdapter(partAdapter);


        numPartsAvailable = 0;
        numPagesDownloaded = 0;
        new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/parts/count");

        final LoaderManager.LoaderCallbacks<List<Part>> loaderCallbacks = this;
        partListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && partAdapter.getCount() < numPartsAvailable){
                        Log.i("com.docdoku.android.plm.client", "Loading more parts");
                        Bundle bundle = new Bundle();
                        bundle.putInt("page", numPagesDownloaded);
                        bundle.putString("workspace", getCurrentWorkspace());
                        getSupportLoaderManager().initLoader(PART_LOADER_ID_BASE + numPagesDownloaded, bundle, loaderCallbacks);
                }
            }
        });

    }

    @Override
    public Loader<List<Part>> onCreateLoader(int i, Bundle bundle) {
        return new PartLoader(this, bundle.getInt("page"), bundle.getString("workspace"));  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLoadFinished(Loader<List<Part>> partLoader, List<Part> parts) {
        partsArray.addAll(parts);
        partAdapter.notifyDataSetChanged();
        numPagesDownloaded++;

        if (partAdapter.getCount() == numPartsAvailable){
            partListView.removeFooterView(footerProgressBar);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Part>> partLoader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onHttpGetResult(String result) {
        try{
            result = result.substring(0, result.length() - 1);
            numPartsAvailable = Integer.parseInt(result);
            Bundle bundle = new Bundle();
            bundle.putInt("page", 0);
            bundle.putString("workspace", getCurrentWorkspace());
            getSupportLoaderManager().initLoader(PART_LOADER_ID_BASE + 0, bundle, this);
        } catch (NumberFormatException e) {
            Log.e("com.docdoku.android.plm.client", "NumberFormatException: didn't correctly download number of pages of parts");
            Log.e("com.docdoku.android.plm.client", "Number of pages result: " + result);
            e.printStackTrace();
        }
    }

    private class PartAdapter extends BaseAdapter{

        private ArrayList<Part> parts;
        private final LayoutInflater inflater;

        public PartAdapter(ArrayList<Part> parts){
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
            View partRowView = inflater.inflate(R.layout.adapter_part, null);
            final Part part = parts.get(i);
            TextView reference = (TextView) partRowView.findViewById(R.id.number);
            reference.setText(part.getKey());
            TextView reservedBy = (TextView) partRowView.findViewById(R.id.reservedBy);
            ImageView reservedPart = (ImageView) partRowView.findViewById(R.id.reservedPart);
            String reservedByName = part.getCheckOutUserName();
            if (reservedByName != null){
                String reservedByLogin = part.getCheckOutUserLogin();
                if (reservedByLogin.equals(getCurrentUserLogin())){
                    reservedPart.setImageResource(R.drawable.checked_out_current_user);
                }
                reservedBy.setText(reservedByName);
            }
            else{
                reservedBy.setText("");
                reservedPart.setImageResource(R.drawable.checked_in);
            }
            return partRowView;
        }
    }

    private static class PartLoader extends Loader<List<Part>> implements HttpGetListener{

        private int startIndex;
        private String workspace;
        private AsyncTask asyncTask;

        public PartLoader(Context context, int page, String workspace) {
            super(context);
            startIndex = page*20;
            this.workspace = workspace;
        }

        @Override
        protected void onStartLoading (){
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts?start=" +  startIndex);
        }

        @Override
        protected void onStopLoading (){
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
        }

        @Override
        protected void onReset (){
            if (asyncTask != null){
                asyncTask.cancel(false);
            }
            asyncTask = new HttpGetTask(this).execute("api/workspaces/" + workspace + "/parts?start=" +  startIndex);
        }

        @Override
        protected void onForceLoad (){
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void onAbandon (){
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onHttpGetResult(String result) {
            List<Part> partList= new ArrayList<Part>();
            try {
                JSONArray partsJSON = new JSONArray(result);
                for (int i=0; i<partsJSON.length(); i++){
                    JSONObject partJSON = partsJSON.getJSONObject(i);
                    Part part = new Part(partJSON.getString("partKey"));
                    partList.add(part);
                }
            }catch (JSONException e){
                Log.e("docdoku.DocDokuPLM", "Error handling json of workspace's parts");
                e.printStackTrace();
                Log.i("docdoku.DocDokuPLM", "Error message: " + e.getMessage());
            }
            deliverResult(partList);
        }
    }
}