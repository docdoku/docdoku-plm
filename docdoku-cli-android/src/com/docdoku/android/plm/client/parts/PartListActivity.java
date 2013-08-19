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

package com.docdoku.android.plm.client.parts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.docdoku.android.plm.client.NavigationHistory;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.SearchActionBarActivity;

import java.util.List;

/**
 * @author: martindevillers
 */
public abstract class PartListActivity extends SearchActionBarActivity {

    private static final String PREFERENCE_PART_HISTORY = "part history_light";

    protected NavigationHistory navigationHistory;
    protected List<Part> partsArray;
    protected PartAdapter partAdapter;
    protected ListView partListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        View loading = findViewById(R.id.loading);
        ((ViewGroup) loading.getParent()).removeView(loading);

        partListView = (ListView) findViewById(R.id.elementList);
        navigationHistory = new NavigationHistory(getSharedPreferences(getCurrentWorkspace() + PREFERENCE_PART_HISTORY, MODE_PRIVATE));
    }

    protected class PartAdapter extends BaseAdapter {

        private List<Part> parts;
        private final LayoutInflater inflater;

        public PartAdapter(List<Part> parts){
            this.parts = parts;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return parts.size();
        }

        @Override
        public Object getItem(int i) {
            return parts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View partRowView = inflater.inflate(R.layout.adapter_part, null);
            final Part part = parts.get(i);
            if (part != null){
                TextView reference = (TextView) partRowView.findViewById(R.id.number);
                reference.setText(part.getKey());
                TextView reservedBy = (TextView) partRowView.findViewById(R.id.checkOutUser);
                ImageView reservedPart = (ImageView) partRowView.findViewById(R.id.reservedPart);
                String reservedByName = part.getCheckOutUserName();
                if (reservedByName != null){
                    String reservedByLogin = part.getCheckOutUserLogin();
                    if (reservedByLogin.equals(getCurrentUserLogin())){
                        reservedPart.setImageResource(R.drawable.checked_out_current_user_light);
                    }
                    reservedBy.setText(reservedByName);
                }
                else{
                    reservedBy.setText("");
                    reservedPart.setImageResource(R.drawable.checked_in_light);
                }
                final View finalPartRowView = partRowView;
                partRowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finalPartRowView.setBackgroundResource(R.drawable.clickable_item_background);
                        navigationHistory.add(part.getKey());
                        Intent intent = new Intent(PartListActivity.this, PartActivity.class);
                        intent.putExtra(PartActivity.PART_EXTRA,part);
                        startActivity(intent);
                    }
                });
            } else{
                partRowView = new ProgressBar(PartListActivity.this);
            }

            return partRowView;
        }
    }
}
