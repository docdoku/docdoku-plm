/**
 * Package containing the {@code class}es that handle the {@code Android} Google Cloud Messaging Service.
 * <p>
 * The {@code Service} {@link com.docdoku.android.plm.client.GCM.GCMRegisterService} is used to contact the DocDokuPLM
 * server to register or unregister for Google Cloud Messaging notifications. It also handles the downloading and
 * verification of the GCM Id from the GCM servers.
 * <p>
 * When a GCM message is received, {@link com.docdoku.android.plm.client.GCM.GCMWakefulBroadcastReceiver} is called,
 * which in turn passes the message to {@link com.docdoku.android.plm.client.GCM.GCMIntentService} to handle the message
 * and create a {@code Notification} on the phone.
 * <br>When this notification is clicked, {@link com.docdoku.android.plm.client.GCM.NotificationService} is called to
 * start the {@code Activity} for the document that was received.
 */
package com.docdoku.android.plm.client.GCM;