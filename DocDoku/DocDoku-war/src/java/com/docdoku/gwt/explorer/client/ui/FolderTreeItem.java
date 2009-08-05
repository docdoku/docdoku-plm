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
 * @author Florent GARIN
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
