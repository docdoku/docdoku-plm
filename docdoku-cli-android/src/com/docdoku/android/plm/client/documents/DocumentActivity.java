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

package com.docdoku.android.plm.client.documents;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;
import com.docdoku.android.plm.client.Element;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.SimpleActionBarActivity;
import com.docdoku.android.plm.network.*;
import com.docdoku.android.plm.network.listeners.HttpGetDownloadFileListener;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.listeners.HttpPostUploadFileListener;
import com.docdoku.android.plm.network.listeners.HttpPutListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author: Martin Devillers
 */
public class DocumentActivity extends SimpleActionBarActivity implements HttpPutListener, HttpGetDownloadFileListener, HttpPostUploadFileListener {

    public static final String EXTRA_DOCUMENT = "document";

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
        document = (Document) intent.getSerializableExtra(EXTRA_DOCUMENT);

        Log.i("com.docdoku.android.plm.client", "starting activity for document with id: " + document.getIdentification());

        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.list);
        expandableListView.setAdapter(new DetailsList());
        expandableListView.expandGroup(1);
    }

    private void setDocumentCheckedIn(){
        checkedIn = true;
        checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_in_light, 0, 0);
        checkInOutButton.setText(R.string.documentCheckOut);
        document.setCheckOutInformation(null, null, null);
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOutDocument();
            }
        });
    }

    private void checkOutDocument(){
        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentActivity.this);
        builder.setTitle(R.string.documentCheckOutConfirm);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new HttpPutTask(DocumentActivity.this).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getIdentification() + "/checkout/");
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    public void setDocumentCheckedOutByCurrentUser(){
        checkedIn = false;
        checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_out_current_user_light, 0, 0);
        checkInOutButton.setText(R.string.documentCheckIn);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
        document.setCheckOutInformation(getCurrentUserLogin(), getCurrentUserLogin(), simpleDateFormat.format(c.getTime()));
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInDocument();
            }
        });
    }

    private void checkInDocument(){
        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentActivity.this);
        builder.setTitle(R.string.documentCheckInConfirm);
        builder.setMessage(R.string.documentIterationNotePrompt);
        final EditText iterationNoteField = new EditText(DocumentActivity.this);
        builder.setView(iterationNoteField);
        builder.setPositiveButton(R.string.documentDoCheckIn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String iterationNote = iterationNoteField.getText().toString();
                if (iterationNote.length()>0){
                    Log.i("com.docdoku.android.plm", "Iteration note for document checkin: " + iterationNote);
                    HttpPutListener httpPutListener = new HttpPutListener() {
                        @Override
                        public void onHttpPutResult(boolean result, String responseContent) {
                            if (result){
                                Log.i("com.docdoku.android.plm", "Checking out document after successfully uploading iteration");
                                new HttpPutTask(DocumentActivity.this).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getIdentification() + "/checkin/");
                            } else{
                                DocumentActivity.this.onHttpPutResult(false, "");
                            }
                        }
                    };
                    new HttpPutTask(httpPutListener).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getIdentification() + "/iterations/" + document.getIterationNumber(), document.getLastIterationJSONWithUpdateNote(iterationNote).toString());
                }else {
                    Log.i("com.docdoku.android.plm", "No iteration note was entered for document checkin");
                    new HttpPutTask(DocumentActivity.this).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getIdentification() + "/checkin/");
                }
            }
        });
        builder.setNeutralButton(R.string.documentCancelCheckOut, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new HttpPutTask(DocumentActivity.this).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + document.getIdentification() + "/undocheckout/");
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    @Override
    public void onHttpPutResult(boolean result, String responseContent) {
        Log.i("com.docdoku.android.plm.client", "Result of checkin/checkout: " + result);
        if (result){
            if (checkedIn){
                setDocumentCheckedOutByCurrentUser();
                Toast.makeText(this, R.string.documentSuccessfullyCheckedOut, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject responseJSON = new JSONObject(responseContent);
                    document.updateFromJSON(responseJSON, getResources());
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            else{
                setDocumentCheckedIn();
                Toast.makeText(this, R.string.documentSuccessfullyCheckedIn, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.connectionError);
            builder.setPositiveButton(R.string.OK, null);
            builder.create().show();
        }
    }

    /**
     * HttpGetDownloadListener methods
     */
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

    /**
     * HttpPostUploadFileListener method
     */
    @Override
    public void onUploadResult(boolean result, final String fileName) {
        if (result){
            Toast.makeText(this, R.string.uploadSuccess, Toast.LENGTH_SHORT).show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Echec du chargement de l'image");
            builder.setNegativeButton("Annuler", null);
            builder.setPositiveButton("Réessayer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {String docReference = document.getIdentification();
                    String docId = docReference.substring(0, docReference.lastIndexOf("-"));
                    String docVersion = docReference.substring(docReference.lastIndexOf("-") + 1);
                    new HttpPostUploadFileTask(DocumentActivity.this).execute("files/" + getCurrentWorkspace() + "/documents/" + docId + "/" + docVersion + "/" + document.getIterationNumber() + "/" + fileName + ".png",pictureSavePath);
                }
            });
            builder.create().show();
        }
    }

    /**
     * Result of taking a picture or video
     */
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
            dialogBuilder.setPositiveButton(R.string.uploadImage, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String fileName = ((EditText) dialogView.findViewById(R.id.imageName)).getText().toString();
                    if (fileName.length() == 0) fileName = "mobileImage" + new SimpleDateFormat("HH-mm-ss_MM-dd-yyyy").format(new Date());;
                    String docReference = document.getIdentification();
                    String docId = docReference.substring(0, docReference.lastIndexOf("-"));
                    String docVersion = docReference.substring(docReference.lastIndexOf("-") + 1);
                    new HttpPostUploadFileTask(DocumentActivity.this).execute("files/" + getCurrentWorkspace() + "/documents/" + docId + "/" + docVersion + "/" + document.getIterationNumber() + "/" + fileName + ".png",pictureSavePath);
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

    @Override
    protected int getActivityButtonId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Adapter for the expandable list view
     * Group 0: Header with tile of document and important buttons
     * Group 1: General information about the document
     * Group 2: Linked files
     * Group 3: Linked documents
     * Group 4: Information about the last iteration
     * Group 5: Attributes
     */
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
                case 2:
                    if (getCurrentUserLogin().equals(document.getCheckOutUserLogin())){
                        return Math.max(2,document.getNumberOfFiles() + 1);
                    } else {
                        return Math.max(1, document.getNumberOfFiles());
                    }
                case 3: return Math.max(1, document.getNumberOfLinkedDocuments());
                case 4: return NUM_REVISION_FIELDS;
                case 5: return Math.max(1, document.getNumberOfAttributes());
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
                    return createHeaderView();
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
        public View getChildView(int i, final int i2, boolean b, View view, ViewGroup viewGroup) {
            View rowView = null;
            switch (i){
                case 1: //Document general information
                    String[] fieldNames = getResources().getStringArray(R.array.documentGeneralInformationFieldNames);
                    String[] fieldValues = document.getDocumentDetails();
                    rowView = createNameValuePairRowView(fieldNames[i2], fieldValues[i2]);
                    break;
                case 2: // Document attached files
                    if (getCurrentUserLogin().equals(document.getCheckOutUserLogin())){
                        if (i2 == 0){
                            rowView = createUploadFileRowView();
                        } else {
                            rowView = createFileRowView(i2 - 1);
                        }
                    } else {
                        rowView = createFileRowView(i2);
                    }
                    break;
                case 3: // Linked documents
                    rowView = createLinkedDocumentRowView(i2);
                    break;
                case 4: // Last document revision
                    fieldNames = getResources().getStringArray(R.array.documentIterationFieldNames);
                    fieldValues = document.getLastIteration();
                    rowView = createNameValuePairRowView(fieldNames[i2], fieldValues[i2]);
                    break;
                case 5: //Document attributes
                    try{
                        Element.Attribute attribute = document.getAttribute(i2);
                        rowView = createNameValuePairRowView(attribute.getName(), attribute.getValue());
                    }catch (ArrayIndexOutOfBoundsException e){
                        rowView = createNoContentFoundRowView(R.string.documentNoAttributes);
                    }catch (NullPointerException e){
                        rowView = createNoContentFoundRowView(R.string.documentNoAttributes);
                    }

                    break;
            }
            return rowView;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private View createNameValuePairRowView(String name, String value){
        View rowView = getLayoutInflater().inflate(R.layout.adapter_name_value_pair, null);
        ((TextView) rowView.findViewById(R.id.fieldName)).setText(name);
        ((TextView) rowView.findViewById(R.id.fieldValue)).setText(value);
        return rowView;
    }

    private View createLinkedDocumentRowView(int i2){
        try{
            ViewGroup rowView = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_simple, null);
            TextView docView = (TextView) rowView.findViewById(R.id.docId);
            final String linkedDocument = document.getLinkedDocument(i2);
            docView.setText(linkedDocument);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("com.docdoku.android.plm.client", "Following link from document " + document.getIdentification()
                            + "to document " + linkedDocument);
                    HttpGetListener httpGetListener = new HttpGetListener() {
                        @Override
                        public void onHttpGetResult(String result) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                Document document1 = new Document(jsonObject.getString("id"));
                                document1.updateFromJSON(jsonObject, getResources());
                                Intent intent = new Intent(DocumentActivity.this, DocumentActivity.class);
                                intent.putExtra(EXTRA_DOCUMENT, document1);
                                startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    };
                    new HttpGetTask(httpGetListener).execute(getUrlWorkspaceApi() + "/documents/" + linkedDocument);
                }
            });
            return rowView;
        }catch (ArrayIndexOutOfBoundsException e){
            return createNoContentFoundRowView(R.string.documentNoLinkedDocuments);
        }catch (NullPointerException e){
            return createNoContentFoundRowView(R.string.documentNoLinkedDocuments);
        }
    }

    private View createUploadFileRowView(){
        View rowView =  getLayoutInflater().inflate(R.layout.adapter_upload_file, null);
        ImageButton takePicture = (ImageButton) rowView.findViewById(R.id.takePicture);
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
        return rowView;
    }

    private View createFileRowView(final int position){
        try{
            View rowView = getLayoutInflater().inflate(R.layout.adapter_dowloadable_file, null);
            TextView fileNameField = (TextView) rowView.findViewById(R.id.fileName);
            final String fileName = document.getFileName(position);
            fileNameField.setText(document.getFileName(position));
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String fileUrl = document.getFile(position);
                    Log.i("com.docdoku.android.plm.client", "downloading file from path: " + fileUrl);
                    new HttpGetDownloadFileTask(DocumentActivity.this).execute("files/" + fileUrl, fileName);
                }
            });
            return rowView;
        }catch (ArrayIndexOutOfBoundsException e){
            return createNoContentFoundRowView(R.string.documentNoAttachedFiles);
        }catch (NullPointerException e){
            return  createNoContentFoundRowView(R.string.documentNoAttachedFiles);
        }
    }

    private View createNoContentFoundRowView(int messageId){
        View rowView = getLayoutInflater().inflate(R.layout.adapter_message, null);
        TextView message = (TextView) rowView.findViewById(R.id.message);
        message.setText(messageId);
        return rowView;
    }

    private View createHeaderView(){
        ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_header, null);
        TextView documentReference = (TextView) header.findViewById(R.id.documentIdentification);
        documentReference.setText(document.getIdentification());

        ToggleButton notifyIteration = (ToggleButton) header.findViewById(R.id.notifyIteration);
        setNotifyIterationNotification(notifyIteration);
        ToggleButton notifyStateChange = (ToggleButton) header.findViewById(R.id.notifyStateChange);
        setNotifyStateChangeNotification(notifyStateChange);

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
    }

    private void setNotifyStateChangeNotification(final CompoundButton notifyStateChange){
        notifyStateChange.setChecked(document.getStateChangeNotification());
        notifyStateChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean b = notifyStateChange.isChecked();
                HttpPutListener httpPutListener = new HttpPutListener() {
                    @Override
                    public void onHttpPutResult(boolean result, String responseContent) {
                        if (b){
                            Toast.makeText(DocumentActivity.this, R.string.documentStateChangeNotificationSuccessfullyActivated, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DocumentActivity.this, R.string.documentStateChangeNotificationSuccessfullyDeactivated, Toast.LENGTH_SHORT).show();
                        }
                        document.setStateChangeNotification(b);
                    }
                };
                if (b) {
                    subscriptionChangeRequested(R.string.confirmSubscribeToStateChangeNotification,
                            document,
                            "stateChange/subscribe",
                            notifyStateChange,
                            b,
                            httpPutListener);
                } else {
                    subscriptionChangeRequested(R.string.confirmUnsubscribeToStateChangeNotification,
                            document,
                            "stateChange/unsubscribe",
                            notifyStateChange,
                            b,
                            httpPutListener);

                }
            }
        });
    }

    private void setNotifyIterationNotification(final CompoundButton notifyIteration){
        notifyIteration.setChecked(document.getIterationNotification());
        notifyIteration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean b = notifyIteration.isChecked();
                HttpPutListener httpPutListener = new HttpPutListener() {
                    @Override
                    public void onHttpPutResult(boolean result, String responseContent) {
                        if (b){
                            Toast.makeText(DocumentActivity.this, R.string.documentIterationChangeNotificationSuccessfullyActivated, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DocumentActivity.this, R.string.documentIterationChangeNotificationSuccessfullyDeactivated, Toast.LENGTH_SHORT).show();
                        }
                        document.setIterationNotification(b);
                    }
                };
                if (b) {
                    subscriptionChangeRequested(R.string.confirmSubscribeToIterationChangeNotification,
                            document,
                            "iterationChange/subscribe",
                            notifyIteration,
                            b,
                            httpPutListener);
                } else {
                    subscriptionChangeRequested(R.string.confirmUnsubscribeToIterationChangeNotification,
                            document,
                            "iterationChange/unsubscribe",
                            notifyIteration,
                            b,
                            httpPutListener);

                }
            }
        });
    }

    private void subscriptionChangeRequested(int messageId, final Document doc, final String urlCommand, final CompoundButton compoundButton, final boolean compoundButtonState, final HttpPutListener httpPutListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentActivity.this);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("docDoku.DocDokuPLM", "Subscribing to iteration change notification for document with reference " + doc.getIdentification());
                new HttpPutTask(httpPutListener).execute("api/workspaces/" + getCurrentWorkspace() + "/documents/" + doc.getIdentification() + "/notification/" + urlCommand);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                compoundButton.setChecked(!compoundButtonState);
            }
        });
        builder.create().show();
    }
}

