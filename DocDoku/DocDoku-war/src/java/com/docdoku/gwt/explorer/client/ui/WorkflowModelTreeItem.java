package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.google.gwt.user.client.ui.TreeItem;

/**
 *
 * @author Florent GARIN
 */
public class WorkflowModelTreeItem extends TreeItem{

    private String m_workspaceId;

    public WorkflowModelTreeItem(String workspaceId){
        super(HTMLUtil.imageItemHTML(ServiceLocator.getInstance().getExplorerImageBundle().workflowNodeIcon(),ServiceLocator.getInstance().getExplorerI18NConstants().treeWorkflows()));
        m_workspaceId=workspaceId;
    }

    public String getWorkspaceId() {
        return m_workspaceId;
    }

}
