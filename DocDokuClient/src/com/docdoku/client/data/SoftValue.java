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
