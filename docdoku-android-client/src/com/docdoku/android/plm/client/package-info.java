/**
 * Package containing {@code Android} components and {@code class}es used to run the application and the GUI.
 * <p>
 * {@link com.docdoku.android.plm.client.Element} and {@link com.docdoku.android.plm.client.ElementActivity} are
 * {@code abstract class}es used as parents for the par and document models
 * ({@link com.docdoku.android.plm.client.documents.Document} and {@link com.docdoku.android.plm.client.parts.Part}) and
 * the {@code Activities} used to present them to the user ({@link com.docdoku.android.plm.client.documents.DocumentActivity}
 * and {@link com.docdoku.android.plm.client.parts.PartActivity}). {@link com.docdoku.android.plm.client.SearchActivity}
 * is an {@code Activity} used as a base to do document and part advanced searches, through the implementations in
 * {@link com.docdoku.android.plm.client.documents.DocumentSearchActivity} and
 * {@link com.docdoku.android.plm.client.parts.PartSearchActivity}. In document and part {@code Activities}, instances
 * of {@link com.docdoku.android.plm.client.NavigationHistory} will be used to keep track of the history of elements
 * viewed by the user so that he may access it later on.
 * <p>
 * {@link com.docdoku.android.plm.client.SimpleActionBarActivity} is a parent {@code class} for all {@code Activities},
 * except those that handle the connection process, found in {@link connection}. It handles the behaviour of the
 * {@code ActionBar} and the {@code DrawerMenu}, found in {@link com.docdoku.android.plm.client.MenuFragment}, as well
 * as provides some useful methods for accessing the current {@link com.docdoku.android.plm.client.Session} to which the
 * user is connected. {@link com.docdoku.android.plm.client.SearchActionBarActivity} is an implementation that adds a
 * collapsible {@code SearchView} to the {@code ActionBar} for {@code Activities} that require one.
 */
package com.docdoku.android.plm.client;