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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.SearchActivity;

/**
 * {@code Activity} used for doing an advanced search of parts.
 * <p>The criteria that the user may use for a part search are:
 * <br>Part reference
 * <br>Title
 * <br>Version
 * <br>Author
 * <br>Minimum creation date
 * <br>Maximum creation date
 * <p>Layout file: {@link /res/layout/activity_search.xml activity_search}
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public class PartSearchActivity extends SearchActivity {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.parts.PartSearchActivity";

    private EditText partKey, partTitle, partVersion;

    /**
     * Called when the {@code Activity} is created
     * <p>Set the {@code OnClickListener} on the {@code Button} that starts the search, so that it creates the {@code String}
     * to be passed in the server url to execute the query.
     * <br>This {@code String} is then passed in an {@code Intent} to a {@link PartSimpleListActivity}, specifying that
     * it should display the search results.
     *
     * @param savedInstanceState
     * @see android.app.Activity
     * @see SearchActivity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView idTitle = (TextView) findViewById(R.id.idTitle); idTitle.setText(R.string.partKey);
        TextView titleTitle = (TextView) findViewById(R.id.titleTitle); titleTitle.setText(R.string.partTitle);
        TextView versionTitle = (TextView) findViewById(R.id.versionTitle); versionTitle.setText(R.string.partVersion);
        TextView authorTitle = (TextView) findViewById(R.id.authorTitle); authorTitle.setText(R.string.partAuthor);
        TextView creationDateMinTitle = (TextView) findViewById(R.id.creationDateMinTitle); creationDateMinTitle.setText(R.string.partCreationDateMin);
        TextView creationDateMaxTitle = (TextView) findViewById(R.id.creationDateMaxTitle); creationDateMaxTitle.setText(R.string.partCreationDateMax);

        partKey = (EditText) findViewById(R.id.id);
        partTitle = (EditText) findViewById(R.id.title);
        partVersion = (EditText) findViewById(R.id.version);

        Button doSearch = (Button) findViewById(R.id.doSearch); doSearch.setText(R.string.partSearchStart);
        doSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchQuery = "";
                String reference = partKey.getText().toString();
                searchQuery +="number="+reference;
                String title = partTitle.getText().toString();
                searchQuery +="&name="+title;
                String versions = partVersion.getText().toString();
                searchQuery += "&version="+versions;
                if (selectedUser != null){
                    searchQuery += "&author="+selectedUser.getLogin();
                }
                if (!minCreationDate.getText().equals("")){
                    String minDateString = Long.toString(minDate.getTimeInMillis());
                    searchQuery += "&from="+ minDateString;
                }
                if (!maxCreationDate.getText().equals("")){
                    String maxDateString = Long.toString(maxDate.getTimeInMillis());
                    searchQuery += "&to="+ maxDateString;
                }
                Log.i(LOG_TAG, "Part search query: " + searchQuery);
                Intent intent = new Intent(PartSearchActivity.this, PartSimpleListActivity.class);
                intent.putExtra(PartSimpleListActivity.LIST_MODE_EXTRA, PartSimpleListActivity.PART_SEARCH);
                intent.putExtra(PartSimpleListActivity.SEARCH_QUERY_EXTRA, searchQuery);
                startActivity(intent);
            }
        });
    }

    /**
     *
     * @return
     * @see com.docdoku.android.plm.client.SimpleActionBarActivity#getActivityButtonId()
     */
    @Override
    protected int getActivityButtonId() {
        return R.id.partSearch;  //To change body of implemented methods use File | Settings | File Templates.
    }
}