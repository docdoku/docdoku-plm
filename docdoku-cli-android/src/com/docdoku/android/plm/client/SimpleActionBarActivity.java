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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.docdoku.android.plm.client.GCM.GCMRegisterService;
import com.docdoku.android.plm.client.connection.ConnectionActivity;
import com.docdoku.android.plm.client.users.UserListActivity;

/**
 * This class contains the methods that are used by almost all of this application's <code>Activities</code>.
 * <p>The roles of this class are:
 * <br> - Implement the methods that handle the <code>ActionBar</code>
 * ({@link #onCreateOptionsMenu(android.view.Menu) onCreateOptionsMenu()},
 * {@link #onOptionsItemSelected(android.view.MenuItem) onOptionsItemSelected()},
 * {@link #restartActivity()})
 * <br> - Implement the methods that handle the drawer menu
 * ({@link #onResume()})
 * <br> - Provide methods to easily access the <code>Session</code> data
 * ({@link #getCurrentWorkspace()},
 * {@link #getCurrentUserLogin()},
 * {@link #getUrlWorkspaceApi()},
 * {@link #getCurrentUserName()})
 *
 * @author: Martin Devillers
 */
public abstract class SimpleActionBarActivity extends FragmentActivity {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.SimpleActionBarActivity";

    private static final String URL_API = "api/workspaces/";

    private ActionBarDrawerToggle drawerToggle;

    /**
     * Returns the current workspace being used by the user, loading it from the <code>SharedPreferences</code> if it
     * can't be found in memory.
     * @return the current workspace.
     */
    protected String getCurrentWorkspace(){
        try {
            return Session.getSession(this).getCurrentWorkspace(this);
        } catch (Session.SessionLoadException e) {
            Log.e(LOG_TAG, "Unable to get current workspace because no session was found");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    /**
     * Returns the url used to access the current workspace in Http requests
     * @return the url path to the current workspace
     */
    protected String getUrlWorkspaceApi(){
        return URL_API + getCurrentWorkspace();
    }

    /**
     * Returns the login of the current user, loading it from the <code>SharedPreferences</code> if it can't be found in memory.
     * @return the current users's login
     */
    protected String getCurrentUserLogin(){
        try{
            return Session.getSession(this).getUserLogin();
        } catch (Session.SessionLoadException e) {
            Log.e(LOG_TAG, "Unable to get current user login because no session was found");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    /**
     * Returns the name of the current user, loading it from the <code>SharedPreferences</code> if it can't be found in memory.
     * @return the current users's name
     */
    String getCurrentUserName(){
        try{
            return Session.getSession(this).getUserName();
        } catch (Session.SessionLoadException e) {
            Log.e(LOG_TAG, "Unable to get current user login because no session was found");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    /**
     * Called when the GUI is visible to the user.
     * <p> - Sets the <code>ActionBar</code> "Home" button to open the navigation drawer.
     * <br> Sets a <code>Listener</code> that calls {@link #restartActivity()} if the selected workspace has changed while the
     * drawer menu was visible to the user.
     * @see android.app.Activity
     */
    @Override
    public void onResume(){
        super.onResume();
        final MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager().findFragmentById(R.id.menu);
        menuFragment.setCurrentActivity(getActivityButtonId());
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.navigation_drawer, 0, 0){
            @Override
            public void onDrawerClosed(View view) {
                if (menuFragment.isWorkspaceChanged()){
                    restartActivity();
                }
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        drawerToggle.syncState();
    }

    /**
     * Inflates the menu to create the <code>ActionBar</code> buttons
     * <p>menu file: {@link /res/menu/action_bar_simple.xml action_bar_simple}
     *
     * @param menu the menu to inflate
     * @return true
     * @see android.app.Activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_simple, menu);
        return true;
    }

    /**
     * Handles <code>ActionBar</code> button clicks, identifying them by the <code>MenuItem</code> id.
     * <p> - Users button: starts the {@link com.docdoku.android.plm.client.users.UserListActivity}
     * <br> - Logout button: show an <code>AlertDialog</code> asking the user for confirmation.
     *
     * @param item the <code>ActionBar</code> item that was clicked
     * @return true
     * @see android.app.Activity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_users:
                Intent intent = new Intent(this, UserListActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_logout:
                new AlertDialog.Builder(this)
                    .setIcon(R.drawable.logout_light)
                    .setTitle(" ")
                    .setMessage(getResources().getString(R.string.confirmDisconnect))
                    .setNegativeButton(getResources().getString(R.string.no), null)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                if (Session.getSession(SimpleActionBarActivity.this).isPermanentSession()) {
                                    Intent GCMLogoutIntent = new Intent(SimpleActionBarActivity.this, GCMRegisterService.class);
                                    GCMLogoutIntent.putExtra(GCMRegisterService.INTENT_KEY_ACTION, GCMRegisterService.ACTION_ERASE_ID);
                                    startService(GCMLogoutIntent);
                                }
                            } catch (Session.SessionLoadException e) {
                                Log.w(LOG_TAG, "Could not remove gcm id from server because session information was unavailable");
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            Intent intent = new Intent(SimpleActionBarActivity.this, ConnectionActivity.class);
                            intent.putExtra(ConnectionActivity.INTENT_KEY_ERASE_ID, true);
                            startActivity(intent);
                        }
                    })
                    .create().show();
                return true;
            default:
                Log.i(LOG_TAG, "Could not identify title bar button click");
                return super.onOptionsItemSelected(item);
        }

    }

    void restartActivity(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /**
     * Returns the Id of the <code>Button</code> leading to the current {@code Activity}. This {@code Button} is highlighted
     * in the side menu to show that this is the {@code Activity} that the user is currently viewing.
     * @return the id of the {@code Button} linking to the current {@code Activity}
     */
    protected abstract int getActivityButtonId();
}
