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
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author: Martin Devillers
 */
public class UserListActivity extends FragmentActivity implements HttpGetTask.HttpGetListener {

    private static final int INTENT_CODE_CONTACT_PICKER = 100;

    private ArrayList<User> userArray;
    private UserArrayAdapter userArrayAdapter;
    private ListView userListView;
    private User linkedContact;

    @Override
    public void onResume(){
        super.onResume();
        if (linkedContact != null){
            searchForContactOnPhone(linkedContact);
            userArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        //Show all contacts on phone's id, name, and email
        /*String result = "Email contacts on phone: ";
        Cursor contacts = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, null, null);
        while (contacts.moveToNext()){
            String id = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Email._ID));
            String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME));
            String address = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
            result += "\nid: " + id + ", name: " + name + ", address: " + address;
        }
        Log.i("com.docdoku.android.plm", result);*/

        userListView = (ListView) findViewById(R.id.elementList);
        View headerView = getLayoutInflater().inflate(R.layout.header_users, null);
        headerView.findViewById(R.id.selectAllUsers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int numUsers = userArrayAdapter.getCount();
                Log.i("com.docdoku.android.plm", "Current number of users in list: " + numUsers);
                if (getNumSelectedUsers() == numUsers){
                    for (int j=0; j < numUsers; j++) {
                        userListView.setItemChecked(j, false);
                    }
                }else{
                    for (int i=0; i < numUsers; i++) {
                        userListView.setItemChecked(i, true);
                    }
                }
            }
        });
        userListView.addHeaderView(headerView);
        userListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        userListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                int numSelectedUsers = getNumSelectedUsers();
                Log.i("com.docdoku.android.plm", numSelectedUsers + " users now selected");
                if (numSelectedUsers>1){
                    if (selectedUsersHavePhoneNumber()){
                        Log.i("com.docdoku.android.plm", "Removing call option");
                        setMenu(R.menu.action_bar_users_selected, actionMode);
                    }else{
                        Log.i("com.docdoku.android.plm", "Removing phone related options");
                        setMenu(R.menu.action_bar_users_selected_nonexistent, actionMode);
                    }
                }else {
                    User selectedUser = getSelectedUser();
                    if (selectedUser != null){
                        if (selectedUser.existsOnPhone()){
                            Log.i("com.docdoku.android.plm", "Adding call option");
                            setMenu(R.menu.action_bar_user_selected, actionMode);
                        }else{
                            Log.i("com.docdoku.android.plm", "Adding create user option");
                            setMenu(R.menu.action_bar_user_selected_nonexistent, actionMode);
                        }
                    }
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.action_bar_user_selected, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()){
                    case R.id.sendEmails:
                        intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
                        String[] checkedEmailsArray = getSelectedUsersEmail();
                        intent.putExtra(Intent.EXTRA_EMAIL, checkedEmailsArray);
                        intent.putExtra(Intent.EXTRA_SUBJECT, getCurrentWorkspace() + "//");
                        startActivity(Intent.createChooser(intent, getResources().getString(R.string.userSendEmail)));
                        return true;
                    case R.id.call:
                        User selectedUser = getSelectedUser();
                        final String[] phoneNumbers = selectedUser.getPhoneNumbers();
                        new AlertDialog.Builder(UserListActivity.this)
                                .setTitle(R.string.userChooseNumber)
                                .setIcon(R.drawable.call_light)
                                .setNegativeButton(R.string.userCancelCall, null)
                                .setItems(phoneNumbers, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Log.i("com.docdoku.android.plm", "Calling phone number: " + phoneNumbers[i]);
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumbers[i]));
                                        startActivity(intent);
                                    }
                                })
                                .create().show();
                        break;
                    case R.id.sendSMS:
                        String receiversString = "smsto:";
                        String[] receivers = getSelectedUsersPhoneNumbers();
                        for (int i = 0; i<receivers.length; i++){
                            receiversString += receivers[i];
                            if (i<receivers.length-1) receiversString += "; ";
                        }
                        intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(receiversString));
                        startActivity(intent);
                        break;
                    case R.id.createContact:
                        new AlertDialog.Builder(UserListActivity.this)
                                .setIcon(R.drawable.create_contact_light)
                                .setTitle(" ")
                                .setItems(R.array.userAddContactOptions, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        User selectedUser = getSelectedUser();
                                        linkedContact = selectedUser;
                                        switch (i) {
                                            case 0: //Create new user
                                                Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                                                intent.putExtra(ContactsContract.Intents.Insert.NAME, selectedUser.getName());
                                                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, selectedUser.getEmail());
                                                startActivity(intent);
                                                break;
                                            case 1: //Add email to existing user
                                                intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                                                startActivityForResult(intent, INTENT_CODE_CONTACT_PICKER);
                                        }
                                    }
                                })
                                .create().show();
                        break;
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
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        Log.i("com.docdoku.android.plm", "onActivityResult called with request code " + reqCode + " and result code " + resCode);
        if (resCode == RESULT_OK) {
            switch (reqCode){
            case INTENT_CODE_CONTACT_PICKER:
                Uri result = data.getData();
                Log.i("com.docdoku.android.plm", "Contact Uri: " + result.toString());
                int id = Integer.parseInt(result.getLastPathSegment());
                ContentValues contentValues = new ContentValues();
                contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, id);
                contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                contentValues.put(ContactsContract.CommonDataKinds.Email.ADDRESS, linkedContact.getEmail());
                contentValues.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                searchForContactOnPhone(linkedContact);
                userArrayAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void setMenu(int menuId, ActionMode actionMode){
        MenuInflater inflater = actionMode.getMenuInflater();
        Menu menu = actionMode.getMenu();
        menu.clear();
        inflater.inflate(menuId, menu);
    }

    private int getNumSelectedUsers(){
        int numSelectedUsers = 0;
        SparseBooleanArray checked = userListView.getCheckedItemPositions();
        int size = checked.size();
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value){
                numSelectedUsers++;
            }
        }
        return numSelectedUsers;
    }

    private String[] getSelectedUsersEmail(){
        ArrayList<String> checkedEmails = new ArrayList<String>();
        SparseBooleanArray checked = userListView.getCheckedItemPositions();
        int size = checked.size();
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value){
                checkedEmails.add(((User) userArrayAdapter.getItem(checked.keyAt(i))).getEmail());
            }
        }
        String[] checkedEmailsArray = new String[checkedEmails.size()];
        checkedEmailsArray = checkedEmails.toArray(checkedEmailsArray);
        return checkedEmailsArray;
    }

    private String[] getSelectedUsersPhoneNumbers(){
        ArrayList<String> checkedEmails = new ArrayList<String>();
        SparseBooleanArray checked = userListView.getCheckedItemPositions();
        int size = checked.size();
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value){
                checkedEmails.add(((User) userArrayAdapter.getItem(checked.keyAt(i))).getPhoneNumber());
            }
        }
        String[] checkedEmailsArray = new String[checkedEmails.size()];
        checkedEmailsArray = checkedEmails.toArray(checkedEmailsArray);
        return checkedEmailsArray;
    }

    private boolean selectedUsersHavePhoneNumber(){
        SparseBooleanArray checked = userListView.getCheckedItemPositions();
        int size = checked.size();
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value){
                User user = (User) userArrayAdapter.getItem(checked.keyAt(i));
                if (!user.existsOnPhone()){
                    return false;
                }
            }
        }
        return true;
    }

    private User getSelectedUser(){
        SparseBooleanArray checked = userListView.getCheckedItemPositions();
        int size = checked.size();
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value){
                return (User) userArrayAdapter.getItem(checked.keyAt(i));
            }
        }
        Log.i("com.docdoku.android.plm", "Internal error: couldn't find a selected user");
        return null;
    }

    @Override
    public void onHttpGetResult(String result) {
        View loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        userArray = new ArrayList<User>();
        try {
            JSONArray usersJSON = new JSONArray(result);
            for (int i=0; i<usersJSON.length(); i++){
                JSONObject userJSON = usersJSON.getJSONObject(i);
                User user = new User(userJSON.getString("name"),userJSON.getString("email"), userJSON.getString("login"));
                userArray.add(user);
                searchForContactOnPhone(user);
            }
            userArrayAdapter = new UserArrayAdapter(userArray);
            userListView.setAdapter(userArrayAdapter);
        } catch (JSONException e) {
            Log.e("docdoku.DocDokuPLM", "Error handling json of workspace's users");
            e.printStackTrace();
        }
    }

    private void searchForContactOnPhone(User user){
        Cursor contacts = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.ADDRESS + "= ?", new String[]{user.getEmail()}, null);
        if (contacts.moveToNext()){
            user.setExistsOnPhone(true);
            String contactId = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Identity.CONTACT_ID));
            Cursor contactPhones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);
            String result = "Phone contact found with email address " + user.getEmail() +
                "\nId: " + contactId +
                "\nName: " + contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME));
            while (contactPhones.moveToNext()){
                String phoneNumber = contactPhones.getString(contactPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int phoneTypeCode = contactPhones.getInt(contactPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                String phoneLabel = contactPhones.getString(contactPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
                String phoneType = ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), phoneTypeCode, phoneLabel).toString();
                result += "\nPhone: " + phoneNumber + ", Type: " + phoneType;
                user.addPhoneNumber(phoneNumber, phoneType);
            }
            Log.i("com.docdoku.android.plm", result);
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
            if (user.existsOnPhone()){
                checkBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.user_highlighted, 0);
            }
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

    /**
     * SearchActionBarActivity methods
     */
    @Override
    protected int getSearchQueryHintId() {
        return R.string.userSearch;
    }

    @Override
    protected void executeSearch(String query) {
        if (query.length()>0){
            Log.i("com.docdoku.android.plm", "User seach query: " + query);
            ArrayList<User> searchResultUsers =  searchUsers(query);
            UserArrayAdapter searchResultAdapter = new UserArrayAdapter(searchResultUsers);
            userListView.setAdapter(searchResultAdapter);
        }else{
            userListView.setAdapter(userArrayAdapter);
        }
    }

    private ArrayList<User> searchUsers(String query){
        ArrayList<User> searchResult = new ArrayList<User>();
        Iterator<User> iterator = userArray.iterator();
        while (iterator.hasNext()){
            User user = iterator.next();
            if (user.getName().toLowerCase().contains(query.toLowerCase())){
                searchResult.add(user);
            }
        }
        return searchResult;
    }

    @Override
    protected int getActivityButtonId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

