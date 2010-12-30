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
public class TagTreeItem extends TreeItem implements DocDropListener, DocDropTarget {

    private String m_workspaceId;
    private String m_label;
    private Map<String, Action> m_cmds;
    private DocDropController m_dropController;

    public TagTreeItem(String workspaceId, String label, final Map<String, Action> cmds) {
        super(new InlineHTML(HTMLUtil.imageItemHTML(ServiceLocator.getInstance().getExplorerImageBundle().tagNodeIcon(), label)));
        m_workspaceId = workspaceId;
        m_label = label;
        m_cmds = cmds;
        m_dropController = new DocDropController(getWidget(), this);
    }

    public String getLabel() {
        return m_label;
    }

    public String getWorkspaceId() {
        return m_workspaceId;
    }

    public DocDropController getDropController() {
        return m_dropController;
    }

    public void onDrop() {
        m_cmds.get("SaveTagsCommand").execute(false, true, m_label);
    }

    public void unregisterDropController(PickupDragController dc) {
        dc.unregisterDropController(m_dropController);
    }
}
