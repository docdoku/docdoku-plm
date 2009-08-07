/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009 DocDoku SARL
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
import com.docdoku.gwt.explorer.client.data.ExplorerConstants;
import com.docdoku.gwt.explorer.client.data.MDocDataSource;
import com.docdoku.gwt.explorer.client.data.MDocTemplateDataSource;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.data.TableDataSource;
import com.docdoku.gwt.explorer.client.data.WorkflowModelDataSource;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.explorer.client.ui.doc.*;
import com.docdoku.gwt.explorer.client.ui.folder.CreateFolderPanel;
import com.docdoku.gwt.explorer.client.ui.search.CompleteSearchPanel;
import com.docdoku.gwt.explorer.client.ui.template.MDocTemplatePanel;
import com.docdoku.gwt.explorer.client.ui.widget.*;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.WorkflowModelEditor;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.WorkflowModelModel;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.*;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Florent GARIN
 */
public class ExplorerPage extends DockPanel implements ResizeHandler{

    private String m_workspaceId;
    private String m_login;
    private FolderTree m_folderTree;
    private Table m_elementTable;
    private ExplorerDocumentMenuBar m_menuDocumentBarTop;
    private ExplorerDocumentMenuBar m_menuDocumentBarBottom;
    private ExplorerMenuBar m_menuBarTop;
    private ExplorerMenuBar m_menuBarBottom;
    private SearchPanel m_searchPanel;
    private SimplePanel inputPanel;
    private CreateFolderPanel m_createFolderPanel;
    private CreateMDocPanel m_createMDocPanel;
    private MDocTemplatePanel m_mdocTemplatePanel;
    private CreateVersionPanel m_createVersionPanel;

    private WorkflowModelEditor m_wfEditor ;
    private DocPanel m_docPanel;
    private TabMenuGroup m_group;
    private MasterDocumentDTO m_lastOpenedMDoc;
    private MasterDocumentTemplateDTO m_lastOpenedMDocTemplate;
    private WorkflowModelDTO m_lastOpenedWorkflowModel;
    private IconFactory m_iconFactory;
    private CompleteSearchPanel m_completeSearchPanel;
    private PickupDragController m_dndController;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();
    private final ExplorerImageBundle images = ServiceLocator.getInstance().getExplorerImageBundle();
    private DecoratorPanel elementTableDecPanel;

    public ExplorerPage(String workspaceId, String login) {
        m_workspaceId = workspaceId;
        m_login = login;
        m_dndController = new DocDragController(RootPanel.get(), false);
        ExplorerConstants.init(workspaceId);
        Window.addResizeHandler(this);
    }

    public FilesPanel getEditDocFilesPanel() {
        return m_docPanel.getFilesPanel();
    }

    public FilesPanel getEditTemplateFilesPanel() {
        return m_mdocTemplatePanel.getFilesPanel();
    }

    public void init(Map<String, Action> cmds) {
        setWidth("100%");
        m_createFolderPanel = new CreateFolderPanel(cmds);
        m_createMDocPanel = new CreateMDocPanel(cmds);
        m_createVersionPanel = new CreateVersionPanel(cmds);
        m_mdocTemplatePanel = new MDocTemplatePanel(cmds);
        m_docPanel = new DocPanel(cmds);
        m_wfEditor = new WorkflowModelEditor(cmds);

        m_iconFactory = new IconFactory(cmds);
        m_folderTree = new FolderTree(cmds, m_workspaceId, m_login, m_dndController);
        m_elementTable = new Table(null, "myTable", m_dndController);
        m_elementTable.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                Cell cell = m_elementTable.getCellForEvent(event);
                int col = cell.getCellIndex();
                int row = cell.getRowIndex();
                TableDataSource source = m_elementTable.getSource();
                if (((source.getHeaderRow() == null) || (row > 0)) && ((col > 2 && !m_elementTable.isDragNDropEnabled()) || (col > 3 && m_elementTable.isDragNDropEnabled()))) {
                    if (source instanceof MDocDataSource) {
                        if (!m_elementTable.isInCommandZone(col)) {
                            m_lastOpenedMDoc = ((MDocDataSource) source).getElementAt(row - 1);
                            showEditDocPanel();
                        }

                    } else if (source instanceof MDocTemplateDataSource) {
                        m_lastOpenedMDocTemplate = ((MDocTemplateDataSource) source).getElementAt(row - 1);
                        showEditMDocTemplatePanel();
                    } else if (source instanceof WorkflowModelDataSource) {
                        m_lastOpenedWorkflowModel = ((WorkflowModelDataSource) source).getElementAt(row - 1);
                        showEditWorkflowModelPanel();
                    }
                }
            }
        });
        m_elementTable.setStyleName("myTable");
        m_menuDocumentBarTop = new ExplorerDocumentMenuBar(cmds, this, false);
        TableNavigatorPanel tmp1 = new TableNavigatorPanel(m_elementTable);
        m_elementTable.addListener(tmp1);
        m_menuDocumentBarTop.addExtension(tmp1);
        m_menuDocumentBarBottom = new ExplorerDocumentMenuBar(cmds, this, true);
        TableNavigatorPanel tmp2 = new TableNavigatorPanel(m_elementTable);
        m_elementTable.addListener(tmp2);
        m_menuDocumentBarBottom.addExtension(tmp2);
        m_menuBarTop = new ExplorerMenuBar(cmds, this, false);
        TableNavigatorPanel tmp3 = new TableNavigatorPanel(m_elementTable);
        m_elementTable.addListener(tmp3);
        m_menuBarTop.addExtension(tmp3) ;
        m_menuBarBottom = new ExplorerMenuBar(cmds, this, true);
        TableNavigatorPanel tmp4 = new TableNavigatorPanel(m_elementTable);
        m_elementTable.addListener(tmp4);
        m_menuBarBottom.addExtension(tmp4);
        m_searchPanel = new SearchPanel(cmds, this);
        m_completeSearchPanel = new CompleteSearchPanel(cmds, this);
        m_completeSearchPanel.setVisible(false);
        m_folderTree.addSelectionHandler(new SelectionHandler<TreeItem>() {

            public void onSelection(SelectionEvent<TreeItem> event) {
                if (!m_elementTable.isAttached()) {
                    showTablePanel();
                }
                refreshElementTable(event.getSelectedItem());
            }
        });

        VerticalPanel northPanel = new VerticalPanel();
        northPanel.setHorizontalAlignment(ALIGN_RIGHT);
        northPanel.setWidth("100%");
        northPanel.add(m_searchPanel);
        northPanel.add(m_completeSearchPanel);
        add(northPanel, DockPanel.NORTH);
        setCellHorizontalAlignment(northPanel, ALIGN_RIGHT);

        VerticalPanel menuPanel = new VerticalPanel();
        m_group = new TabMenuGroup();
        TabMenu newDocTab = new TabMenu(i18n.actionNewDocument(), m_group);
        newDocTab.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                showCreateMDocPanel();
            }
        });
        TabMenu newFolderTab = new TabMenu(i18n.actionNewFolder(), m_group);
        newFolderTab.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                showCreateFolderPanel();
            }
        });
        TabMenu newTemplateTab = new TabMenu(i18n.actionNewTemplate(), m_group);
        newTemplateTab.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                showCreateMDocTemplatePanel();
            }
        });
        TabMenu newWorkflowTab = new TabMenu(i18n.actionNewWorkflow(), m_group);
        newWorkflowTab.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_lastOpenedWorkflowModel = null;
                showCreateWorkflowModelPanel();
            }
        });

        Label emptySpace = new Label();
        menuPanel.add(emptySpace);
        menuPanel.add(newDocTab);
        menuPanel.add(newFolderTab);
        menuPanel.add(newTemplateTab);
        menuPanel.add(newWorkflowTab);
        menuPanel.setCellHeight(emptySpace, "15px");
        menuPanel.setCellHorizontalAlignment(newDocTab, ALIGN_RIGHT);
        menuPanel.setCellHorizontalAlignment(newFolderTab, ALIGN_RIGHT);
        menuPanel.setCellHorizontalAlignment(newTemplateTab, ALIGN_RIGHT);
        menuPanel.setCellHorizontalAlignment(newWorkflowTab, ALIGN_RIGHT);

        ScrollPanel folderScroll = new ScrollPanel(m_folderTree);
        folderScroll.setSize("300px", "380px");
        DecoratorPanel folderTreeDecPanel = new DecoratorPanel();
        folderTreeDecPanel.setWidget(folderScroll);
        folderTreeDecPanel.addStyleName("folderTree");

        menuPanel.add(folderTreeDecPanel);
        menuPanel.setWidth("100%");
        menuPanel.setCellHorizontalAlignment(m_folderTree, ALIGN_LEFT);
        add(menuPanel, WEST);
        setCellHorizontalAlignment(menuPanel, ALIGN_RIGHT);
        setCellWidth(menuPanel, "350px");
        inputPanel = new SimplePanel();
        showTablePanel();
        inputPanel.setWidth("100%");
        elementTableDecPanel = new DecoratorPanel();
        elementTableDecPanel.addStyleName("elementTable");
        elementTableDecPanel.setWidget(inputPanel);
        elementTableDecPanel.setWidth("100%");
        add(elementTableDecPanel, CENTER);
        setCellHorizontalAlignment(elementTableDecPanel, ALIGN_LEFT);
        setCellWidth(elementTableDecPanel, "100%");


        // as a start point, we can display the workspace tree entry selected (root item), and display root content in element table
        fetchMDocsByFolder(m_workspaceId);

        m_createMDocPanel.getTemplateListBox().addChangeHandler(new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                fetchMDocId(m_workspaceId, m_createMDocPanel.getTemplateId());
            }
        });

    }

    public void setDocFiles(Map<String, String> files) {
        m_docPanel.setFiles(files);
    }

    public void setEditTemplateFiles(Map<String, String> files) {
        m_mdocTemplatePanel.setFiles(files);
    }

    public void showCreateFolderPanel() {
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel() ;
        inputPanel.clear();
        m_createFolderPanel.clearInputs();
        String selectedFolder = m_folderTree.getSelectedFolderPath();
        m_createFolderPanel.setParentFolder(selectedFolder == null ? "" : selectedFolder);
        inputPanel.setWidget(m_createFolderPanel);
    }

    public void showCreateVersionPanel(String workspaceId, String id, String version) {
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel() ;
        inputPanel.clear();
        m_createVersionPanel.clearInputs();
        m_createVersionPanel.setWorkspaceId(workspaceId);
        m_createVersionPanel.setMDocId(id);
        m_createVersionPanel.setMDocVersion(version);
        fetchWorkflowModels();
        fetchUserGroupMemberships();
        fetchUserMemberships();
        inputPanel.setWidget(m_createVersionPanel);
    }

    public void showEditDocPanel() {
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel() ;
        DocumentDTO iteration = m_lastOpenedMDoc.getLastIteration();
        if (iteration != null) {
            inputPanel.clear();
            m_docPanel.clearInputs();
            m_docPanel.setMDoc(m_lastOpenedMDoc);
            m_docPanel.setEditionMode(m_login.equals(m_lastOpenedMDoc.getCheckOutUser()));
            m_docPanel.setFiles(iteration.getFiles());
            m_docPanel.setAttributes(iteration.getAttributes());
            m_docPanel.setLinks(iteration.getLinks(), m_lastOpenedMDoc.getWorkspaceId());
            inputPanel.setWidget(m_docPanel);
        }

    }

    public void showEditDocPanel(int iter) {
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel() ;
        if (iter != m_lastOpenedMDoc.getIterations().size() - 1) {
            DocumentDTO iteration = m_lastOpenedMDoc.getIterations().get(iter);
            if (iteration != null) {
                inputPanel.clear();
                m_docPanel.clearInputs();
                m_docPanel.setMDoc(m_lastOpenedMDoc, iter);
                m_docPanel.setEditionMode(false);
                m_docPanel.setFiles(iteration.getFiles());
                m_docPanel.setAttributes(iteration.getAttributes());
                m_docPanel.setLinks(iteration.getLinks(), m_lastOpenedMDoc.getWorkspaceId());
                inputPanel.setWidget(m_docPanel);
            }
        } else {
            showEditDocPanel();
        }

    }

    public void showEditWorkflowModelPanel() {
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel() ;
        int spaceAvailable = inputPanel.getOffsetWidth() ;
        inputPanel.clear();
        WorkflowModelModel model = new WorkflowModelModel(m_lastOpenedWorkflowModel, m_workspaceId) ;
        inputPanel.setWidget(m_wfEditor);
        m_wfEditor.setWidth(spaceAvailable);
        m_wfEditor.setWorkflowModel(model);

    }

    public void showCreateWorkflowModelPanel() {
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel() ;
        
        int spaceAvailable = inputPanel.getOffsetWidth() -10 ;

        inputPanel.clear();
        inputPanel.setWidget(m_wfEditor);
        m_wfEditor.setWidth(spaceAvailable);
        m_wfEditor.setWorkflowModel(new WorkflowModelModel(m_workspaceId));
    }

    public void showEditMDocTemplatePanel() {
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel() ;
        inputPanel.clear();
        m_mdocTemplatePanel.clearInputs();
        m_mdocTemplatePanel.setTemplate(m_lastOpenedMDocTemplate);
        m_mdocTemplatePanel.setCreationMode(false);
        inputPanel.setWidget(m_mdocTemplatePanel);
    }

    public void showSearchResult(MasterDocumentDTO[] result) {
        inputPanel.clear();
        MDocDataSource source = new MDocDataSource(result, m_login);
        m_elementTable.setSource(source, true);
        m_menuDocumentBarBottom.setStyleName("myMenuBarSearch");
        m_menuDocumentBarTop.setStyleName("myMenuBarSearch");
        m_elementTable.setStylePrefix("searchTable");
        elementTableDecPanel.addStyleName("searchTable");
        showTablePanel(false);
    }

    public void showCreateMDocTemplatePanel() {
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel() ;
        inputPanel.clear();
        m_mdocTemplatePanel.clearInputs();
        m_mdocTemplatePanel.setCreationMode(true);
        inputPanel.setWidget(m_mdocTemplatePanel);

    }

    public void showCreateMDocPanel() {
        showBasicSearchPanel() ;
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        inputPanel.clear();
        m_createMDocPanel.clearInputs();
        String selectedFolder = m_folderTree.getSelectedFolderPath();
        m_createMDocPanel.setParentFolder(selectedFolder == null ? "" : selectedFolder);
        fetchWorkflowModels();
        fetchTemplates();
        fetchUserGroupMemberships();
        fetchUserMemberships();
        inputPanel.setWidget(m_createMDocPanel);
    }

    public void showTablePanel() {
        m_menuDocumentBarTop.setStyleName("myMenuBar") ;
        m_menuDocumentBarBottom.setStyleName("myMenuBar") ;
        m_elementTable.setStylePrefix("myTable");
        if(elementTableDecPanel != null){
            elementTableDecPanel.removeStyleName("searchTable");
        }
        
        showTablePanel(true);
    }

    public void showTablePanel(boolean maskSearchComplete) {
        if (maskSearchComplete){
            showBasicSearchPanel() ;
        }
        inputPanel.clear();
        m_group.unselect();

        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");

        if (m_elementTable.getSource() instanceof MDocDataSource) {
            vp.add(m_menuDocumentBarTop);
            vp.add(m_elementTable);
            vp.add(m_menuDocumentBarBottom);
        } else {
            vp.add(m_menuBarTop);
            vp.add(m_elementTable);
            vp.add(m_menuBarBottom);
        }


        inputPanel.setWidget(vp);
    }

    public void showCompleteSearchPanel() {
        m_searchPanel.setVisible(false);
        m_completeSearchPanel.setupUi();
        m_completeSearchPanel.setVisible(true);
    }

    public void showBasicSearchPanel() {
        m_searchPanel.setVisible(true);
        m_completeSearchPanel.setVisible(false);
    }

    public String getWorkspaceId() {
        return m_workspaceId;
    }

    public void selectFolderTreeItem(TreeItem item) {
        m_folderTree.setSelectedItem(item);

    }

    public TreeItem getSelectedFolderTreeItem() {
        return m_folderTree.getSelectedItem();
    }

    public String getSelectedFolderPath() {
        return m_folderTree.getSelectedFolderPath();
    }

    public String getSelectedTag() {
        return m_folderTree.getSelectedTag();
    }

    public boolean isSelectedFolderLoaded() {
        return m_folderTree.isSelectedFolderLoaded();
    }

    public MasterDocumentDTO getLastOpenedMDoc() {
        return m_lastOpenedMDoc;
    }

    public MasterDocumentTemplateDTO getLastOpenedMDocTemplate() {
        return m_lastOpenedMDocTemplate;
    }

    public List<MasterDocumentDTO> getSelectedMDocs() {
        List<MasterDocumentDTO> mdocs = new ArrayList<MasterDocumentDTO>();
        TableDataSource source = m_elementTable.getSource();
        if (source instanceof MDocDataSource) {
            MDocDataSource mdocSource = (MDocDataSource) source;
            for (int row : m_elementTable.getSelectedRows()) {
                mdocs.add(mdocSource.getElementAt(row));
            }
        }
        return mdocs;
    }

    public List<MasterDocumentTemplateDTO> getSelectedMDocTemplates() {
        List<MasterDocumentTemplateDTO> templates = new ArrayList<MasterDocumentTemplateDTO>();
        TableDataSource source = m_elementTable.getSource();
        if (source instanceof MDocTemplateDataSource) {
            MDocTemplateDataSource templateSource = (MDocTemplateDataSource) source;
            for (int row : m_elementTable.getSelectedRows()) {
                templates.add(templateSource.getElementAt(row));
            }
        }
        return templates;
    }

    public List<WorkflowModelDTO> getSelectedWorkflowModels() {
        List<WorkflowModelDTO> workflows = new ArrayList<WorkflowModelDTO>();
        TableDataSource source = m_elementTable.getSource();
        if (source instanceof WorkflowModelDataSource) {
            WorkflowModelDataSource workflowSource = (WorkflowModelDataSource) source;
            for (int row : m_elementTable.getSelectedRows()) {
                workflows.add(workflowSource.getElementAt(row));
            }
        }
        return workflows;
    }

    public void refreshElementTable() {
        refreshElementTable(getSelectedFolderTreeItem());
    }

    public void refreshElementTable(TreeItem item) {
        if (item instanceof RootTreeItem) {
            RootTreeItem rootItem = (RootTreeItem) item;
            fetchMDocsByFolder(rootItem.getWorkspaceId());
        } else if (item instanceof FolderTreeItem) {
            FolderTreeItem folderItem = (FolderTreeItem) item;
            fetchMDocsByFolder(folderItem.getCompletePath());
        } else if (item instanceof CheckedOutTreeItem) {
            CheckedOutTreeItem checkedOutItem = (CheckedOutTreeItem) item;
            fetchCheckedOutMDocs(checkedOutItem.getWorkspaceId());
        } else if (item instanceof HomeTreeItem) {
            HomeTreeItem homeItem = (HomeTreeItem) item;
            fetchMDocsByFolder(homeItem.getCompletePath());
        } else if (item instanceof TagTreeItem) {
            TagTreeItem tagItem = (TagTreeItem) item;
            fetchMDocsByTag(tagItem.getWorkspaceId(), tagItem.getLabel());
        } else if (item instanceof TemplateTreeItem) {
            TemplateTreeItem templateItem = (TemplateTreeItem) item;
            fetchMDocTemplates(templateItem.getWorkspaceId());
        } else if (item instanceof WorkflowModelTreeItem) {
            WorkflowModelTreeItem workflowItem = (WorkflowModelTreeItem) item;
            fetchWorkflowModels(workflowItem.getWorkspaceId());
        } else {
            m_elementTable.setSource(null, true);
        }
    }

    public void refreshTags() {
        m_menuDocumentBarBottom.reloadTags();
        m_menuDocumentBarTop.reloadTags();
        m_folderTree.reloadTags();
    }

    public void refreshFolder(String completePath) {
        m_folderTree.reloadFolder(completePath);
    }

    public void refreshTreeItem(TreeItem ti) {
        m_folderTree.reloadTreeItem(ti);
    }

    public void selectAllElementsInTable() {
        if (m_elementTable.getSource() != null) {
            m_elementTable.selectAllRows();
        }
    }

    public void unselectAllElementsInTable() {
        if (m_elementTable.getSource() != null) {
            m_elementTable.unselectAllRows();
        }

    }

    public void selectAllCheckedInDocs() {
        m_elementTable.unselectAllRows();
        m_elementTable.setSelectionForRows(((MDocDataSource) m_elementTable.getSource()).getCheckedInDocuments(), true);
    }

    public void selectAllCheckedOutDocs() {
        m_elementTable.unselectAllRows();
        m_elementTable.setSelectionForRows(((MDocDataSource) m_elementTable.getSource()).getCheckedOutDocuments(), true);
    }

    private void fetchWorkflowModels(String workspaceId) {
        AsyncCallback<WorkflowModelDTO[]> callback = new AsyncCallback<WorkflowModelDTO[]>() {

            public void onSuccess(WorkflowModelDTO[] workflows) {
                m_elementTable.setSource(new WorkflowModelDataSource(workflows, m_login), false);
                showTablePanel();
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getWorkflowModels(workspaceId, callback);
    }

    private void fetchMDocTemplates(String workspaceId) {
        AsyncCallback<MasterDocumentTemplateDTO[]> callback = new AsyncCallback<MasterDocumentTemplateDTO[]>() {

            public void onSuccess(MasterDocumentTemplateDTO[] templates) {
                m_elementTable.setSource(new MDocTemplateDataSource(templates, m_login), false);
                showTablePanel();
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getMDocTemplates(workspaceId, callback);
    }

    private void fetchCheckedOutMDocs(String workspaceId) {
        AsyncCallback<MasterDocumentDTO[]> callback = new AsyncCallback<MasterDocumentDTO[]>() {

            public void onSuccess(MasterDocumentDTO[] mdocs) {
                MDocDataSource mdocSource = new MDocDataSource(mdocs, m_login);
                m_elementTable.setSource(mdocSource, true);
                showTablePanel();
                fetchIterationSubscriptions();
                fetchStateSubscriptions();

                for (int row = 0; row < mdocSource.getRowCount(); row++) {
                    final MasterDocumentDTO mdoc = mdocSource.getElementAt(row);
                    Image icon = m_iconFactory.createNewVersionIcon(mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                    icon.addClickHandler(new ClickHandler() {

                        public void onClick(ClickEvent event) {
                            showCreateVersionPanel(mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                        }
                    });
                    m_elementTable.setToCommandPanel(row, 0, icon);
                }

            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getCheckedOutMDocs(workspaceId, callback);
    }

    private void fetchMDocsByFolder(String completePath) {
        AsyncCallback<MasterDocumentDTO[]> callback = new AsyncCallback<MasterDocumentDTO[]>() {

            public void onSuccess(MasterDocumentDTO[] mdocs) {
                MDocDataSource mdocSource = new MDocDataSource(mdocs, m_login);
                m_elementTable.setSource(mdocSource, true);
                showTablePanel();
                fetchIterationSubscriptions();
                fetchStateSubscriptions();

                for (int row = 0; row < mdocSource.getRowCount(); row++) {
                    final MasterDocumentDTO mdoc = mdocSource.getElementAt(row);
                    Image icon = m_iconFactory.createNewVersionIcon(mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                    icon.addClickHandler(new ClickHandler() {

                        public void onClick(ClickEvent event) {
                            showCreateVersionPanel(mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                        }
                    });
                    m_elementTable.setToCommandPanel(row, 0, icon);
                }
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().findMDocsByFolder(completePath, callback);
    }

    private void fetchMDocsByTag(String workspaceId, String label) {
        AsyncCallback<MasterDocumentDTO[]> callback = new AsyncCallback<MasterDocumentDTO[]>() {

            public void onSuccess(MasterDocumentDTO[] mdocs) {
                MDocDataSource mdocSource = new MDocDataSource(mdocs, m_login);
                m_elementTable.setSource(mdocSource, true);
                showTablePanel();
                fetchIterationSubscriptions();
                fetchStateSubscriptions();

                for (int row = 0; row < mdocSource.getRowCount(); row++) {
                    final MasterDocumentDTO mdoc = mdocSource.getElementAt(row);
                    Image icon = m_iconFactory.createNewVersionIcon(mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                    icon.addClickHandler(new ClickHandler() {

                        public void onClick(ClickEvent event) {
                            showCreateVersionPanel(mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                        }
                    });
                    m_elementTable.setToCommandPanel(row, 0, icon);
                }
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().findMDocsByTag(workspaceId, label, callback);
    }

    private void fetchIterationSubscriptions() {
        AsyncCallback<MasterDocumentDTO[]> callback = new AsyncCallback<MasterDocumentDTO[]>() {

            public void onSuccess(MasterDocumentDTO[] mdocs) {
                TableDataSource source = m_elementTable.getSource();
                if (source instanceof MDocDataSource) {
                    List<MasterDocumentDTO> lstMDocs = Arrays.asList(mdocs);
                    MDocDataSource mdocSource = (MDocDataSource) source;
                    for (int row = 0; row < mdocSource.getRowCount(); row++) {
                        MasterDocumentDTO mdoc = mdocSource.getElementAt(row);
                        if (lstMDocs.contains(mdoc)) {
                            Image icon = m_iconFactory.createIterationSubscriptionIcon(true, mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                            m_elementTable.setToIndicatorPanel(row, 1, icon);
                        } else {
                            Image icon = m_iconFactory.createIterationSubscriptionIcon(false, mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                            m_elementTable.setToIndicatorPanel(row, 1, icon);
                        }
                    }
                }
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getIterationChangeEventSubscriptions(m_workspaceId, callback);
    }

    private void fetchStateSubscriptions() {
        AsyncCallback<MasterDocumentDTO[]> callback = new AsyncCallback<MasterDocumentDTO[]>() {

            public void onSuccess(MasterDocumentDTO[] mdocs) {
                TableDataSource source = m_elementTable.getSource();
                if (source instanceof MDocDataSource) {
                    List<MasterDocumentDTO> lstMDocs = Arrays.asList(mdocs);
                    MDocDataSource mdocSource = (MDocDataSource) source;
                    for (int row = 0; row < mdocSource.getRowCount(); row++) {
                        MasterDocumentDTO mdoc = mdocSource.getElementAt(row);
                        if (lstMDocs.contains(mdoc)) {
                            Image icon = m_iconFactory.createStateSubscriptionIcon(true, mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                            m_elementTable.setToIndicatorPanel(row, 2, icon);
                        } else {
                            Image icon = m_iconFactory.createStateSubscriptionIcon(false, mdoc.getWorkspaceId(), mdoc.getId(), mdoc.getVersion());
                            m_elementTable.setToIndicatorPanel(row, 2, icon);
                        }
                    }
                }
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getStateChangeEventSubscriptions(m_workspaceId, callback);
    }

    private void fetchWorkflowModels() {
        AsyncCallback<WorkflowModelDTO[]> callback = new AsyncCallback<WorkflowModelDTO[]>() {

            public void onSuccess(WorkflowModelDTO[] wks) {
                m_createMDocPanel.setWorkflowModels(wks);
                m_createVersionPanel.setWorkflowModels(wks);
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getWorkflowModels(getWorkspaceId(), callback);
    }

    private void fetchUserGroupMemberships() {
        AsyncCallback<UserGroupDTO[]> callback = new AsyncCallback<UserGroupDTO[]>() {

            public void onSuccess(UserGroupDTO[] ms) {
                m_createMDocPanel.setUserGroupMemberships(ms);
                m_createVersionPanel.setUserGroupMemberships(ms);
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getWorkspaceUserGroupMemberships(getWorkspaceId(), callback);
    }

    private void fetchUserMemberships() {
        AsyncCallback<UserDTO[]> callback = new AsyncCallback<UserDTO[]>() {

            public void onSuccess(UserDTO[] ms) {
                m_createMDocPanel.setUserMemberships(ms);
                m_createVersionPanel.setUserMemberships(ms);
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getWorkspaceUserMemberships(getWorkspaceId(), callback);
    }

    private void fetchTemplates() {
        AsyncCallback<MasterDocumentTemplateDTO[]> callback = new AsyncCallback<MasterDocumentTemplateDTO[]>() {

            public void onSuccess(MasterDocumentTemplateDTO[] templates) {
                m_createMDocPanel.setTemplates(templates);

            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getMDocTemplates(getWorkspaceId(), callback);
    }

    private void fetchMDocId(String workspaceId, String templateId) {
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onSuccess(String mdocId) {
                m_createMDocPanel.setMDocId(mdocId);
            }

            public void onFailure(Throwable caught) {
            }
        };
        if (templateId != null) {
            ServiceLocator.getInstance().getExplorerService().generateId(workspaceId, templateId, callback);
        }
    }

    public void onResize(ResizeEvent event) {
        if (m_wfEditor.isVisible()){
            m_wfEditor.setVisible(false);
            m_wfEditor.setWidth(inputPanel.getOffsetWidth() -10);
            m_wfEditor.setVisible(true);
        }
    }

}
