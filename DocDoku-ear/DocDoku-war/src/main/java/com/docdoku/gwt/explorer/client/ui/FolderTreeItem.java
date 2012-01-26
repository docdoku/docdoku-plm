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

package com.docdoku.gwt.explorer.client.ui;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TreeItem;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
public class FolderTreeItem extends TreeItem implements DocDropListener, DocDropTarget{

    private String m_completePath;
    private boolean m_loaded=false;
    private Map<String, Action> m_cmds;
    private DocDropController m_dropController;
    
    public FolderTreeItem(String parentCompletePath, String shortName, final Map<String, Action> cmds){
        super(new InlineHTML(HTMLUtil.imageItemHTML(ServiceLocator.getInstance().getExplorerImageBundle().closedFolderNodeIcon(),shortName)));
        m_completePath=parentCompletePath + "/" + shortName;
        m_dropController = new DocDropController(getWidget(), this);
        addItem("chargement...");
        m_cmds = cmds;
    }



    public String getCompletePath(){
        return m_completePath;
    }

    public String getShortName(){
        int index=m_completePath.lastIndexOf('/');
        return m_completePath.substring(index+1);
    }

    public FolderTreeItem getChildNode(String name){
        for (int i =0;i<getChildCount();i++){
            FolderTreeItem item=(FolderTreeItem) getChild(i);
            if(item.getShortName().equals(name))
                return item;
        }
        return null;
    }

    public void setLoaded(boolean loaded){
        m_loaded=loaded;
    }
    
    public boolean isLoaded(){
        return m_loaded;
    }

    public DocDropController getDropController() {
        return m_dropController;
    }

    public void onDrop() {
        m_cmds.get("MoveCommand").execute(getCompletePath());
    }

    public void unregisterDropController(PickupDragController dc) {
        dc.unregisterDropController(m_dropController);
    }

}
