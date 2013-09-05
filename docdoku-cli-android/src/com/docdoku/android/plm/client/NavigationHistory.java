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

import android.content.SharedPreferences;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;

/**
 * This class is used to keep track of which <code>Document</code>s and <code>Part</code>s the user has most recently viewed.
 *
 * <p>An instance of this path is generated with a <code>SharedPreferences</code> file in the constructor, then this instance
 * should be updated every time a new <code>Element</code> is viewed by the user. If the user wants to view his browsing history,
 * he can the fetch this instance.
 *
 * <p>Create a <code>NavigationHistory</code> instance when starting a {@link com.docdoku.android.plm.client.documents.DocumentListActivity DocumentListActivity}:
 * <p><code>
 *     private static final String PREFERENCE_DOCUMENT_HISTORY = "document history";
 * <p>
 * <p> navigationHistory = new NavigationHistory(getSharedPreferences(getCurrentWorkspace() + PREFERENCE_DOCUMENT_HISTORY, MODE_PRIVATE));
 * </code>
 *
 * <p>Update the <code>NavigationHistory</code> when viewing a <code>Document</code>
 * <p><code>
 *     navigationHistory.add(document.getIdentification());
 * </code>
 * <p>Load the full <code>NavigationHistory</code> in {@link com.docdoku.android.plm.client.documents.DocumentHistoryListActivity DocumentHistoryListActivity}
 * <p><code>
      Iterator<String> iterator = navigationHistory.getKeyIterator();
  <p> int i = 0;
  <p> while (iterator.hasNext()){
  <p>       String docKey = iterator.next();
  <p>       i++;
  <p>  }
 * </code>
 *
 * @author: martindevillers
 */
public class NavigationHistory {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.NavigationHistory";

    private static final int NAVIGATION_HISTORY_MAX_SIZE = 20;
    private static final String PREFERENCE_NAVIGATION_HISTORY_SIZE = "size";

    private final SharedPreferences sharedPreferences;
    private final LinkedHashSet<String> navigationHistory;
    private int size;

    /**
     * Create a <code>NavigationHistory</code> instance linked to the provided <code>SharedPreferences</code>.
     * <p>Loads the <code>String</code> preferences that are the Id's of the previously visited <code>Element</code>s, and
     * stores them in a <code>LinkedHashSet</code>
     *
     * @param sharedPreferences <code>SharedPreferences</code> where the <code>NavigationHistory</code> data is kept
     */
    public NavigationHistory(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
        size = sharedPreferences.getInt(PREFERENCE_NAVIGATION_HISTORY_SIZE, 0);
        navigationHistory = new LinkedHashSet<String>();
        for (int i = 0; i< size; i++){
            String key = sharedPreferences.getString(Integer.toString(i), "");
            navigationHistory.add(key);
            Log.i(LOG_TAG, "Retreiving key at position " + i + ": " + key);
        }
    }

    /**
     * Gets the <code>Iterator</code> used to load the <code>NavigationHistory</code> <code>Element</code> keys
     * @return the <code>Iterator</code>
     */
    public Iterator<String> getKeyIterator(){
        return navigationHistory.iterator();
    }

    /**
     * Returns the number of elements in the <code>NavigationHistory</code>
     * @return the navigation history size
     */
    public int getSize(){
        return size;
    }

    /**
     * Adds an entry to the <code>NavigationHistory</code>.
     * <p>If the key of the <code>Element</code> is already in history <code>LinkedHashSet</code>, then that key is moved to the top
     * of the <code>LinkedHashSet</code>. Otherwise, it is added at the top. {@link #saveHistory()} is then called to store
     * the new history into the <code>SharedPreferences</code>.
     * @param key the key of the entry to add to the history
     */
    public void add(String key){
        if (navigationHistory.contains(key)){
            navigationHistory.remove(key);
        } else {
            size++;
        }
        navigationHistory.add(key);
        if (size > NAVIGATION_HISTORY_MAX_SIZE){
            removeLastEntry();
        }
        saveHistory();
    }

    private void saveHistory(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREFERENCE_NAVIGATION_HISTORY_SIZE, size);
        Iterator<String> iterator = navigationHistory.iterator();
        int i = size - 1;
        while (iterator.hasNext()){
            String next = iterator.next();
            Log.i(LOG_TAG, "Storing key " + next + " in preferences at position " + i);
            editor.putString(Integer.toString(i), next);
            i--;
        }
        editor.commit();
    }

    private void removeLastEntry() throws NoSuchElementException, UnsupportedOperationException{
        Iterator<String> iterator = navigationHistory.iterator();
        iterator.next();
        iterator.remove();
        size--;
    }

}
