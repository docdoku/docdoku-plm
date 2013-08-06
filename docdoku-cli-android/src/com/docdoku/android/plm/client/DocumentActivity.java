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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author: Martin Devillers
 */
public class DocumentActivity extends SimpleActionBarActivity implements HttpPutListener, HttpGetDownloadFileListener {

    public static final String DOCUMENT_EXTRA = "document";

    private final int NUM_PAGES = 6;
    private final int NUM_GENERAL_INFORMATION_FIELDS = 10;
    private final int NUM_REVISION_FIELDS = 4;

    private Document document;

    private Button checkInOutButton;
    private boolean checkedIn;
    private ProgressDialog fileDownloadProgressDialog;
    private String pictureSavePath;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        Intent intent = getIntent();
        document = (Document) intent.getSerializableExtra(DOCUMENT_EXTRA);

        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.list);
        expandableListView.setAdapter(new DetailsList());
        expandableListView.expandGroup(1);

        /*
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment[] pages = new Fragment[5];
        pages[0] = new DocumentPageFragment(getResources().getStringArray(R.array.documentGeneralInformationFieldNames), document.getDocumentDetails(), R.string.documentGeneralInformation);
        String[] files = document.getFiles();
        if (files != null){
            pages[1] =  new DocumentFilePageFragment(files, this, R.string.documentFiles);
        }
        else{
            pages[1] = new DocumentPageFragment(new String[0], new String[0], R.string.documentFiles);
        }
        pages[2] = new DocumentPageFragment(new String[0], document.getLinkedDocuments(), R.string.documentLinks);
        pages[3] = new DocumentPageFragment(getResources().getStringArray(R.array.documentIterationFieldNames), document.getLastRevision(), R.string.documentIteration);
        pages[4] = new DocumentPageFragment(document.getAttributeNames(), document.getAttributeValues(), R.string.documentAttributes);
        for (int i = 0; i<pages.length; i++){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.list, pages[i]);
            fragmentTransaction.commit();
        }
        */
    }

    private void setDocumentCheckedIn(){
        checkedIn = true;
        checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_in_light, 0, 0);
        checkInOutButton.setText(R.string.documentCheckOut);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
        document.setCheckOutInformation(null, null, null);
        final Activity activity = this;
        final HttpPutListener httpPutListener = this;
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.documentCheckOutConfirm);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new HttpPutTask(httpPutListener).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getIdentification() + "/checkout/");
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.create().show();
            }
        });
    }

    public void setDocumentCheckedOutByCurrentUser(){
        checkedIn = false;
        checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_out_current_user_light, 0, 0);
        checkInOutButton.setText(R.string.documentCheckIn);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
        document.setCheckOutInformation(getCurrentUserLogin(), getCurrentUserLogin(), simpleDateFormat.format(c.getTime()));
        final Activity activity = this;
        final HttpPutListener httpPutListener = this;
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.documentCheckInConfirm);
                builder.setPositiveButton(R.string.documentDoCheckIn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new HttpPutTask(httpPutListener).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getIdentification() + "/checkin/");
                    }
                });
                builder.setNeutralButton(R.string.documentCancelCheckOut, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new HttpPutTask(httpPutListener).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getIdentification() + "/undocheckout/");
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.create().show();
            }
        });
    }

    @Override
    public void onHttpPutResult(boolean result) {
        Log.i("docDoku.DocDokuPLM", "Result of checkin/checkout: " + result);
        if (result){
            if (checkedIn){
                setDocumentCheckedOutByCurrentUser();
            }
            else{
                setDocumentCheckedIn();
            }
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.connectionError);
            builder.setPositiveButton(R.string.OK, null);
            builder.create().show();
        }
    }

    private class DetailsList extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return NUM_PAGES;
        }

        @Override
        public int getChildrenCount(int i) {
            switch (i){
                case 0: return 0;
                case 1: return NUM_GENERAL_INFORMATION_FIELDS;
                case 2: return document.getNumberOfFiles();
                case 3: return 0;
                case 4: return NUM_REVISION_FIELDS;
                case 5: return 0;
            }
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
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
            return i;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getChildId(int i, int i2) {
            return i2;  //To change body of implemented methods use File | Settings | File Templates.
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
                ((ImageView) pageView.findViewById(R.id.collapse_expand_group)).setImageResource(R.drawable.group_collapse);
            }
            TextView title = (TextView) pageView.findViewById(R.id.page_title);
            switch (i){
                case 0:
                    ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_header, null);
                    TextView documentReference = (TextView) header.findViewById(R.id.documentIdentification);
                    documentReference.setText(document.getIdentification());

                    ToggleButton notifyIteration = (ToggleButton) header.findViewById(R.id.notifyIteration);
                    notifyIteration.setChecked(document.getIterationNotification());
                    ToggleButton notifyStateChange = (ToggleButton) header.findViewById(R.id.notifyStateChange);
                    notifyStateChange.setChecked(document.getStateChangeNotification());

                    checkInOutButton = (Button) header.findViewById(R.id.checkInOutButton);
                    if (document.getCheckOutUserLogin() != null){
                        if (getCurrentUserLogin().equals(document.getCheckOutUserLogin())){
                            setDocumentCheckedOutByCurrentUser();
                        }
                        else{
                            checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_out_other_user_light, 0, 0);
                            checkInOutButton.setClickable(false);
                            checkInOutButton.setText(R.string.documentLocked);
                        }
                    }
                    else{
                        setDocumentCheckedIn();
                    }
                    return header;
                case 1:
                    title.setText(R.string.documentGeneralInformation);
                    break;
                case 2:
                    title.setText(R.string.documentFiles);
                    break;
                case 3:
                    title.setText(R.string.documentLinks);
                    break;
                case 4:
                    title.setText(R.string.documentIteration);
                    break;
                case 5:
                    title.setText(R.string.documentAttributes);
                    break;
            }
            return pageView;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
            View rowView = null;
            switch (i){
                case 1: //Document general information
                    rowView = getLayoutInflater().inflate(R.layout.row_name_value_pair, null);
                    String[] fieldNames = getResources().getStringArray(R.array.documentGeneralInformationFieldNames);
                    String[] fieldValues = document.getDocumentDetails();
                    ((TextView) rowView.findViewById(R.id.fieldName)).setText(fieldNames[i2]);
                    ((TextView) rowView.findViewById(R.id.fieldValue)).setText(fieldValues[i2]);
                    break;
                case 2: // Document attached files
                    rowView = getLayoutInflater().inflate(R.layout.row_dowloadable_element, null);
                    TextView fileNameField = (TextView) rowView.findViewById(R.id.fileName);
                    fileNameField.setText(document.getFile(i2));
                    break;
                case 3: // Linked documents
                    break;
                case 4: // Last document revision
                    rowView = getLayoutInflater().inflate(R.layout.row_name_value_pair, null);
                    fieldNames = getResources().getStringArray(R.array.documentIterationFieldNames);
                    fieldValues = document.getLastRevision();
                    ((TextView) rowView.findViewById(R.id.fieldName)).setText(fieldNames[i2]);
                    ((TextView) rowView.findViewById(R.id.fieldValue)).setText(fieldValues[i2]);
                    break;
                case 5: //Document attributes
                    break;
            }
            return rowView;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private class DocumentPageFragment extends Fragment{

        private String[] names;
        private String[] values;
        private int title;

        public DocumentPageFragment(String[] names, String[] values, int title){
            this.names = names;
            this.values = values;
            this.title = title;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            ViewGroup pageView;
            pageView = (ViewGroup) inflater.inflate(R.layout.adapter_document_detail_header, null);
            TextView titleField = (TextView) pageView.findViewById(R.id.page_title);
            titleField.setText(title);
            ViewGroup content = (ViewGroup) pageView.findViewById(R.id.content);

            if (names.length != values.length){
                names = new String[values.length];
            }
            for (int i = 0; i< names.length; i++){
                addNameValueRow(inflater,content, names[i],values[i]);
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

    private class DocumentFilePageFragment extends Fragment{

        private String[] files;
        private HttpGetDownloadFileListener httpGetDownloadFileListener;
        private int title;

        public DocumentFilePageFragment(String[] files, HttpGetDownloadFileListener httpGetDownloadFileListener, int title){
            this.files = files;
            this.httpGetDownloadFileListener = httpGetDownloadFileListener;
            this.title = title;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            ViewGroup pageView;
            pageView = (ViewGroup) inflater.inflate(R.layout.adapter_document_detail_header, null);
            TextView titleField = (TextView) pageView.findViewById(R.id.page_title);
            titleField.setText(title);
            ViewGroup content = (ViewGroup) pageView.findViewById(R.id.content);

            if (getCurrentUserLogin().equals(document.getCheckOutUserLogin())){
                View uploadRow = inflater.inflate(R.layout.row_upload_element, null);
                pageView.addView(uploadRow);
                ImageButton takePicture = (ImageButton) uploadRow.findViewById(R.id.takePicture);
                takePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String timeStamp = new SimpleDateFormat("HH-mm-ss_MM-dd-yyyy").format(new Date());
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DocDokuPLM" + timeStamp +".jpg");
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        pictureSavePath = file.getAbsolutePath();
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                        startActivityForResult(intent, 0);
                    }
                });
            }

            for (int i = 0; i<files.length; i++){
                View row = inflater.inflate(R.layout.row_dowloadable_element, null);
                TextView fileNameField = (TextView) row.findViewById(R.id.fileName);
                final String fileUrl = files[i];
                final String fileName = fileUrl.substring(fileUrl.lastIndexOf('/')+1, fileUrl.length());
                fileNameField.setText(fileName);
                content.addView(row);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("com.docdoku.android.plm.client", "downloading file from path: " + fileUrl);
                        new HttpGetDownloadFileTask(httpGetDownloadFileListener).execute("files/" + fileUrl, fileName);
                    }
                });
            }

            return pageView;
        }
    }

    @Override
    public void onFileDownloadStart() {
        fileDownloadProgressDialog = new ProgressDialog(this);
        fileDownloadProgressDialog.setTitle(R.string.downloadingFile);
        fileDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        fileDownloadProgressDialog.show();
    }

    @Override
    public void onFileDownloaded(boolean result, String path) {
        fileDownloadProgressDialog.dismiss();
        if (result){
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File(path);

            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext=file.getName().substring(file.getName().indexOf(".")+1);
            String type = mime.getMimeTypeFromExtension(ext);

            intent.setDataAndType(Uri.fromFile(file),type);

            startActivity(Intent.createChooser(intent, getResources().getString(R.string.chooseHowToOpenFile)));
        }
        else{
            Toast.makeText(this, R.string.fileDownloadFail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProgressUpdate(int progress) {
        fileDownloadProgressDialog.setProgress(progress);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){
            Toast.makeText(this, "Image saved to " + pictureSavePath, Toast.LENGTH_LONG).show();
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_picture, null);
            Bitmap picture = BitmapFactory.decodeFile(pictureSavePath);
            ((ImageView) dialogView.findViewById(R.id.image)).setImageBitmap(picture);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(false);
            final Context context = this;
            dialogBuilder.setPositiveButton(R.string.uploadImage, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String fileName = ((EditText) dialogView.findViewById(R.id.imageName)).getText().toString();
                    if (fileName.length() == 0) fileName = "mobileImage" + new SimpleDateFormat("HH-mm-ss_MM-dd-yyyy").format(new Date());;
                    String docReference = document.getIdentification();
                    String docId = docReference.substring(0, docReference.lastIndexOf("-"));
                    String docVersion = docReference.substring(docReference.lastIndexOf("-") + 1);
                    new HttpPostUploadFileTask(context).execute("files/" + getCurrentWorkspace() + "/documents/" + docId + "/" + docVersion + "/" + document.getRevisionNumber() + "/" + fileName + ".png",pictureSavePath);
                }
            });
            dialogBuilder.setNegativeButton(R.string.cancel, null);
            dialogBuilder.create().show();
        } else if (resultCode == RESULT_CANCELED) {
            // User cancelled the image capture
        } else {
            // Image capture failed, advise user
        }
    }

}

