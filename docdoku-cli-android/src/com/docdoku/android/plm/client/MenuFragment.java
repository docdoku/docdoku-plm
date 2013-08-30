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
 *
 * @author: Martin Devillers
 */
public class MenuFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.MenuFragment";

    protected boolean workspaceChanged = false;

    private View view;
    private RadioGroup workspaceRadioGroup;
    private TextView expandRadioButtons;

    private Session session;
    private String[] downloadedWorkspaces;
    private String currentWorkspace;

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

    public void addCurrentWorkspace(String workspace, RadioGroup radioGroup){
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

    public void addWorkspaces(String[] workspaces, RadioGroup radioGroup){
        int selectedButtonId = radioGroup.getCheckedRadioButtonId();
        if (selectedButtonId != -1){
            radioGroup.removeView(view.findViewById(selectedButtonId));
        }
        workspaceChanged = false;
        int numWorkspaces = workspaces.length;
        for (int i=0; i<numWorkspaces; i++){
            RadioButton radioButton;
            radioButton = new RadioButton(getActivity());
            radioButton.setText(workspaces[i]);
            radioButton.setTextColor(R.color.darkGrey);
            radioGroup.addView(radioButton);
            if (workspaces[i].equals(currentWorkspace)){
                radioGroup.check(radioButton.getId());
            }
        }
    }

    public void setCurrentActivity(int buttonId){
        View activityView = view.findViewById(buttonId);
        if (activityView != null){
            activityView.setSelected(true);
        }else{
            Log.i(LOG_TAG,"Current activity did not provide a correct button id. Id provided: " + buttonId);
        }
    }

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
}
