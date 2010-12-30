package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.google.gwt.user.client.ui.TreeItem;

/**
 *
 * @author Florent GARIN
 */
public class TemplateTreeItem extends TreeItem{

    private String m_workspaceId;

    public TemplateTreeItem(String workspaceId){
        super(HTMLUtil.imageItemHTML(ServiceLocator.getInstance().getExplorerImageBundle().templateNodeIcon(),ServiceLocator.getInstance().getExplorerI18NConstants().treeTemplates()));
        m_workspaceId=workspaceId;
    }

    public String getWorkspaceId() {
        return m_workspaceId;
    }
}
