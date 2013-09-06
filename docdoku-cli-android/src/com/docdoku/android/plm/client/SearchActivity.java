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

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import com.docdoku.android.plm.client.users.User;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Basic structure for {@link com.docdoku.android.plm.client.documents.DocumentSearchActivity DocumentSearchActivity}
 * and {@link com.docdoku.android.plm.client.parts.PartSearchActivity PartSeachActivity}.
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public abstract class SearchActivity extends SimpleActionBarActivity implements HttpGetTask.HttpGetListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.SearchActivity";

    private Button author;
    protected Button minCreationDate;
    protected Button maxCreationDate;

    private ArrayList<User> users;
    protected User selectedUser;
    protected Calendar minDate, maxDate;
    private AsyncTask<String, Void, String> userLoadTask;

    /**
     * Called when the <code>Activity</code> is created
     * <p>Starts an {@link HttpGetTask} to request the list of users for the current workspace
     * <br>Sets the listeners for the search criterion that open a <code>Dialog</code> when choosing them (author, maxDate, minDate)
     * @param savedInstanceState
     * @see android.app.Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        userLoadTask  = new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/users/");

        author = (Button) findViewById(R.id.author);
        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserPickerDialog();
            }
        });

        minDate = Calendar.getInstance();
        minCreationDate = (Button) findViewById(R.id.creationDateMin);
        minCreationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerFragment(minCreationDate, minDate).show(getSupportFragmentManager(), "tagMin");
            }
        });
        maxDate = Calendar.getInstance();
        maxCreationDate = (Button) findViewById(R.id.creationDateMax);
        maxCreationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerFragment(maxCreationDate, maxDate).show(getSupportFragmentManager(), "tagMax");
            }
        });
    }

    /**
     * Shows a {@code AlertDialog} with the list of users so that the current can pick one.
     * <p>If the {@code ArrayList} of users has not yet been downloaded, the method checks if the {@link HttpGetTask} user download task is still running.
     * <br>If it is, then the dialog indicates to the user that the workspace's users have not yet been downloaded.
     * <br>If it isn't, then the task is assumed to have failed, and an error message is shown to the user.
     */
    private void showUserPickerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this)
                .setTitle(R.string.documentPickAuthor);
        if (users != null) {
            builder.setItems(getUserNames(users), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int item) {
                    selectedUser = users.get(item);
                    author.setText(selectedUser.getName());
                    author.setCompoundDrawables(null, null, null, null);
                }
            });
        }else if (AsyncTask.Status.FINISHED.equals(userLoadTask.getStatus())){
            builder.setMessage(R.string.userLoadError);
        }else{
            builder.setMessage(R.string.userLoadInProgress);
        }
        builder.create().show();
    }

    /**
     * Called when the result of the <code>HttpGetTask</code> is obtained.
     * If the request was successful, the result is a <code>JSONArray</code> of the users of the workspace.
     *
     * @param result
     * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
     */
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
            Log.e(LOG_TAG, "Error handling json of workspace's users");
            e.printStackTrace();
        }
    }

    String[] getUserNames(ArrayList<User> userArray){
        String[] userNames = new String[userArray.size()];
        for (int i=0; i<userNames.length; i++){
            userNames[i] = userArray.get(i).getName();
        }
        return userNames;
    }

    /**
     * <code>DialogFragment</code> that allows the user to pick a date.
     */
    static class DatePickerFragment extends DialogFragment
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
            button.setCompoundDrawables(null, null, null, null);
        }
    }
}
