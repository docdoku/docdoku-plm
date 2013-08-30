package com.docdoku.android.plm.client;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author: martindevillers
 */
public abstract class SearchActivity extends SimpleActionBarActivity implements HttpGetTask.HttpGetListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.SearchActivity";

    protected Button author, minCreationDate, maxCreationDate;

    protected ArrayList<User> users;
    protected User selectedUser;
    protected Calendar minDate, maxDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/users/");

        author = (Button) findViewById(R.id.author);
        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setTitle(R.string.documentPickAuthor);
                if (users != null) {
                    builder.setItems(getUserNames(users), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int item) {
                            selectedUser = users.get(item);
                            author.setText(selectedUser.getName());
                        }
                    });
                }
                builder.create().show();
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

    protected String[] getUserNames(ArrayList<User> userArray){
        String[] userNames = new String[userArray.size()];
        for (int i=0; i<userNames.length; i++){
            userNames[i] = userArray.get(i).getName();
        }
        return userNames;
    }

    protected static class DatePickerFragment extends DialogFragment
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
