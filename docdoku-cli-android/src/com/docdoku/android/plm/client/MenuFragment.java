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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.docdoku.android.plm.client.documents.*;
import com.docdoku.android.plm.client.parts.PartCompleteListActivity;
import com.docdoku.android.plm.client.parts.PartHistoryListActivity;
import com.docdoku.android.plm.client.parts.PartSearchActivity;

/**
 * The <code>DrawerLayout</code> component that slides from the left side of the screen to allow users to access the menu.
 *
 * @author: Martin Devillers
 */
public class MenuFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.MenuFragment";

    private boolean workspaceChanged = false;

    private View view;
    private RadioGroup workspaceRadioGroup;
    private TextView expandRadioButtons;

    private Session session;
    private String[] downloadedWorkspaces;
    private String currentWorkspace;

    /**
     * Creates the <code>View</code> for the sliding menu.
     * <p>Attempts to find the downloaded workspaces  and the current workspace for the <code>RadioGroup</code> in the
     * current <code>Session</code>, and if that fails attempts to load them from the <code>SharedPreferences</code>.
     * Sets the <code>OnClickListener</code> that expands the list of workspaces, and the one that detects when the current
     * workspace has been changed.
     * <p>Sets this <code>MenuFragment</code> as the <code>OnClickListener</code> for the menu items.
     * <p>Layout file: {@link /res/layout/fragment_menu.xml fragment_menu}
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     * @see Fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_menu, container);
        workspaceChanged = false;
        workspaceRadioGroup = (RadioGroup) view.findViewById(R.id.workspaceRadioGroup);
        try {
            session = Session.getSession();
            currentWorkspace = session.getCurrentWorkspace(getActivity());
            addCurrentWorkspace(currentWorkspace, workspaceRadioGroup);
            downloadedWorkspaces = session.getDownloadedWorkspaces(getActivity());
            if (downloadedWorkspaces.length == 1){
                ((ViewGroup) expandRadioButtons.getParent()).removeView(expandRadioButtons);
            }else{
                expandRadioButtons.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ViewGroup) expandRadioButtons.getParent()).removeView(expandRadioButtons);
                        addWorkspaces(downloadedWorkspaces, workspaceRadioGroup);
                    }
                });
            }

            workspaceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    RadioButton selectedWorkspace = (RadioButton) view.findViewById(radioGroup.getCheckedRadioButtonId());
                    session.setCurrentWorkspace(getActivity(), selectedWorkspace.getText().toString());
                    workspaceChanged = true;
                }
            });
        } catch (Session.SessionLoadException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        view.findViewById(R.id.documentSearch).setOnClickListener(this);
        view.findViewById(R.id.recentlyViewedDocuments).setOnClickListener(this);
        view.findViewById(R.id.allDocuments).setOnClickListener(this);
        view.findViewById(R.id.checkedOutDocuments).setOnClickListener(this);
        view.findViewById(R.id.documentFolders).setOnClickListener(this);

        view.findViewById(R.id.partSearch).setOnClickListener(this);
        view.findViewById(R.id.recentlyViewedParts).setOnClickListener(this);
        view.findViewById(R.id.allParts).setOnClickListener(this);

        return view;
    }

    /**
     * Sets the current workspace in the workspaces <code>RadioGroup</code>
     * <p>Adds a single <code>RadioButton</code>, which is selected, for the current workspace. If more than one workspace
     * have been downloaded, than add a <code>Button</code> to show the other workspaces.
     *
     * @param workspace the selected workspace
     * @param radioGroup the empty <code>RadioGroup</code> to be populated
     */
    void addCurrentWorkspace(String workspace, RadioGroup radioGroup){
        RadioButton radioButton;
        radioButton = new RadioButton(getActivity());
        radioButton.setText(workspace);
        radioButton.setTextColor(R.color.darkGrey);
        radioGroup.addView(radioButton);
        radioGroup.check(radioButton.getId());
        expandRadioButtons = new TextView(getActivity());
        expandRadioButtons.setText("...");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        expandRadioButtons.setLayoutParams(params);
        expandRadioButtons.setGravity(Gravity.CENTER_HORIZONTAL);
        expandRadioButtons.setBackgroundResource(R.drawable.selector_background);
        radioGroup.addView(expandRadioButtons);
    }

    /**
     * Expands the list of workspaces in the <code>RadioGroup</code> to show them all.
     * <p>Removes the selected workspace, then adds the full list of workspaces. Goes through the list until the position
     * of the current workspace is found and sets its <code>RadioButton</code> to checked.
     *
     * @param workspaces the list of downloaded workspaces
     * @param radioGroup the <code>RadioGroup</code>, containing only one child <code>View</code>, which is the selected workspace
     */
    void addWorkspaces(String[] workspaces, RadioGroup radioGroup){
        int selectedButtonId = radioGroup.getCheckedRadioButtonId();
        if (selectedButtonId != -1){
            radioGroup.removeView(view.findViewById(selectedButtonId));
        }
        workspaceChanged = false;
        for (String workspace : workspaces) {
            RadioButton radioButton;
            radioButton = new RadioButton(getActivity());
            radioButton.setText(workspace);
            radioButton.setTextColor(R.color.darkGrey);
            radioGroup.addView(radioButton);
            if (workspace.equals(currentWorkspace)) {
                radioGroup.check(radioButton.getId());
            }
        }
    }

    /**
     * Highlights the menu item that represents the current <code>Activity</code>, if such an item exists.
     *
     * @param buttonId The id of the item linking to the current <code>Activity</code>, if such an item exists.
     */
    public void setCurrentActivity(int buttonId){
        View activityView = view.findViewById(buttonId);
        if (activityView != null){
            activityView.setSelected(true);
        }else{
            Log.i(LOG_TAG,"Current activity did not provide a correct button id. Id provided: " + buttonId);
        }
    }

    /**
     * Handles a click on a menu item by starting an <code>Intent</code> to the corresponding activity.
     *
     * @param view
     * @see android.view.View.OnClickListener
     */
    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        Intent intent = null;
        switch (viewId){
            case R.id.documentSearch:
                intent = new Intent(getActivity(), DocumentSearchActivity.class);
                break;
            case R.id.recentlyViewedDocuments:
                intent = new Intent(getActivity(), DocumentHistoryListActivity.class);
                break;
            case R.id.allDocuments:
                intent = new Intent(getActivity(), DocumentCompleteListActivity.class);
                break;
            case R.id.documentFolders:
                intent = new Intent(getActivity(), DocumentFoldersActivity.class);
                break;
            case R.id.checkedOutDocuments:
                intent = new Intent(getActivity(), DocumentSimpleListActivity.class);
                intent.putExtra(DocumentSimpleListActivity.LIST_MODE_EXTRA, DocumentSimpleListActivity.CHECKED_OUT_DOCUMENTS_LIST);
                break;
            case R.id.partSearch:
                intent = new Intent(getActivity(), PartSearchActivity.class);
                break;
            case R.id.recentlyViewedParts:
                intent = new Intent(getActivity(), PartHistoryListActivity.class);
                break;
            case R.id.allParts:
                intent = new Intent(getActivity(), PartCompleteListActivity.class);
                break;
        }
        if (intent !=null){
            startActivity(intent);
        }
    }

    /**
     * Indicates whether the user has changed workspace while this drawer menu was open.
     *
     * @return if the selected workspace has changed
     */
    public boolean isWorkspaceChanged() {
        return workspaceChanged;
    }
}
