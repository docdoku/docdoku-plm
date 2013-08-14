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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.docdoku.android.plm.client.documents.Document;
import com.docdoku.android.plm.client.documents.DocumentActivity;
import com.docdoku.android.plm.network.HttpGetDownloadFileTask;
import com.docdoku.android.plm.network.HttpGetTask;
import com.docdoku.android.plm.network.HttpPutTask;
import com.docdoku.android.plm.network.listeners.HttpGetDownloadFileListener;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.listeners.HttpPutListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author: martindevillers
 */
public abstract class ElementActivity extends SimpleActionBarActivity implements HttpPutListener, HttpGetDownloadFileListener {

    protected Element element;
    protected Button checkInOutButton;
    private boolean checkedIn;
    private ProgressDialog fileDownloadProgressDialog;

    /**
     * Methods to do checkins/checkouts
     *
     * setElementCheckedIn
     * checkOutElement
     * setElementCheckedOutByCurrentUser
     * checkInElement
     */
    protected void setElementCheckedIn(){
        checkedIn = true;
        checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_in_light, 0, 0);
        checkInOutButton.setText(R.string.checkOut);
        element.setCheckOutInformation(null, null, null);
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOutElement();
            }
        });
    }

    private void checkOutElement(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ElementActivity.this);
        builder.setTitle(R.string.checkOutConfirm);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new HttpPutTask(ElementActivity.this).execute(getUrlWorkspaceApi() + element.getUrlPath() + "/checkout/");
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    protected void setElementCheckedOutByCurrentUser(){
        checkedIn = false;
        checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_out_current_user_light, 0, 0);
        checkInOutButton.setText(R.string.checkin);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
        element.setCheckOutInformation(getCurrentUserLogin(), getCurrentUserLogin(), simpleDateFormat.format(c.getTime()));
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInElement();
            }
        });
    }

    private void checkInElement(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ElementActivity.this);
        builder.setTitle(R.string.checkInConfirm);
        builder.setMessage(R.string.iterationNotePrompt);
        final EditText iterationNoteField = new EditText(ElementActivity.this);
        builder.setView(iterationNoteField);
        builder.setPositiveButton(R.string.doCheckIn, new DialogInterface.OnClickListener() {
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
                                new HttpPutTask(ElementActivity.this).execute("api/workspaces/" + getCurrentWorkspace() + element.getUrlPath()+ "/checkin/");
                            } else{
                                ElementActivity.this.onHttpPutResult(false, "");
                            }
                        }
                    };
                    new HttpPutTask(httpPutListener).execute(getUrlWorkspaceApi() + element.getUrlPath() + "/iterations/" + element.getIterationNumber(), element.getLastIterationJSONWithUpdateNote(iterationNote).toString());
                }else {
                    Log.i("com.docdoku.android.plm", "No iteration note was entered for document checkin");
                    new HttpPutTask(ElementActivity.this).execute(getUrlWorkspaceApi() + element.getUrlPath() + "/checkin/");
                }
            }
        });
        builder.setNeutralButton(R.string.cancelCheckOut, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new HttpPutTask(ElementActivity.this).execute(getUrlWorkspaceApi() + element.getUrlPath() + "/undocheckout/");
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    /**
     * HttpPutListener method:
     * @Override onHttpPutResult
     */
    @Override
    public void onHttpPutResult(boolean result, String responseContent) {
        Log.i("com.docdoku.android.plm.client", "Result of checkin/checkout: " + result);
        if (result){
            if (checkedIn){
                setElementCheckedOutByCurrentUser();
                Toast.makeText(this, R.string.checkOutSuccessful, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject responseJSON = new JSONObject(responseContent);
                    element.updateFromJSON(responseJSON, getResources());
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            else{
                setElementCheckedIn();
                Toast.makeText(this, R.string.checkInSuccessful, Toast.LENGTH_SHORT).show();
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
     * HttpGetDownloadListener methods :
     * @Override onFileDownloadStart
     * @Override onFileDownloaded
     * @Override onFileDownloadStart
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
     * @return View
     * Create Views for the rows in each group. Methods:
     *
     * createNameValuePairRowView //Row presenting an attribute's name and value
     * createLinkedDocumentRowView //Row linking to another document
     * createUploadFileRowView //Row with buttons to upload files
     * createFileRowView //Row linking to a file download
     * createNoContentFoundRowView //Row indicating that no content is available for group
     */
    protected View createNameValuePairRowView(String name, String value){
        View rowView = getLayoutInflater().inflate(R.layout.adapter_name_value_pair, null);
        ((TextView) rowView.findViewById(R.id.fieldName)).setText(name);
        ((TextView) rowView.findViewById(R.id.fieldValue)).setText(value);
        return rowView;
    }

    protected View createLinkedDocumentRowView(final String linkedDocument){
        ViewGroup rowView = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_simple, null);
        TextView docView = (TextView) rowView.findViewById(R.id.docId);
        docView.setText(linkedDocument);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("com.docdoku.android.plm.client", "Following link to " + linkedDocument);
                HttpGetListener httpGetListener = new HttpGetListener() {
                    @Override
                    public void onHttpGetResult(String result) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            Document document1 = new Document(jsonObject.getString("id"));
                            document1.updateFromJSON(jsonObject, getResources());
                            Intent intent = new Intent(ElementActivity.this, DocumentActivity.class);
                            intent.putExtra(DocumentActivity.EXTRA_DOCUMENT, document1);
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
    }

    protected View createFileRowView(final String fileName, final String fileUrl){
        View rowView = getLayoutInflater().inflate(R.layout.adapter_dowloadable_file, null);
        TextView fileNameField = (TextView) rowView.findViewById(R.id.fileName);
        fileNameField.setText(fileName);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("com.docdoku.android.plm.client", "downloading file from path: " + fileUrl);
                new HttpGetDownloadFileTask(ElementActivity.this).execute("files/" + fileUrl, fileName);
            }
        });
        return rowView;
    }

    protected View createNoContentFoundRowView(int messageId){
        View rowView = getLayoutInflater().inflate(R.layout.adapter_message, null);
        TextView message = (TextView) rowView.findViewById(R.id.message);
        message.setText(messageId);
        return rowView;
    }
}
