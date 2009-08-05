/*
 * ApplicationException.java
 *
 * Created on 23 février 2008, 17:26
 *
 */

package com.docdoku.gwt.explorer.common;

import java.io.Serializable;

/**
 *
 * @author Florent GARIN
 */
public class ApplicationException extends Exception implements Serializable{
    
    public ApplicationException() {
        
    }
    public ApplicationException(String message) {
        super(message);
    }
    
}
