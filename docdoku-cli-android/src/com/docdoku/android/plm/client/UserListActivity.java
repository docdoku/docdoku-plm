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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *
 * @author: Martin Devillers
 */
public class UserListActivity extends SearchActionBarActivity implements HttpGetListener {

    private ListView userListView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        userListView = (ListView) findViewById(R.id.elementList);
        userListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        userListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.title_bar_user_selected, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.send_emails:
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
                        ArrayList<String> checkedEmails = new ArrayList<String>();
                        SparseBooleanArray checked = userListView.getCheckedItemPositions();
                        int size = checked.size();
                        for (int i = 0; i < size; i++) {
                            int key = checked.keyAt(i);
                            boolean value = checked.get(key);
                            if (value){
                                checkedEmails.add(((User) userListView.getItemAtPosition(checked.keyAt(i))).getEmail());
                            }
                        }
                        String[] checkedEmailsArray = new String[checkedEmails.size()];
                        checkedEmailsArray = checkedEmails.toArray(checkedEmailsArray);
                        intent.putExtra(Intent.EXTRA_EMAIL, checkedEmailsArray);
                        intent.putExtra(Intent.EXTRA_SUBJECT, getCurrentWorkspace() + "//");
                        startActivity(Intent.createChooser(intent, "Send Email"));
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/users/");
    }

    @Override
    public void onHttpGetResult(String result) {
        View loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        ArrayList<User> userArray = new ArrayList<User>();
        try {
            JSONArray usersJSON = new JSONArray(result);
            for (int i=0; i<usersJSON.length(); i++){
                JSONObject userJSON = usersJSON.getJSONObject(i);
                User user = new User(userJSON.getString("name"),userJSON.getString("email"), userJSON.getString("login"));
                userArray.add(user);
            }
            userListView.setAdapter(new UserArrayAdapter(userArray));
        } catch (JSONException e) {
            Log.e("docdoku.DocDokuPLM", "Error handling json of workspace's users");
            e.printStackTrace();
        }
    }

    private class UserArrayAdapter extends BaseAdapter {

        private ArrayList<User> users;
        private LayoutInflater inflater;

        public UserArrayAdapter(ArrayList<User> users){
            this.users = users;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int i) {
            return users.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View userRowView = inflater.inflate(R.layout.adapter_user, null);
            User user = users.get(i);
            TextView username = (TextView) userRowView.findViewById(R.id.username);
            username.setText(user.getName());
            CheckBox checkBox = (CheckBox) userRowView.findViewById(R.id.checkBox);
            if (userListView.isItemChecked(i)){
                checkBox.setChecked(true);
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    userListView.setItemChecked(i,b);
                }
            });
            return userRowView;
        }
    }
}

