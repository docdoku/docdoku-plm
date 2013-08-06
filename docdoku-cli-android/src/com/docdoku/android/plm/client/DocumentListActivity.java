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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;

/**
 * @author: martindevillers
 */
public abstract class DocumentListActivity extends SearchActionBarActivity {

    private static final String PREFERENCE_DOCUMENT_HISTORY = "document history";

    protected NavigationHistory navigationHistory;
    protected List<Document> documentArray;
    protected DocumentAdapter documentAdapter;
    protected ListView documentListView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        View loading = findViewById(R.id.loading);
        ((ViewGroup) loading.getParent()).removeView(loading);

        documentListView = (ListView) findViewById(R.id.elementList);
        Log.i("com.docdoku.android.plm.client", "Loading navigation history from preference path: " + getCurrentWorkspace() + PREFERENCE_DOCUMENT_HISTORY);
        navigationHistory = new NavigationHistory(getSharedPreferences(getCurrentWorkspace() + PREFERENCE_DOCUMENT_HISTORY, MODE_PRIVATE));
    }

    protected class DocumentAdapter extends BaseAdapter {

        private List<Document> documents;
        private LayoutInflater inflater;

        public DocumentAdapter(List<Document> documents){
            this.documents = documents;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return documents.size();
        }

        @Override
        public Object getItem(int i) {
            return documents.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View documentRowView;
            final Document doc = documents.get(i);
            if (doc != null){
                documentRowView = inflater.inflate(R.layout.adapter_document, null);
                TextView identification = (TextView) documentRowView.findViewById(R.id.identification);
                identification.setText(doc.getIdentification());
                TextView checkOutUser = (TextView) documentRowView.findViewById(R.id.checkOutUser);
                ImageView checkedInOutImage = (ImageView) documentRowView.findViewById(R.id.checkedInOutImage);
                String checkOutUserName = doc.getCheckOutUserName();
                if (checkOutUserName != null){
                    String checkOutUserLogin = doc.getCheckOutUserLogin();
                    if (checkOutUserLogin.equals(getCurrentUserLogin())){
                        checkedInOutImage.setImageResource(R.drawable.checked_out_current_user_light);
                    }
                    checkOutUser.setText(checkOutUserName);
                }
                else{
                    checkOutUser.setText("");
                    checkedInOutImage.setImageResource(R.drawable.checked_in_light);
                }
                View contentLink = documentRowView.findViewById(R.id.contentLink);
                contentLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigationHistory.add(doc.getIdentification());
                        Intent intent = new Intent(getBaseContext(), DocumentActivity.class);
                        intent.putExtra(DocumentActivity.DOCUMENT_EXTRA, doc);
                        startActivity(intent);
                    }
                });
                final CompoundButton notifyStateChange = (CompoundButton) documentRowView.findViewById(R.id.notifyStateChange);
                notifyStateChange.setChecked(doc.getStateChangeNotification());
                notifyStateChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final boolean b = notifyStateChange.isChecked();
                        HttpPutListener httpPutListener = new HttpPutListener() {
                            @Override
                            public void onHttpPutResult(boolean result) {
                                doc.setStateChangeNotification(b);
                            }
                        };
                        if (b) {
                            subscriptionChangeRequested(R.string.confirmSubscribeToStateChangeNotification,
                                    doc,
                                    "stateChange/subscribe",
                                    notifyStateChange,
                                    b,
                                    httpPutListener);
                        } else {
                            subscriptionChangeRequested(R.string.confirmUnsubscribeToStateChangeNotification,
                                    doc,
                                    "stateChange/unsubscribe",
                                    notifyStateChange,
                                    b,
                                    httpPutListener);

                        }
                    }
                });
                final CompoundButton notifyIteration = (CompoundButton) documentRowView.findViewById(R.id.notifyIteration);
                notifyIteration.setChecked(doc.getIterationNotification());
                notifyIteration.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final boolean b = notifyIteration.isChecked();
                        HttpPutListener httpPutListener = new HttpPutListener() {
                            @Override
                            public void onHttpPutResult(boolean result) {
                                doc.setIterationNotification(b);
                            }
                        };
                        if (b) {
                            subscriptionChangeRequested(R.string.confirmSubscribeToIterationChangeNotification,
                                    doc,
                                    "iterationChange/subscribe",
                                    notifyIteration,
                                    b,
                                    httpPutListener);
                        } else {
                            subscriptionChangeRequested(R.string.confirmUnsubscribeToIterationChangeNotification,
                                    doc,
                                    "iterationChange/unsubscribe",
                                    notifyIteration,
                                    b,
                                    httpPutListener);

                        }
                    }
                });
            } else {
                documentRowView = new ProgressBar(DocumentListActivity.this);
            }
            return documentRowView;
        }

        private void subscriptionChangeRequested(int messageId, final Document doc, final String urlCommand, final CompoundButton compoundButton, final boolean compoundButtonState, final HttpPutListener httpPutListener){
            AlertDialog.Builder builder = new AlertDialog.Builder(DocumentListActivity.this);
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
}
