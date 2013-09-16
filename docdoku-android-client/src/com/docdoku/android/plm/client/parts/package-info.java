/**
 * Package containing {@code Activities} and {@code class}es used to model and display parts as well as part
 * lists.
 * <p>
 * {@link com.docdoku.android.plm.client.parts.Part} instances contain the data relating to a part that was
 * downloaded from the server.
 * <p>
 * {@link com.docdoku.android.plm.client.parts.PartListActivity} contains the basic methods for creating an
 * {@code Activity} that displays a list of parts. It is implemented in different lists:
 * <br>- {@link com.docdoku.android.plm.client.parts.PartCompleteListActivity}, displaying all the parts in
 * the workspace.
 * <br>- {@link com.docdoku.android.plm.client.parts.PartHistoryListActivity}, displaying the parts recently
 * viewed by the user.
 * <br>- {@link com.docdoku.android.plm.client.parts.PartSimpleListActivity}, displaying the results of an advanced
 * part search.
 * <p>
 * {@link com.docdoku.android.plm.client.parts.PartActivity} is used to show the information relating to a
 * specific part.
 */
package com.docdoku.android.plm.client.parts;