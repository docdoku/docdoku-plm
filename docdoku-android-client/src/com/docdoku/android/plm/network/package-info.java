/**
 * Package containing {@code class}es used for network queries.
 * <p>
 * {@link com.docdoku.android.plm.network.HttpTask} provides a parent class for Http requests by maintaining the server
 * connection information in {@code static} fields and providing useful methods for downloading and uploading data. It
 * inherits from {@code AsyncTask}, meaning that all the other {class}es in this package implement the method
 * {@code doInBackground()} which contains the code to be executed asynchronously to perform network operations.
 */
package com.docdoku.android.plm.network;