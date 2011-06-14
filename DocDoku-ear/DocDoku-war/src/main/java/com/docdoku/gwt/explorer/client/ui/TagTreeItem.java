/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
