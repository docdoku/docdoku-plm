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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author: Martin Devillers
 */
public class DocumentSearchActivity extends ActionBarActivity implements HttpGetListener {

    private EditText docReference, docTitle, docVersion;
    private Button docAuthor, docMinCreationDate, docMaxCreationDate;

    private ArrayList<User> users;
    private User selectedUser;
    private Calendar minDate, maxDate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_search);

        new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/users/");

        docReference = (EditText) findViewById(R.id.docReference);
        docTitle = (EditText) findViewById(R.id.docTitle);
        docVersion = (EditText) findViewById(R.id.docVersion);
        docAuthor = (Button) findViewById(R.id.docAuthor);

        final Activity activity = this;
        docAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.documentPickAuthor);
                if (users != null){
                    builder.setItems(getUserNames(users), new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialogInterface, int item) {
                            selectedUser = users.get(item);
                            docAuthor.setText(selectedUser.getName());
                        }
                    });
                }
                builder.create().show();
            }
        });
        minDate = Calendar.getInstance();
        docMinCreationDate = (Button) findViewById(R.id.docCreationDateMin);
        docMinCreationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerFragment(docMinCreationDate, minDate).show(getSupportFragmentManager(), "tagMin");
            }
        });
        maxDate = Calendar.getInstance();
        docMaxCreationDate = (Button) findViewById(R.id.docCreationDateMax);
        docMaxCreationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerFragment(docMaxCreationDate, maxDate).show(getSupportFragmentManager(), "tagMax");
            }
        });

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
                if (!docMinCreationDate.getText().equals("")){
                    String minDateString = Long.toString(minDate.getTimeInMillis());
                    searchQuery += "&from="+ minDateString;
                }
                if (!docMaxCreationDate.getText().equals("")){
                    String maxDateString = Long.toString(maxDate.getTimeInMillis());
                    searchQuery += "&to="+ maxDateString;
                }
                Log.i("docDoku.DocDokuPLM", "Document search query: " + searchQuery);
                Intent intent = new Intent(DocumentSearchActivity.this, DocumentListActivity.class);
                intent.putExtra(DocumentListActivity.LIST_MODE_EXTRA,DocumentListActivity.SEARCH_RESULTS_LIST);
                intent.putExtra(DocumentListActivity.SEARCH_QUERY_EXTRA, searchQuery);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onHttpGetResult(String result) {
        users = new ArrayList<User>();
        try {
            JSONArray usersJSON = new JSONArray(result);
            for (int i=0; i<usersJSON.length(); i++){
                JSONObject userJSON = usersJSON.getJSONObject(i);
                User user = new User(userJSON.getString("name"),userJSON.getString("email"),userJSON.getString("login"));
                users.add(user);
            }
        } catch (JSONException e) {
            Log.e("docdoku.DocDokuPLM", "Error handling json of workspace's users");
            e.printStackTrace();
        }
    }

    private String[] getUserNames(ArrayList<User> userArray){
        String[] userNames = new String[userArray.size()];
        for (int i=0; i<userNames.length; i++){
            userNames[i] = userArray.get(i).getName();
        }
        return userNames;
    }

    private static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        Button button;
        Calendar date;

        public DatePickerFragment(Button button, Calendar date){
            this.button = button;
            this.date = date;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (!button.getText().equals("")){
                return new DatePickerDialog(getActivity(), this, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
            }
            else{
                // Use the current date as the default date in the picker
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Create a new instance of DatePickerDialog and return it
                return new DatePickerDialog(getActivity(), this, year, month, day);
            }
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            date.set(year, month, day, 0, 0);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
            button.setText(simpleDateFormat.format(date.getTime()));
        }
    }
}
