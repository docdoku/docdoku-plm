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

import android.app.ActionBar;
import android.util.Log;
import android.view.*;
import android.widget.SearchView;
import android.widget.TextView;

/**
 *
 * @author: Martin Devillers
 */
public abstract class SearchActionBarActivity extends SimpleActionBarActivity {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.SearchActionBarActivity";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_search, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        int searchTextViewId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView searchTextView = (TextView) searchView.findViewById(searchTextViewId);
        searchTextView.setHintTextColor(getResources().getColor(R.color.darkGrey));
        searchTextView.setTextColor(R.color.darkGrey);

        int queryHintId = getSearchQueryHintId();
        if (queryHintId != 0){
            searchView.setQueryHint(getResources().getString(queryHintId));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.i(LOG_TAG, "Search query submitted: " + s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.i(LOG_TAG, "Document search query changed to: " + s);
                executeSearch(s);
                return false;
            }
        });
        return true;
    }

    protected abstract int getSearchQueryHintId();

    protected abstract void executeSearch(String query);
}
