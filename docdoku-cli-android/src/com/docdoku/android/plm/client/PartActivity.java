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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *
 * @author: Martin Devillers
 */
public class PartActivity extends ActionBarActivity {

    public static final String PART_EXTRA = "part";

    private Part part;

    private ViewPager pager;
    private static final int NUM_PAGES = 6;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part);

        Intent intent = getIntent();
        part = (Part) intent.getSerializableExtra(PART_EXTRA);

        TextView partNumber = (TextView) findViewById(R.id.partKey);
        partNumber.setText(part.getKey());
        TextView partAuthor = (TextView) findViewById(R.id.partAuthor);
        partAuthor.setText(part.getAuthorName());

        ImageView checkOutLogo = (ImageView) findViewById(R.id.checkOutLogo);
        TextView checkOutUser = (TextView) findViewById(R.id.checkOutUser);
        checkOutUser.setText(part.getCheckOutUserName());
        Button checkInOutButton = (Button) findViewById(R.id.checkInOutButton);
        if (part.getCheckOutUserLogin() != null){
            if (getCurrentUserLogin().equals(part.getCheckOutUserLogin())){
                checkOutLogo.setImageResource(R.drawable.checked_out_current_user);
                checkInOutButton.setText(R.string.partCheckIn);
                checkInOutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
            else{
                checkOutLogo.setImageResource(R.drawable.checked_out_other_user);
                checkInOutButton.setVisibility(View.GONE);
            }
        }

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new PartPagerAdapter(getSupportFragmentManager()));
    }

    private class PartPagerAdapter extends FragmentPagerAdapter{

        public PartPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new PartPageFragment(getResources().getStringArray(R.array.partGeneralInformationFieldNames), part.getGeneralInformationValues());
                case 1:
                    return new PartPageFragment(new String[0], new String[0]);
                case 2:
                    return new PartPageFragment(part.getAttributeNames(), part.getAttributeValues());
                case 3:
                    return new PartPageFragment(new String[0], new String[0]);
                case 4:
                    return new PartPageFragment(new String[0], new String[0]);
                case 5:
                    return new PartPageFragment(new String[0], new String[0]);
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position){
            switch (position){
                case 0:
                    return "Général";
                case 1:
                    return "Itération";
                case 2:
                    return "Attributs";
                case 3:
                    return "Fichier CAD";
                case 4:
                    return "Assemblage";
                case 5:
                    return "Liens";
            }
            return "";
        }
    }

    private class PartPageFragment extends Fragment{

        private String[] names;
        private String[] values;

        public PartPageFragment(String[] names, String[] values){
            this.names = names;
            this.values = values;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            ViewGroup pageView;
            pageView = (ViewGroup) inflater.inflate(R.layout.fragment_document_page, null);
            ViewGroup content = (ViewGroup) pageView.findViewById(R.id.content);

            if (names.length != values.length){
                Log.e("docdoku.DocDokuPLM","ERROR: Not the same number of names as values");
            }
            else{
                for (int i = 0; i< names.length; i++){
                    addNameValueRow(inflater,content, names[i],values[i]);
                }
            }

            return pageView;
        }

        private void addNameValueRow(LayoutInflater inflater, ViewGroup content, String name, String value){
            View row = inflater.inflate(R.layout.row_name_value_pair, null);
            TextView fieldName = (TextView) row.findViewById(R.id.fieldName);
            fieldName.setText(name);
            TextView fieldValue = (TextView) row.findViewById(R.id.fieldValue);
            fieldValue.setText(value);
            content.addView(row);
        }
    }
}