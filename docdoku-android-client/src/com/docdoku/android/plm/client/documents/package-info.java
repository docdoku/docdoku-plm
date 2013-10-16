/**
 * Package containing {@code Activities} and {@code class}es used to model and display documents as well as document
 * lists.
 * <p>
 * {@link com.docdoku.android.plm.client.documents.Document} instances contain the data relating to a document that was
 * downloaded from the server.
 * <p>
 * {@link com.docdoku.android.plm.client.documents.DocumentListActivity} contains the basic methods for creating an
 * {@code Activity} that displays a list of documents. It is implemented in different lists:
 * <br>- {@link com.docdoku.android.plm.client.documents.DocumentCompleteListActivity}, displaying all the documents in
 * the workspace.
 * <br>- {@link com.docdoku.android.plm.client.documents.DocumentFoldersActivity}, displaying the contents of a folder,
 * allowing the user to use the filing system to find documents.
 * <br>- {@link com.docdoku.android.plm.client.documents.DocumentHistoryListActivity}, displaying the documents recently
 * viewed by the user.
 * <br>- {@link com.docdoku.android.plm.client.documents.DocumentSimpleListActivity}, displaying either the results of
 * an advanced document search or the documents checked out by the current user.
 * <p>
 * {@link com.docdoku.android.plm.client.documents.DocumentActivity} is used to show the information relating to a
 * specific document.
 */
package com.docdoku.android.plm.client.documents;