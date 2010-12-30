/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.data;

import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.explorer.shared.ExplorerService;
import com.docdoku.gwt.explorer.shared.ExplorerServiceAsync;
import com.google.gwt.core.client.GWT;

/**
 *
 * @author Florent GARIN
 */
public class ServiceLocator {


    private final ExplorerServiceAsync m_explorerService = (ExplorerServiceAsync) GWT.create(ExplorerService.class);
    private final ExplorerI18NConstants m_explorerConstants = (ExplorerI18NConstants) GWT.create(ExplorerI18NConstants.class);
    private final ExplorerImageBundle m_imageBundle = (ExplorerImageBundle) GWT.create(ExplorerImageBundle.class);
    
    private final static ServiceLocator s_instance = new ServiceLocator();

    private ServiceLocator() {
        
    }
    
    public static ServiceLocator getInstance() {
        return s_instance;
    }

    
    public ExplorerServiceAsync getExplorerService(){
        return m_explorerService;
    }
    
    public ExplorerI18NConstants getExplorerI18NConstants(){
        return m_explorerConstants;
    }
    
    public ExplorerImageBundle getExplorerImageBundle(){
        return m_imageBundle;
    }
}
