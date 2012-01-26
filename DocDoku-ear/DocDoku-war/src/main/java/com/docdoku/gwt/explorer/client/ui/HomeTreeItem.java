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

/**
 *
 * @author Florent Garin
 */
public class HomeTreeItem extends TreeItem implements DocDropListener, DocDropTarget{

    
    private String m_completePath;
    private boolean m_loaded=false;
    private DocDropController m_dropController ;
    private CheckedOutTreeItem m_checkedOutNode;
    private final Action m_action ;
    
    public HomeTreeItem(String workspaceId, String login, final Action moveAction){
        super(new InlineHTML(HTMLUtil.imageItemHTML(ServiceLocator.getInstance().getExplorerImageBundle().homeNodeIcon(),"~"+login)));
        m_completePath=workspaceId + "/~" + login;
        m_checkedOutNode=new CheckedOutTreeItem(workspaceId);
        m_dropController = new DocDropController(this.getWidget(), this) ;
        addStaticFolders();
        m_action = moveAction ;
    }

    public void addStaticFolders(){
        addItem(m_checkedOutNode);
    }

    public FolderTreeItem getChildNode(String name){
        for (int i =1;i<getChildCount();i++){
            FolderTreeItem item=(FolderTreeItem) getChild(i);
            if(item.getShortName().equals(name))
                return item;
        }
        return null;
    }

    public String getLogin(){
        int index = m_completePath.indexOf('~');
        return m_completePath.substring(index+1);
    }
    public String getCompletePath(){
        return m_completePath;
    }
    
    public void setLoaded(boolean loaded){
        m_loaded=loaded;
    }
    
    public boolean isLoaded(){
        return m_loaded;
    }

    public void onDrop() {
        m_action.execute(m_completePath);
    }

    public void unregisterDropController(PickupDragController dc) {
        dc.unregisterDropController(m_dropController);
    }

    public DocDropController getDropController() {
        return m_dropController;
    }


}
