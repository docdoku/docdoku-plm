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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.docdoku.android.plm.client.Element;
import com.docdoku.android.plm.client.ElementActivity;
import com.docdoku.android.plm.client.R;

/**
 *
 * @author: Martin Devillers
 */
public class PartActivity extends ElementActivity {

    public static final String PART_EXTRA = "part";

    private static final int NUM_PAGES = 6;
    private static final int NUM_GENERAL_INFORMATION_FIELDS = 10;
    private static final int NUM_REVISION_FIELDS = 4;

    private Part part;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element);

        Intent intent = getIntent();
        part = (Part) intent.getSerializableExtra(PART_EXTRA);
        element = part;

        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.list);
        expandableListView.addHeaderView(createHeaderView());
        expandableListView.setAdapter(new PartDetailsExpandableListAdapter());
        expandableListView.expandGroup(0);

        /*TextView partNumber = (TextView) findViewById(R.id.partKey);
        partNumber.setText(part.getKey());
        TextView partAuthor = (TextView) findViewById(R.id.partAuthor);
        partAuthor.setText(part.getAuthorName());

        ImageView checkOutLogo = (ImageView) findViewById(R.id.checkOutLogo);
        TextView checkOutUser = (TextView) findViewById(R.id.checkOutUser);
        checkOutUser.setText(part.getCheckOutUserName());
        Button checkInOutButton = (Button) findViewById(R.id.checkInOutButton);
        if (part.getCheckOutUserLogin() != null){
            if (getCurrentUserLogin().equals(part.getCheckOutUserLogin())){
                checkOutLogo.setImageResource(R.drawable.checked_out_current_user_light);
                checkInOutButton.setText(R.string.partCheckIn);
                checkInOutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
            else{
                checkOutLogo.setImageResource(R.drawable.checked_out_other_user_light);
                checkInOutButton.setVisibility(View.GONE);
            }
        }

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new PartPagerAdapter(getSupportFragmentManager()));
        */
    }

    private View createHeaderView(){
        ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_header, null);
        TextView documentReference = (TextView) header.findViewById(R.id.documentIdentification);
        documentReference.setText(part.getKey());

        ToggleButton notifyIteration = (ToggleButton) header.findViewById(R.id.notifyIteration);
        notifyIteration.setVisibility(View.INVISIBLE);
        ToggleButton notifyStateChange = (ToggleButton) header.findViewById(R.id.notifyStateChange);
        notifyStateChange.setVisibility(View.INVISIBLE);

        checkInOutButton = (Button) header.findViewById(R.id.checkInOutButton);
        if (part.getCheckOutUserLogin() != null){
            if (getCurrentUserLogin().equals(part.getCheckOutUserLogin())){
                setElementCheckedOutByCurrentUser();
            }
            else{
                checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_out_other_user_light, 0, 0);
                checkInOutButton.setClickable(false);
                checkInOutButton.setText(R.string.locked);
            }
        }
        else{
            setElementCheckedIn();
        }
        return header;
    }

    @Override
    protected int getActivityButtonId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private class PartDetailsExpandableListAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return NUM_PAGES;
        }

        @Override
        public int getChildrenCount(int i) {
            switch (i){
                case 0: return NUM_GENERAL_INFORMATION_FIELDS;
                case 1: return 1;
                case 2: return Math.max(part.getNumComponents(), 1);
                case 3: return Math.max(part.getNumberOfLinkedDocuments(), 1);
                case 4: return NUM_REVISION_FIELDS;
                case 5: return Math.max(part.getNumberOfAttributes(), 1);
            }
            return 0;
        }

        @Override
        public Object getGroup(int i) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getChild(int i, int i2) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i2) {
            return i2;
        }

        @Override
        public boolean hasStableIds() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            ViewGroup pageView;
            pageView = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_detail_header, null);
            if (b){
                ((ImageView) pageView.findViewById(R.id.collapse_expand_group)).setImageResource(R.drawable.group_collapse_light);
            }
            TextView title = (TextView) pageView.findViewById(R.id.page_title);
            switch (i){
                case 0:
                    title.setText(R.string.partGeneralInformation);
                    break;
                case 1:
                    title.setText(R.string.partCADFile);
                    break;
                case 2:
                    title.setText(R.string.partAssembly);
                    break;
                case 3:
                    title.setText(R.string.partLinks);
                    break;
                case 4:
                    title.setText(R.string.partIteration);
                    break;
                case 5:
                    title.setText(R.string.partAttributes);
                    break;
            }
            return pageView;
        }

        @Override
        public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
            View rowView = null;
            switch (i){
                case 0://Part general information
                    String[] fieldNames = getResources().getStringArray(R.array.partGeneralInformationFieldNames);
                    String[] fieldValues = part.getGeneralInformationValues(PartActivity.this);
                    rowView = createNameValuePairRowView(fieldNames[i2], fieldValues[i2]);
                    break;
                case 1://CAD file
                    try{
                        rowView = createFileRowView(part.getCADFileName(), part.getCADFileUrl());
                    }catch (NullPointerException e){
                        return createNoContentFoundRowView(R.string.partNoCADFile);
                    }
                    break;
                case 2: //Components
                    try{
                        Part.Component component = part.getComponent(i2);
                        rowView = createComponentRowView(component.getAmount(), component.getNumber());
                    }catch (NullPointerException e){
                        return createNoContentFoundRowView(R.string.partNoComponents);
                    }catch (ArrayIndexOutOfBoundsException e){
                        return createNoContentFoundRowView(R.string.partNoComponents);
                    }
                    break;
                case 3: //Linked documents
                    try{
                        String linkedDocument = part.getLinkedDocument(i2);
                        rowView = createLinkedDocumentRowView(linkedDocument);
                    }catch (NullPointerException e){
                        return createNoContentFoundRowView(R.string.partNoLinkedDocuments);
                    }catch (ArrayIndexOutOfBoundsException e){
                        return createNoContentFoundRowView(R.string.partNoLinkedDocuments);
                    }
                    break;
                case 4: //Last iteration
                    fieldNames = getResources().getStringArray(R.array.iterationFieldNames);
                    fieldValues = part.getLastIteration();
                    rowView = createNameValuePairRowView(fieldNames[i2], fieldValues[i2]);
                    break;
                case 5: //Attributes
                    try{
                        Element.Attribute attribute = part.getAttribute(i2);
                        rowView = createNameValuePairRowView(attribute.getName(), attribute.getValue());
                    }catch (ArrayIndexOutOfBoundsException e){
                        rowView = createNoContentFoundRowView(R.string.partNoAttributes);
                    }catch (NullPointerException e){
                        rowView = createNoContentFoundRowView(R.string.partNoAttributes);
                    }
                    break;
            }
            return rowView;
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            switch (i){
                case 1: //CAD file
                    if (part.getCADFileUrl() != null && part.getCADFileUrl().length()>0){
                        Log.i("com.docdoku.android.plm", "CAD url: " + part.getCADFileUrl());
                        return true;
                    }
                    break;
                case 3: //Linked documents
                    if (part.getNumberOfLinkedDocuments()>0){
                        return true;
                    }
                    break;
            }
            return false;
        }
    }

    private View createComponentRowView(int quantity, String name){
        View rowView = getLayoutInflater().inflate(R.layout.adapter_component, null);
        ((TextView) rowView.findViewById(R.id.componentQuantity)).setText("x" + quantity);
        ((TextView) rowView.findViewById(R.id.componentName)).setText(name);
        return rowView;
    }

    /*
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
            pageView = (ViewGroup) inflater.inflate(R.layout.adapter_document_detail_header, null);
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
            View row = inflater.inflate(R.layout.adapter_name_value_pair, null);
            TextView fieldName = (TextView) row.findViewById(R.id.fieldName);
            fieldName.setText(name);
            TextView fieldValue = (TextView) row.findViewById(R.id.fieldValue);
            fieldValue.setText(value);
            content.addView(row);
        }
    }
    */
}