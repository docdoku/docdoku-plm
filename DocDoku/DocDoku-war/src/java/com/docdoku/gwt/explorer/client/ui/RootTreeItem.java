package com.docdoku.gwt.explorer.client.ui;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;

import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TreeItem;


/**
 *
 * @author Florent GARIN
 */
public class RootTreeItem extends TreeItem implements DocDropTarget, DocDropListener{

    private String m_workspaceId;
    
    private HomeTreeItem m_homeNode;
    private WorkflowModelTreeItem m_wfModelNode;
    private TemplateTreeItem m_templateNode;
    private TagRootTreeItem m_tagNode;
    private DocDropController m_dropController ;
    private final Action m_action ;

    public RootTreeItem(String workspaceId, String login, final Action moveAction){
        super(new InlineHTML(HTMLUtil.imageItemHTML(ServiceLocator.getInstance().getExplorerImageBundle().rootNodeIcon(),workspaceId)));
        m_action = moveAction ;
        m_dropController = new DocDropController(this.getWidget(), this) ;
        m_homeNode = new HomeTreeItem(workspaceId, login, moveAction);
        m_wfModelNode = new WorkflowModelTreeItem(workspaceId);
        m_templateNode = new TemplateTreeItem(workspaceId);
        m_tagNode = new TagRootTreeItem();
        m_workspaceId=workspaceId;
        addStaticFolders();
    }

    public TagRootTreeItem getTagRootNode() {
        return m_tagNode;
    }

    public FolderTreeItem getChildNode(String name){
        for (int i =4;i<getChildCount();i++){
            FolderTreeItem item=(FolderTreeItem) getChild(i);
            if(item.getShortName().equals(name))
                return item;
        }
        return null;
    }

    public HomeTreeItem getHomeNode(){
        return m_homeNode;
    }
    public String getWorkspaceId() {
        return m_workspaceId;
    }

    public String getCompletePath(){
        return getWorkspaceId();
    }
    
    public void addStaticFolders(){
        addItem(m_homeNode);
        addItem(m_wfModelNode);
        addItem(m_templateNode);
        addItem(m_tagNode);
    }

    public void unregisterDropController(PickupDragController dc) {
        dc.unregisterDropController(m_dropController);
    }

    public void onDrop() {
        m_action.execute(m_workspaceId);
    }

    public DocDropController getDropController() {
        return m_dropController;
    }


}
