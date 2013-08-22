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
import android.view.*;

/**
 *
 * @author: Martin Devillers
 */
public abstract class SimpleActionBarActivity extends FragmentActivity {

    private static final String URL_API = "api/workspaces/";

    protected static String currentUserLogin;
    private ActionBarDrawerToggle drawerToggle;

    protected String getCurrentWorkspace(){
        return MenuFragment.getCurrentWorkspace();
    }

    protected String getUrlWorkspaceApi(){
        return URL_API + getCurrentWorkspace();
    }

    protected String getCurrentUserLogin(){
        return currentUserLogin;
    }

    @Override
    public void onResume(){
        super.onResume();
        final MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager().findFragmentById(R.id.menu);
        menuFragment.setCurrentActivity(getActivityButtonId());
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.navigation_drawer, 0, 0){
            @Override
            public void onDrawerClosed(View view) {
                if (menuFragment.workspaceChanged){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_simple, menu);
        return true;
    }

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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.confirmDisconnect));
                builder.setNegativeButton(getResources().getString(R.string.no), null);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(), ConnectionActivity.class);
                        intent.putExtra(ConnectionActivity.INTENT_KEY_ERASE_ID, true);
                        startActivity(intent);
                    }
                });
                builder.create().show();
                return true;
            default:
                Log.i("com.docdoku.android.plm.client", "Could not identify title bar button click");
                return super.onOptionsItemSelected(item);
        }

    }

    public void restartActivity(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    protected abstract int getActivityButtonId();
}
