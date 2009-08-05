package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.google.gwt.user.client.ui.TreeItem;

/**
 *
 * @author Florent GARIN
 */
public class TagRootTreeItem extends TreeItem{

    private boolean m_loaded=false;
    
    
    public TagRootTreeItem(){
        super(HTMLUtil.imageItemHTML(ServiceLocator.getInstance().getExplorerImageBundle().tagRootNodeIcon(),ServiceLocator.getInstance().getExplorerI18NConstants().treeTags()));
        addItem("chargement...");
    }
    
    public void setLoaded(boolean loaded){
        m_loaded=loaded;
    }
    
    public boolean isLoaded(){
        return m_loaded;
    }
}
