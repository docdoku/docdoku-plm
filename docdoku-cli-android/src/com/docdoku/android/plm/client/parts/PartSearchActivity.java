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
 * @author: martindevillers
 */
public class PartSearchActivity extends SearchActivity {

    private EditText partKey, partTitle, partVersion;

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
                Log.i("docDoku.DocDokuPLM", "Part search_light query: " + searchQuery);
                Intent intent = new Intent(PartSearchActivity.this, PartListActivity1.class);
                intent.putExtra(PartListActivity1.LIST_MODE_EXTRA,PartListActivity1.PART_SEARCH);
                intent.putExtra(PartListActivity1.SEARCH_QUERY_EXTRA, searchQuery);
                startActivity(intent);
            }
        });
    }

    @Override
    protected int getActivityButtonId() {
        return R.id.partSearch;  //To change body of implemented methods use File | Settings | File Templates.
    }
}