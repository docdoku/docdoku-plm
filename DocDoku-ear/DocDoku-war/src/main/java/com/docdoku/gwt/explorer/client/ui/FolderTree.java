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
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
public class FolderTree extends Tree {

    private RootTreeItem m_rootNode;
    private String m_workspaceId;
    private Map<String, Action> m_cmds;
    private PickupDragController m_dragController;

    public FolderTree(final Map<String, Action> cmds, final String workspaceId, String login, PickupDragController dragDocController) {
        super(ServiceLocator.getInstance().getExplorerImageBundle());
        m_cmds = cmds;
        m_rootNode = new RootTreeItem(workspaceId, login, cmds.get("MoveCommand"));
        m_workspaceId = workspaceId;
        m_dragController = dragDocController;
        m_dragController.registerDropController(m_rootNode.getDropController());
        m_dragController.registerDropController(m_rootNode.getHomeNode().getDropController());
        createLayout();
        createListener();

        // select root node :
        setSelectedItem(m_rootNode);
    }

    private void createLayout() {
        fetchFolders(m_workspaceId, m_rootNode);
        addItem(m_rootNode);
    }

    private void createListener() {
        addOpenHandler(new OpenHandler<TreeItem>() {

            public void onOpen(OpenEvent<TreeItem> event) {
                TreeItem item = event.getTarget();
                if (item instanceof FolderTreeItem) {
                    FolderTreeItem folderItem = (FolderTreeItem) item;
                    if (item.getState() && !folderItem.isLoaded()) {
                        folderItem.removeItems();
                        fetchFolders(folderItem.getCompletePath(), item);
                        folderItem.setLoaded(true);
                    }
                } else if (item instanceof HomeTreeItem) {
                    HomeTreeItem homeItem = (HomeTreeItem) item;
                    if (item.getState() && !homeItem.isLoaded()) {
                        fetchFolders(homeItem.getCompletePath(), item);
                        homeItem.setLoaded(true);
                    }
                } else if (item instanceof TagRootTreeItem) {
                    TagRootTreeItem tagRootItem = (TagRootTreeItem) item;
                    if (item.getState() && !tagRootItem.isLoaded()) {
                        tagRootItem.removeItems();
                        fetchTags(item);
                        tagRootItem.setLoaded(true);
                    }
                }
            }
        });
    }

    public String getSelectedTag() {
        String tag = null;
        TreeItem ti = getSelectedItem();
        if (ti instanceof TagTreeItem) {
            tag = ((TagTreeItem) ti).getLabel();
        }
        return tag;
    }

    public String getSelectedFolderPath() {
        String folderPath = null;
        TreeItem ti = getSelectedItem();
        if (ti instanceof FolderTreeItem) {
            folderPath = ((FolderTreeItem) ti).getCompletePath();
        } else if (ti instanceof HomeTreeItem) {
            folderPath = ((HomeTreeItem) ti).getCompletePath();
        } else if (ti instanceof RootTreeItem) {
            folderPath = ((RootTreeItem) ti).getCompletePath();
        }
        return folderPath;
    }

    public boolean isSelectedFolderLoaded() {
        boolean loaded = false;
        TreeItem ti = getSelectedItem();
        if (ti instanceof FolderTreeItem) {
            loaded = ((FolderTreeItem) ti).isLoaded();
        } else if (ti instanceof HomeTreeItem) {
            loaded = ((HomeTreeItem) ti).isLoaded();
        } else if (ti instanceof RootTreeItem) {
            loaded = true;
        } else {
            loaded = false;
        }
        return loaded;
    }

    private void fetchFolders(final String completePath, final TreeItem treeItem) {

        AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {

            public void onSuccess(String[] firstLevelFolders) {
                for (int i = 0; i < firstLevelFolders.length; i++) {
                    FolderTreeItem item = new FolderTreeItem(completePath, firstLevelFolders[i], m_cmds);
                    treeItem.addItem(item);
                    m_dragController.registerDropController(item.getDropController());
                }
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getFolders(completePath, callback);
    }

    public void reloadTags() {
        TreeItem item = m_rootNode.getTagRootNode();
        for (int i = 0; i < item.getChildCount(); i++) {
            TagTreeItem tagItem = (TagTreeItem) item.getChild(i);
            m_dragController.unregisterDropController(tagItem.getDropController());
        }
        item.removeItems();
        fetchTags(item);
    }

    public void reloadTreeItem(TreeItem ti) {

        if (ti instanceof FolderTreeItem) {
            FolderTreeItem folderNode = (FolderTreeItem) ti;
            // unregister drop controller in children
            unregisterAllDropControllers(ti, false);
            folderNode.removeItems();
            folderNode.setLoaded(true);
            fetchFolders(folderNode.getCompletePath(), folderNode);
        } else if (ti instanceof HomeTreeItem) {
            // unregister drop controller in children
            unregisterAllDropControllers(ti, false);
            HomeTreeItem homeNode = (HomeTreeItem) ti;
            homeNode.removeItems();
            homeNode.addStaticFolders();
            homeNode.setLoaded(true);
            fetchFolders(homeNode.getCompletePath(), homeNode);

        } else if (ti instanceof RootTreeItem) {
            unregisterAllDropControllersForRootNode();
            RootTreeItem rootNode = (RootTreeItem) ti;
            rootNode.removeItems();
            rootNode.addStaticFolders();
            fetchFolders(rootNode.getWorkspaceId(), rootNode);
        }
    }

    public void reloadFolder(String folderPath) {
        String[] pathElements = folderPath.split("/");
        if (m_rootNode.getWorkspaceId().equals(pathElements[0])) {
            if (pathElements.length == 1) {
                unregisterAllDropControllersForRootNode();
                m_rootNode.removeItems();
                m_rootNode.addStaticFolders();
                fetchFolders(folderPath, m_rootNode);
                return;
            } else {
                if (pathElements[1].charAt(0) == '~') {
                    HomeTreeItem homeNode = m_rootNode.getHomeNode();
                    if (homeNode.getLogin().equals(pathElements[1].substring(1))) {
                        if (pathElements.length == 2) {
                            unregisterAllDropControllers(homeNode, false);
                            homeNode.removeItems();
                            homeNode.addStaticFolders();
                            homeNode.setLoaded(true);
                            fetchFolders(folderPath, homeNode);
                            return;
                        } else {
                            FolderTreeItem item = homeNode.getChildNode(pathElements[2]);
                            item = findTreeItem(item, pathElements, 3);
                            unregisterAllDropControllers(item, false);

                            item.removeItems();
                            item.setLoaded(true);
                            fetchFolders(folderPath, item);

                        }
                    }
                } else {
                    FolderTreeItem item = m_rootNode.getChildNode(pathElements[1]);
                    item = findTreeItem(item, pathElements, 2);
                    unregisterAllDropControllers(item, false);
                    item.removeItems();
                    item.setLoaded(true);
                    fetchFolders(folderPath, item);
                }
            }
        }
    }

    private FolderTreeItem findTreeItem(FolderTreeItem startingNode, String[] pathElements, int offset) {
        FolderTreeItem item = startingNode;
        for (int i = offset; i < pathElements.length; i++) {
            item = item.getChildNode(pathElements[i]);
        }
        return item;
    }

    private void fetchTags(final TreeItem treeItem) {
        AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {

            public void onSuccess(String[] tags) {
                for (int i = 0; i < tags.length; i++) {
                    TagTreeItem item = new TagTreeItem(m_workspaceId, tags[i], m_cmds);
                    treeItem.addItem(item);
                    m_dragController.registerDropController(item.getDropController());
                }
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getTags(m_workspaceId, callback);
    }

    private void unregisterAllDropControllers(TreeItem item, boolean unregisterMe) {
        for (int i = 0; i < item.getChildCount(); i++) {
            unregisterAllDropControllers(item.getChild(i), true);
        }
        if (item instanceof DocDropTarget && unregisterMe) {
            DocDropTarget target = (DocDropTarget) item;
            target.unregisterDropController(m_dragController);
        }
    }

    private void unregisterAllDropControllersForRootNode() {
        for (int i = 0; i < m_rootNode.getChildCount(); i++) {
            if (m_rootNode.getChild(i) instanceof FolderTreeItem) {
                unregisterAllDropControllers(m_rootNode.getChild(i), true);
            }
        }
    }
}
