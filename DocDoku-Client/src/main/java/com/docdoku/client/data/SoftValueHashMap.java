/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.data;

import java.lang.ref.*;
import java.util.*;

public class SoftValueHashMap <K,V> implements Map<K, V> {

    private ReferenceQueue<V> mQueue;
    private HashMap<K, SoftValue<V>> mInnerMap;

    public SoftValueHashMap() {
        mQueue = new ReferenceQueue<V>();
        mInnerMap = new HashMap<K, SoftValue<V>>();
    }

    public int size() {
        return mInnerMap.size();
    }

    public boolean isEmpty() {
        return mInnerMap.isEmpty();
    }

    public boolean containsKey(Object pKey) {
        return mInnerMap.containsKey(pKey);
    }

    public void clear() {
        mInnerMap.clear();
    }

    public Set<K> keySet() {
        return mInnerMap.keySet();
    }

    public boolean containsValue(Object value) {
        return mInnerMap.containsValue(new SoftValue<Object>(value));
    }

    public V remove(Object pKey) {
        SoftValue<V> ref= mInnerMap.remove(pKey);
        return ref==null?null:ref.get();
    }

    @Override
    public boolean equals(Object pObj) {
        if (!(pObj instanceof Map))
            return false;
        Map map = (Map) pObj;
        return entrySet().equals(map.entrySet());
    }

     @Override
    public int hashCode() {
        return mInnerMap.hashCode();
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException("not implemented");
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> entrySet = new HashSet<Map.Entry<K, V>>();
        Set<Map.Entry<K,SoftValue<V>>> softSet = mInnerMap.entrySet();

        for (Map.Entry<K,SoftValue<V>> softEntry : softSet) {
            entrySet.add(new SoftValueHashMap.Entry<K,V>(softEntry.getKey(), softEntry.getValue().get()));
        }
        return entrySet;
    }

    public Collection<V> values() {
        Collection<V> values = new LinkedList<V>();
        Collection<SoftValue<V>> softCollection = mInnerMap.values();

        for (SoftValue<V> softValue : softCollection) {
            values.add(softValue.get());
        }
        return values;
    }


    public V put(K pKey, V pValue) {
        SoftValue<? extends V> sv = null;
        while ((sv = (SoftValue<? extends V>)mQueue.poll()) != null)
            mInnerMap.remove(sv.getKey());
        SoftValue<V> previousValue=mInnerMap.put(pKey, new SoftValue<V>(pValue, mQueue, pKey));
        return previousValue==null?null:previousValue.get();
    }

    public V get(Object pKey) {
        SoftValue<V> sv = mInnerMap.get(pKey);
        if (sv == null)
            return null;
        else
            return sv.get();
    }

    private static class Entry<K, V> implements Map.Entry<K, V> {
        K mKey;
	    V mValue;

        public Entry(K pKey, V pValue) {
            mKey = pKey;
            mValue = pValue;
        }

        public K getKey() {
            return mKey;
        }

        public V getValue() {
            return mValue;
        }

        public V setValue(V value) {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public boolean equals(Object pObj) {
            if (!(pObj instanceof Map.Entry))
                return false;
            Map.Entry entry = (Map.Entry) pObj;

            Object otherKey = entry.getKey();
            Object otherValue = entry.getValue();

            return ((mKey == null) ? (otherKey == null) : mKey.equals(otherKey))
                    && ((mValue == null) ? (otherValue == null) : mValue.equals(otherValue));
        }

        @Override
        public int hashCode() {
            int hash = 1;
            hash = 31 * hash + ((mKey == null) ? 0 : mKey.hashCode());
            hash = 31 * hash + ((mValue == null) ? 0 : mValue.hashCode());
            return hash;
        }
    }
}