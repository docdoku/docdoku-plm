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
 * <code>Actity</code> that displays the list of the current workspace's <code>User</code>s and allows the current user to:
 * <p> - Send an email
 * <p> - Add to contacts or search in contacts
 * <p> - Call by phone (if available in contacts)
 * <p> - Send SMS (if available in contacts)
 * <p> </p>
 * <p>Layout file: {@link /res/layout/activity_element_list.xml activity_element_list}
 *
 * @author: Martin Devillers
 */
public class UserListActivity extends SearchActionBarActivity implements HttpGetTask.HttpGetListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.UserListActivity";

    private static final int INTENT_CODE_CONTACT_PICKER = 100;

    private ArrayList<User> userArray;
    private UserArrayAdapter userArrayAdapter;
    private ListView userListView;
    private User linkedContact;
    private View headerView;

    /**
     * Checks if this <code>Activity</code> was resumed after having added a <code>User's</code> email to one of the phone's
     * contacts or after having create a new contact. In that case, this <code>User</code> is marked as existing on phone.
     * @see android.app.Activity
     */
    @Override
    public void onResume(){
        super.onResume();
        if (linkedContact != null){
            searchForContactOnPhone(linkedContact);
            userArrayAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Called on the <code>Activity</code>'s creation.
     * <p> - Adds the header showing the current user's name and the <code>Button</code> to (un)select all users, and sets the
     * <code>OnClickListener</code> for that <code>Button</code>
     * <p> - Sets the <code>MultiChoiceModeListener</code> on the <code>User</code> <code>ListView</code> to enable the current
     * user to select them, prompting a contextual <code>ActionBar</code>. Handles the clicks on <code>MenuItem</code>s.
     * <p> - Starts an {@link HttpGetTask} to query the server for the list of the current workspace's <code>User</code>s
     *
     * @param savedInstanceState
     * @see android.app.Activity
     */
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
        Log.i(LOG_TAG, result);*/

        userListView = (ListView) findViewById(R.id.elementList);
        headerView = getLayoutInflater().inflate(R.layout.header_users, null);
        headerView.findViewById(R.id.selectAllUsers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int numUsers = userArrayAdapter.getCount();
                Log.i(LOG_TAG, "Current number of users in list: " + numUsers);
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
                Log.i(LOG_TAG, numSelectedUsers + " users now selected");
                if (numSelectedUsers>1){
                    if (selectedUsersHavePhoneNumber()){
                        Log.i(LOG_TAG, "Removing call option");
                        setMenu(R.menu.action_bar_users_selected, actionMode);
                    }else{
                        Log.i(LOG_TAG, "Removing phone related options");
                        setMenu(R.menu.action_bar_users_selected_nonexistent, actionMode);
                    }
                }else {
                    User selectedUser = getSelectedUser();
                    if (selectedUser != null){
                        if (selectedUser.existsOnPhone()){
                            Log.i(LOG_TAG, "Adding call option");
                            setMenu(R.menu.action_bar_user_selected, actionMode);
                        }else{
                            Log.i(LOG_TAG, "Adding create user option");
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
                switch (menuItem.getItemId()){
                    case R.id.sendEmails:
                        sendEmailToSelectedUsers();
                        return true;
                    case R.id.call:
                        callSelectedUser();
                        break;
                    case R.id.sendSMS:
                        sendSMSToSelectedUsers();
                        break;
                    case R.id.createContact:
                        createContactForSelectedUser();
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

    /**
     * Send an email to all selected {@code User}s in the {@code ListView}
     * <p>Gets all the selected {@code User}'s email addresses, then creates a chooser {@code Dialog} to allow the user
     * to choose which email service to use. The email's subject is set to the current workspace's name followed by "//"
     * by default.
     */
    private void sendEmailToSelectedUsers(){
        Intent intent;
        intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
        String[] checkedEmailsArray = getSelectedUsersEmail();
        intent.putExtra(Intent.EXTRA_EMAIL, checkedEmailsArray);
        intent.putExtra(Intent.EXTRA_SUBJECT, getCurrentWorkspace() + "//");
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.userSendEmail)));
    }

    /**
     * Call the selected {@code User} in the {@code ListView}
     * <p>If several users are selected, the first one in the list is chosen.
     * <p>Creates an {@code AlertDialog} to allow the user to choose between the phone numbers available for the selected
     * {@code User}.
     */
    private void callSelectedUser(){
        User selectedUser = getSelectedUser();
        final String[] phoneNumbers = selectedUser.getPhoneNumbers();
        new AlertDialog.Builder(UserListActivity.this)
                .setTitle(R.string.userChooseNumber)
                .setIcon(R.drawable.call_light)
                .setNegativeButton(R.string.userCancelCall, null)
                .setItems(phoneNumbers, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(LOG_TAG, "Calling phone number: " + phoneNumbers[i]);
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumbers[i]));
                        startActivity(intent);
                    }
                })
                .create().show();
    }

    /**
     * Sends an SMS to all the selected {@code User}s in the {@code ListView}
     * <p>Attempts to find a mobile number for all the users. For users for which none is available, their number will be
     * set to an empty {@code String}, so they will not be in the SMS receivers.
     */
    private void sendSMSToSelectedUsers(){
        Intent intent;
        String receiversString = "smsto:";
        String[] receivers = getSelectedUsersPhoneNumbers();
        for (int i = 0; i<receivers.length; i++){
            receiversString += receivers[i];
            if (i<receivers.length-1) receiversString += "; ";
        }
        intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(receiversString));
        startActivity(intent);
    }

    /**
     * Set the selected {@code User}'s email address to a contact.
     * <p>An {@code AlertDialog} offers the user the possibility of adding the selected {@code User}'s email addres to
     * an existing contact on phone, in which case an {@code Intent} start an {@code Activity} to pick a contact
     * among those existing on the phone.
     * <br>The user can also create a new contact with that email address, in which case an {@code Intent} starts the
     * {@code Activity} to create a new contact.
     */
    private void createContactForSelectedUser(){
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
    }

    /**
     * Handles the result of an <code>Activity</code> that was started to choose a contact to which to add an email
     * @param reqCode
     * @param resCode
     * @param data
     */
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        Log.i(LOG_TAG, "onActivityResult called with request code " + reqCode + " and result code " + resCode);
        if (resCode == RESULT_OK) {
            switch (reqCode){
                case INTENT_CODE_CONTACT_PICKER:
                    Uri result = data.getData();
                    Log.i(LOG_TAG, "Contact Uri: " + result.toString());
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

    /**
     * Sets the {@code ActionBar} with the menu inflated from the resources.
     *
     * @param menuId the resource Id for the menu file to inflate
     * @param actionMode
     */
    private void setMenu(int menuId, ActionMode actionMode){
        MenuInflater inflater = actionMode.getMenuInflater();
        Menu menu = actionMode.getMenu();
        menu.clear();
        inflater.inflate(menuId, menu);
    }

    /**
     * Returns the number of selected {@code User}s in the {@code ListView}
     * @return
     */
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

    /**
     * Returns a {@code String[]} of the selected {@code User}'s email addresses.
     * <p>The email addresses are those that the user put on the <b>PLM</b> website for his account.
     *
     * @return
     */
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

    /**
     * Returns a {@code String[]} of the selected {@code User}'s preferred phone numbers.
     * <p>If the contact of the phone has a mobile phone number available, the {@code User}'s {@link com.docdoku.android.plm.client.User#getPhoneNumber()}
     * method will return it. Otherwise, it will return the first number it can find.
     * <br>If there are no phone numbers on the phone for the {@code User}, then it will return an empty {@code String}.
     * @return
     */
    private String[] getSelectedUsersPhoneNumbers(){
        ArrayList<String> checkedPhoneNumbers = new ArrayList<String>();
        SparseBooleanArray checked = userListView.getCheckedItemPositions();
        int size = checked.size();
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value){
                checkedPhoneNumbers.add(((User) userArrayAdapter.getItem(checked.keyAt(i))).getPhoneNumber());
            }
        }
        String[] checkedPhoneNumbersArray = new String[checkedPhoneNumbers.size()];
        checkedPhoneNumbersArray = checkedPhoneNumbers.toArray(checkedPhoneNumbersArray);
        return checkedPhoneNumbersArray;
    }

    /**
     * Checks if all selected users exist in the phone's contacts.
     * <p>It does not actually check if the contacts have a phone number available for them.
     *
     * @return
     */
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

    /**
     * Returns the selected {@link User}.
     * <p>If several users are selected, returns the first selected one in the list.
     * <p>If no users are selected, returns {@code null} and prints out an error message in the {@code Log}.
     *
     * @return the selected {@code User}
     */
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
        Log.i(LOG_TAG, "Internal error: couldn't find a selected user");
        return null;
    }

    /**
     * Handles the result of the {@link HttpGetTask}. If the query was successful, the result contains a <code>JSONArray</code>
     * of the <code>User</code>s of the workspace.
     *
     * @param result the result of the query
     * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
     */
    @Override
    public void onHttpGetResult(String result) {
        View loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        userArray = new ArrayList<User>();
        try {
            JSONArray usersJSON = new JSONArray(result);
            for (int i=0; i<usersJSON.length(); i++){
                JSONObject userJSON = usersJSON.getJSONObject(i);
                User user = new User(userJSON.getString(User.JSON_KEY_USER_NAME),
                        userJSON.getString(User.JSON_KEY_USER_EMAIL),
                        userJSON.getString(User.JSON_KEY_USER_LOGIN));
                if (user.getLogin().equals(getCurrentUserLogin())){
                    ((TextView) headerView.findViewById(R.id.currentUser)).setText(user.getName());
                }else{
                    userArray.add(user);
                    searchForContactOnPhone(user);
                }
            }
            userArrayAdapter = new UserArrayAdapter(userArray);
            userListView.setAdapter(userArrayAdapter);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error handling json of workspace's users");
            e.printStackTrace();
        }
    }

    /**
     * Searches on the phone contacts for one that has an email address matching the {@link User}'s.
     * <p>If one is found, then the {@code User} is notified through {@link User#setExistsOnPhone(boolean) setExistsOnPhone()}
     * that it exists in the phone's contacts. All the phone numbers available for this contact are added to the
     * {@code User}'s {@code ArrayList} of phone numbers.
     *
     * @param user the user to search for on phone and that may be updated
     */
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
            Log.i(LOG_TAG, result);
        }
    }

    /**
     * {@code BaseAdapter} implementation for the presentation of rows for each {@link User}
     */
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

        /**
         * Inflates a row {@code View} to present a {@link User}.
         * <p>The {@code TextView}'s text is set to the {@code User}'s name.
         * <br>An {@code OnClickListener} is set on this {@code TextView} so that when the user clicks on it, it height
         * increases to be able to show the full name of the {@code User}, if that was not possible on a single line.
         * <p>If a contact matching the {@code User} was found on the phone, the icon next to the {@code CheckBox} is
         * highlighted in light blue.
         * <br>The {@code CheckBox} is set in the correct state and an {@code OnCheckedChangeListener} is set on it, to
         * notify the {@code ListView} that the item has been checked.
         *
         * @param i
         * @param view
         * @param viewGroup
         * @return
         * @see BaseAdapter
         */
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View userRowView = inflater.inflate(R.layout.adapter_user, null);
            User user = users.get(i);
            final TextView username = (TextView) userRowView.findViewById(R.id.username);
            username.setText(user.getName());
            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    username.setSingleLine(false);
                }
            });
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
     *
     *
     * @return
     * @see SearchActionBarActivity#getSearchQueryHintId()
     */
    @Override
    protected int getSearchQueryHintId() {
        return R.string.userSearch;
    }

    /**
     * Filters the list of {@code User}s by name with the query entered in the {@code SearchActionBar}. The search result
     * is used to create a new {@link UserArrayAdapter} that is set for the {@code ListView}.
     * <br>If the query was empty, the default {@link #userArrayAdapter} is set as the {@code Adapter} for the {@code ListView}.
     *
     * @param query the text entered in the <code>SearchActionBar</code>
     * @see SearchActionBarActivity#executeSearch(String)
     */
    @Override
    protected void executeSearch(String query) {
        if (query.length()>0){
            Log.i(LOG_TAG, "User seach query: " + query);
            ArrayList<User> searchResultUsers =  searchUsers(query);
            UserArrayAdapter searchResultAdapter = new UserArrayAdapter(searchResultUsers);
            userListView.setAdapter(searchResultAdapter);
        }else{
            userListView.setAdapter(userArrayAdapter);
        }
    }

    /**
     * Executes the filtering of the {@link User} {@code ArrayList} to return only those whose name contains the content of
     * the {@code query}. Not case-sensitive.
     *
     * @param query
     * @return an {@code ArrayList<User>} with only the users matching the search criteria
     */
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

    /**
     * This {@code Activity}'s {@code Button} is in the {@code ActionBar}, not in the {@link MenuFragment}, so this method does
     * not provide a {@code Button} id to be highlighted.
     *
     * @return
     * @see SimpleActionBarActivity
     */
    @Override
    protected int getActivityButtonId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

