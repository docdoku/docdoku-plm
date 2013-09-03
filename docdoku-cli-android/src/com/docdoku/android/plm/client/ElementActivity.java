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
import android.os.Bundle;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Abstract class for <code>Activity</code> representing an <code>Element</code>'s data.
 * <p>Contains the methods used for operation that <code>Document</code>s and <code>Part</code>s have in common.
 *
 * @author: martindevillers
 */
public abstract class ElementActivity extends SimpleActionBarActivity implements HttpPutTask.HttpPutListener, HttpGetDownloadFileTask.HttpGetDownloadFileListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.ElementActivity";

    protected Element element;
    protected Button checkInOutButton;
    private boolean checkedIn;
    private ProgressDialog fileDownloadProgressDialog;
    private String iterationNote;

    /**
     * Obtains the instance of the <code>Element</code> that this <code>Activity</code> is presenting to the user
     *
     * @see android.app.Activity
     */
    @Override
    public void onResume(){
        super.onResume();
        element = getElement();
    }

    protected abstract Element getElement();

    /**
     * Sets the <code>Element</code> checked in by the current user.
     * <p>Set the <code>checkInOutButton OnClickListener</code>'s <code>onClick()</code> method to start the
     * {@link #checkOutElement()} method.
     */
    protected void setElementCheckedIn(){
        checkedIn = true;
        checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_in_light, 0, 0);
        checkInOutButton.setText(R.string.checkOut);
        getElement().setCheckOutInformation(null, null, null);
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOutElement();
            }
        });
    }

    /**
     * Attempts to check out the <code>Element</code> by the current user.
     * <p>Shows an <code>AlertDialog</code> to obtain confirmation that this is what the user wants to do.
     * If he confirms it, a new {@link HttpPutTask} is started.
     */
    private void checkOutElement(){
        new AlertDialog.Builder(ElementActivity.this)
            .setIcon(R.drawable.checked_in_light)
            .setTitle(" ")
            .setMessage(R.string.checkOutConfirm)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new HttpPutTask(ElementActivity.this).execute(getUrlWorkspaceApi() + element.getUrlPath() + "/checkout/");
                }
            })
            .setNegativeButton(R.string.no, null)
            .create().show();
    }

    /**
     * Sets the <code>Element</code> checked out by the current user at the current time by calling the
     * {@link #setElementCheckedOutByCurrentUser(String) setElementCheckedOutByCurrentUser(String date)} method
     * with date set to current date.
     */
    protected void setElementCheckedOutByCurrentUser(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
        setElementCheckedOutByCurrentUser(simpleDateFormat.format(c.getTime()));
    }

    /**
     * Sets the <code>Element</code> checked out by the current user at the specified time.
     * <p>Set the <code>checkInOutButton OnClickListener</code>'s <code>onClick()</code> method to start the
     * {@link #checkInElement()} method.
     *
     * @param date the checkout date
     */
    protected void setElementCheckedOutByCurrentUser(String date){
        checkedIn = false;
        checkInOutButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_out_current_user_light, 0, 0);
        checkInOutButton.setText(R.string.checkin);
        getElement().setCheckOutInformation(getCurrentUserName(), getCurrentUserLogin(), date);
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInElement();
            }
        });
    }

    /**
     * Attempts to check in the <code>Element</code> by the current user.
     * <p>Shows an <code>AlertDialog</code> to obtain confirmation that this is what the user wants to do and to allow
     * the user to add a revision note in an <code>EditText</code>.
     * If he confirm without a revision  it, a new {@link HttpPutTask} is started to check in the <code>Element</code>.
     * If he confirms with a revision, a new {@link HttpPutTask} is started to send the revision note to the server, and
     * if that task returns a positive result, then another task is started to do the checkin.
     */
    private void checkInElement(){
        final EditText iterationNoteField = new EditText(ElementActivity.this);
        new AlertDialog.Builder(ElementActivity.this)
            .setIcon(R.drawable.checked_out_current_user_light)
            .setTitle(" ")
            .setMessage(R.string.checkInConfirm)
            .setMessage(R.string.iterationNotePrompt)
            .setView(iterationNoteField)
            .setPositiveButton(R.string.doCheckIn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    iterationNote = iterationNoteField.getText().toString();
                    if (iterationNote.length()>0){
                        Log.i(LOG_TAG, "Iteration note for document checkin: " + iterationNote);
                        HttpPutTask.HttpPutListener httpPutListener = new HttpPutTask.HttpPutListener() {
                            @Override
                            public void onHttpPutResult(boolean result, String responseContent) {
                                if (result){
                                    Log.i(LOG_TAG, "Checking out document after successfully uploading iteration");
                                    new HttpPutTask(ElementActivity.this).execute("api/workspaces/" + getCurrentWorkspace() + element.getUrlPath()+ "/checkin/");
                                } else{
                                    ElementActivity.this.onHttpPutResult(false, "");
                                }
                            }
                        };
                        new HttpPutTask(httpPutListener).execute(getUrlWorkspaceApi() + element.getUrlPath() + "/iterations/" + element.getIterationNumber(), element.getLastIterationJSONWithUpdateNote(iterationNote).toString());
                    }else {
                        Log.i(LOG_TAG, "No iteration note was entered for document checkin");
                        new HttpPutTask(ElementActivity.this).execute(getUrlWorkspaceApi() + element.getUrlPath() + "/checkin/");
                    }
                }
            })
            .setNeutralButton(R.string.cancelCheckOut, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new HttpPutTask(ElementActivity.this).execute(getUrlWorkspaceApi() + element.getUrlPath() + "/undocheckout/");
                }
            })
            .setNegativeButton(R.string.no, null)
            .create().show();
    }

    /**
     * Handles the result of an checkin/checkout task.
     * <p>If the result is <code>false</code>, shows a <code>AlertDialog</code> to indicate a connection error.
     * <p>If the result is <code>true</code>, checks the chek in/out status of the <code>Element</code>, and shows
     * a <code>Toast</code> indicating that the operation was successful. The document is also updated with this new
     * check in/out operation.
     *
     * @param result If the Http request return a code 200, indicating that the task was successful
     * @param responseContent The String of the updated <code>JSONObject</code>
     * @see com.docdoku.android.plm.network.HttpPutTask.HttpPutListener
     */
    @Override
    public void onHttpPutResult(boolean result, String responseContent) {
        Log.i(LOG_TAG, "Result of checkin/checkout: " + result);
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
                SimpleDateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.simpleDateFormat));
                element.setLastIteration(element.iterationNumber, iterationNote, getCurrentUserName(), dateFormat.format(Calendar.getInstance().getTime()));
            }
        }
        else{
            new AlertDialog.Builder(this)
                .setIcon(R.drawable.error_light)
                .setTitle(" ")
                .setMessage(R.string.connectionError)
                .setPositiveButton(R.string.OK, null)
                .create().show();
        }
    }

    /**
     * When a file download begins, opens a <code>ProgressDialog</code>.
     *
     * @see com.docdoku.android.plm.network.HttpGetDownloadFileTask.HttpGetDownloadFileListener
     */
    @Override
    public void onFileDownloadStart() {
        fileDownloadProgressDialog = new ProgressDialog(this);
        fileDownloadProgressDialog.setTitle(R.string.downloadingFile);
        fileDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        fileDownloadProgressDialog.show();
    }

    /**
     * Closes the <code>ProgressDialog</code> when the file download task is finished
     * <p>If the download was successful, shows a <code>Toast</code> indicating where the file was saved, and creates a <code>chooser</code>
     * for the user to open the file.
     * <p>If the download failed, show a <code>Toast</code> indicating a download error to the user.
     *
     * @param result whether the download was successful
     * @param path the path on the device to the downloaded file
     */
    @Override
    public void onFileDownloaded(boolean result, String path) {
        fileDownloadProgressDialog.dismiss();
        if (result){
            Toast.makeText(this, getResources().getString(R.string.downloadSuccessToPath, path), Toast.LENGTH_SHORT).show();
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

    /**
     * Updates the <code>ProgressDialog</code> during the file download.
     *
     * @param progress the percentage of file download completed
     * @see com.docdoku.android.plm.network.HttpGetDownloadFileTask.HttpGetDownloadFileListener
     */
    @Override
    public void onProgressUpdate(int progress) {
        fileDownloadProgressDialog.setProgress(progress);
    }

    /**
     * Inflates a layout for an attribute having a name and a value.
     * <p>Layout file: {@link /res/layout/adapter_name_value_pair.xml adapter_name_value_pair}
     *
     * @param name The attribute's name
     * @param value The attribute's value
     * @return The <code>View</code>, which is a row presenting the name and value.
     */
    protected View createNameValuePairRowView(String name, String value){
        View rowView = getLayoutInflater().inflate(R.layout.adapter_name_value_pair, null);
        ((TextView) rowView.findViewById(R.id.fieldName)).setText(name);
        ((TextView) rowView.findViewById(R.id.fieldValue)).setText(value);
        return rowView;
    }

    /**
     * Inflates a layout for an linked document, showing its id.
     * Sets the <code>OnClickListener</code> that starts the download of the document's information with an {@link HttpGetTask}, then, on result,
     * start the {@link DocumentActivity} for it.
     * <p>Layout file: {@link /res/layout/adapter_document_simple.xml adapter_document_simple}
     *
     * @param linkedDocument the id of the linked document
     * @return The <code>View</code>, which is a row with the document's id
     */
    protected View createLinkedDocumentRowView(final String linkedDocument){
        ViewGroup rowView = (ViewGroup) getLayoutInflater().inflate(R.layout.adapter_document_simple, null);
        TextView docView = (TextView) rowView.findViewById(R.id.docId);
        docView.setText(linkedDocument);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("com.docdoku.android.plm.client", "Following link to " + linkedDocument);
                HttpGetTask.HttpGetListener httpGetListener = new HttpGetTask.HttpGetListener() {
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

    /**
     * Inflates a layout for an linked file, showing its name.
     * Sets the <code>OnClickListener</code> that starts the download of the file with an {@link HttpGetDownloadFileTask}.
     * <p>Layout file: {@link /res/layout/adapter_dowloadable_file.xml adapter_downloadable_file}
     *
     * @param fileName the name of the downloadable file
     * @param fileUrl the end of the url used to download the file
     * @return The <code>View</code>, which is a row with the file's name
     */
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

    /**
     * Inflates a layout for a row indicating a simple message.
     * <p>Layout file: {@link /res/layout/adapter_message.xml adapter_message}
     *
     * @param messageId the id of the <code>String</code> resource containing the message
     * @return The <code>View</code>, which is a row with the message
     */
    protected View createNoContentFoundRowView(int messageId){
        View rowView = getLayoutInflater().inflate(R.layout.adapter_message, null);
        TextView message = (TextView) rowView.findViewById(R.id.message);
        message.setText(messageId);
        return rowView;
    }
}
