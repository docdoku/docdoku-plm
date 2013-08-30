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
 * @author: martindevillers
 */
public class NavigationHistory {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.NavigationHistory";

    private static final int NAVIGATION_HISTORY_MAX_SIZE = 20;
    private static final String PREFERENCE_NAVIGATION_HISTORY_SIZE = "size";

    private SharedPreferences sharedPreferences;
    private LinkedHashSet<String> navigationHistory;
    private int size;

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

    public Iterator<String> getKeyIterator(){
        return navigationHistory.iterator();
    }

    public int getSize(){
        return size;
    }

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
