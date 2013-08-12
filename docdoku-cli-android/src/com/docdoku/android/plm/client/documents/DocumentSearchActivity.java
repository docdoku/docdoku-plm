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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.SearchActivity;

/**
 *
 * @author: Martin Devillers
 */
public class DocumentSearchActivity extends SearchActivity {

    private EditText docReference, docTitle, docVersion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        docReference = (EditText) findViewById(R.id.id);
        docTitle = (EditText) findViewById(R.id.title);
        docVersion = (EditText) findViewById(R.id.version);

        Button doSearch = (Button) findViewById(R.id.doSearch);
        doSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchQuery = "";
                String reference = docReference.getText().toString();
                searchQuery +="id="+reference;
                String title = docTitle.getText().toString();
                searchQuery +="&title="+title;
                String versions = docVersion.getText().toString();
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
                Log.i("com.docdoku.android.plm.client", "Document search query: " + searchQuery);
                Intent intent = new Intent(DocumentSearchActivity.this, DocumentSimpleListActivity.class);
                intent.putExtra(DocumentSimpleListActivity.LIST_MODE_EXTRA, DocumentSimpleListActivity.SEARCH_RESULTS_LIST);
                intent.putExtra(DocumentSimpleListActivity.SEARCH_QUERY_EXTRA, searchQuery);
                startActivity(intent);
            }
        });
    }

    @Override
    protected int getActivityButtonId() {
        return R.id.documentSearch;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
