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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 *
 * @author: Martin Devillers
 */
public class MenuFragment extends Fragment {

    public static final String PREFERENCE_WORKSPACE = "workspace";
    private static String[] workspaces;
    private static String WORKSPACE;
    protected static boolean workspaceChanged = false;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_menu, container);
        workspaceChanged = false;
        RadioGroup workspace = (RadioGroup) view.findViewById(R.id.workspaceRadioGroup);
        if (workspaces != null){
            if (WORKSPACE == null){
                SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                WORKSPACE = preferences.getString(PREFERENCE_WORKSPACE,"");
                if (WORKSPACE.equals("")){
                    try {WORKSPACE = workspaces[0];} catch (ArrayIndexOutOfBoundsException e){Log.i("com.docdoku.android.plm.client","No Workspace downloaded");}
                }
                else{
                    Log.i("com.docdoku.android.plm.client", "Loading workspace from last session: " + WORKSPACE);
                }
            }
            addWorkspaces(workspaces, workspace);
        }
        else{
            Log.e("com.docdoku.android.plm.client","ERROR: No workspaces downloaded");
        }

        workspace.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton selectedWorkspace = (RadioButton) view.findViewById(radioGroup.getCheckedRadioButtonId());
                SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                WORKSPACE = selectedWorkspace.getText().toString();
                editor.putString(PREFERENCE_WORKSPACE, WORKSPACE);
                editor.commit();
                workspaceChanged = true;
            }
        });

        View documentSearch = view.findViewById(R.id.documentSearch);
        documentSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DocumentSearchActivity.class);
                startActivity(intent);
            }
        });
        TextView recentlyViewedDocuments = (TextView) view.findViewById(R.id.recentlyViewedDocuments);
        recentlyViewedDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DocumentHistoryListActivity.class);
                startActivity(intent);
            }
        });
        TextView allDocuments = (TextView) view.findViewById(R.id.allDocuments);
        allDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DocumentCompleteListActivity.class);
                startActivity(intent);
            }
        });
        TextView checkedOutDocuments = (TextView) view.findViewById(R.id.checkedOutDocuments);
        checkedOutDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DocumentCheckedOutListActivity.class);
                intent.putExtra(DocumentCheckedOutListActivity.LIST_MODE_EXTRA, DocumentCheckedOutListActivity.CHECKED_OUT_DOCUMENTS_LIST);
                startActivity(intent);
            }
        });

        TextView partSearch = (TextView) view.findViewById(R.id.partSearch);
        partSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PartSearchActivity.class);
                startActivity(intent);
            }
        });
        TextView recentlyViewedParts = (TextView) view.findViewById(R.id.recentlyViewedParts);
        recentlyViewedParts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PartHistoryListActivity.class);
                startActivity(intent);
            }
        });
        TextView allParts = (TextView) view.findViewById(R.id.allParts);
        allParts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PartCompleteListActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    public void addWorkspaces(String[] workspaces, RadioGroup radioGroup){
        int numWorkspaces = workspaces.length;
        for (int i=0; i<numWorkspaces; i++){
            RadioButton radioButton;
            radioButton = new RadioButton(getActivity());
            radioButton.setText(workspaces[i]);
            radioButton.setTextColor(R.color.darkGrey);
            radioGroup.addView(radioButton);
            if (workspaces[i].equals(WORKSPACE)){
                radioGroup.check(radioButton.getId());
            }
        }
    }

    public static void setWorkspaces(String[] setWorkspaces){
        workspaces = setWorkspaces;
    }

    public static String getCurrentWorkspace(){
        return WORKSPACE;
    }

    public void setCurrentActivity(int buttonId){
        View activityView = view.findViewById(buttonId);
        if (activityView != null){
            activityView.setSelected(true);
        }else{
            Log.i("com.docdoku.android.plm","Current activity did not provide a correct button id. Id provided: " + buttonId);
        }
    }
}
