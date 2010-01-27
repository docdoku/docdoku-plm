/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

public class SoftValue<T> extends SoftReference<T>{
  private Object mKey;
  
  public SoftValue(T pReferent, ReferenceQueue<? super T> pQueue, Object pKey){
    super(pReferent, pQueue);
    mKey = pKey;
  }
  public SoftValue(T pReferent){
    super(pReferent);  
  }
  public Object getKey() {
    return mKey;
  }
  
  public boolean equals(Object pObj) {
    if (!(pObj instanceof SoftValue))
  		return false;
    SoftValue sv=(SoftValue)pObj;
    
    Object referent=get();    
    return ((referent==null)?sv.get()==null:referent.equals(sv.get()));
    

  }
  public int hashCode(){
    Object referent=get();
    return (referent==null?0:referent.hashCode());
  }

}
