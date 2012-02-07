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

import com.docdoku.server.rest.dto.DocumentMasterDTO;
import com.docdoku.server.rest.dto.DocumentDTO;
import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ExplorerConstants;
import com.docdoku.gwt.explorer.client.data.DocMTableModel;
import com.docdoku.gwt.explorer.client.data.DocMTemplateTableModel;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.data.WorkflowModelTableModel;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.explorer.client.ui.doc.*;
import com.docdoku.gwt.explorer.client.ui.folder.CreateFolderPanel;
import com.docdoku.gwt.explorer.client.ui.pagemanager.DocMCheckedOutBackend;
import com.docdoku.gwt.explorer.client.ui.pagemanager.DocMFolderBackend;
import com.docdoku.gwt.explorer.client.ui.pagemanager.DocMSearchBackend;
import com.docdoku.gwt.explorer.client.ui.pagemanager.DocMTagBackend;
import com.docdoku.gwt.explorer.client.ui.pagemanager.DocMTemplateBackend;
import com.docdoku.gwt.explorer.client.ui.pagemanager.PageHandler;
import com.docdoku.gwt.explorer.client.ui.pagemanager.PageManager;
import com.docdoku.gwt.explorer.client.ui.pagemanager.PageManagerBackend;
import com.docdoku.gwt.explorer.client.ui.pagemanager.PageManagerEvent;
import com.docdoku.gwt.explorer.client.ui.pagemanager.WorkflowModelBackend;
import com.docdoku.gwt.explorer.client.ui.search.CompleteSearchPanel;
import com.docdoku.gwt.explorer.client.ui.template.DocMTemplatePanel;
import com.docdoku.gwt.explorer.client.ui.widget.*;
import com.docdoku.gwt.client.ui.widget.table.TableClickEvent;
import com.docdoku.gwt.client.ui.widget.table.TableClickHandler;
import com.docdoku.gwt.client.ui.widget.table.TableModel;
import com.docdoku.gwt.client.ui.widget.table.TableProfile;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.WorkflowModelEditor;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.WorkflowModelModel;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Florent GARIN
 */
public class ExplorerPage extends DockPanel implements ResizeHandler, PageHandler {

    private static final int TABLE_PAGE_SIZE = 50;

    private String m_workspaceId;
    private String m_login;
    private FolderTree m_folderTree;
    private ExplorerTable m_elementTable;
    private ExplorerDocumentMenuBar m_menuDocumentBarTop;
    private ExplorerDocumentMenuBar m_menuDocumentBarBottom;
    private ExplorerMenuBar m_menuBarTop;
    private ExplorerMenuBar m_menuBarBottom;
    private SearchPanel m_searchPanel;
    private SimplePanel inputPanel;
    private CreateFolderPanel m_createFolderPanel;
    private CreateDocMPanel m_createDocMPanel;
    private DocMTemplatePanel m_docMTemplatePanel;
    private CreateVersionPanel m_createVersionPanel;
    private WorkflowModelEditor m_wfEditor;
    private DocPanel m_docPanel;
    private TabMenuGroup m_group;
    private DocumentMasterDTO m_lastOpenedDocM;
    private DocumentMasterTemplateDTO m_lastOpenedDocMTemplate;
    private WorkflowModelDTO m_lastOpenedWorkflowModel;
    private IconFactory m_iconFactory;
    private CompleteSearchPanel m_completeSearchPanel;
    private DocDragController m_dndController;
    private ExplorerTableProfileCollection m_tableProfiles;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();
    private final ExplorerImageBundle images = ServiceLocator.getInstance().getExplorerImageBundle();
    private DecoratorPanel elementTableDecPanel;
    private TableProfile desiredProfile;
    private PageManager pageManager;

    public ExplorerPage(String workspaceId, String login) {
        m_workspaceId = workspaceId;
        m_login = login;
        m_dndController = new DocDragController(RootPanel.get(), false);
        m_dndController.setBehaviorDragProxy(true);
        ExplorerConstants.init(workspaceId);
        Window.addResizeHandler(this);
        pageManager = new PageManager();
        pageManager.setPageSize(TABLE_PAGE_SIZE);
    }

    public FilesPanel getEditDocFilesPanel() {
        return m_docPanel.getFilesPanel();
    }

    public FilesPanel getEditTemplateFilesPanel() {
        return m_docMTemplatePanel.getFilesPanel();
    }

    public void init(Map<String, Action> cmds) {
        setWidth("100%");
        m_createFolderPanel = new CreateFolderPanel(cmds);
        m_createDocMPanel = new CreateDocMPanel(cmds);
        m_createVersionPanel = new CreateVersionPanel(cmds);
        m_docMTemplatePanel = new DocMTemplatePanel(cmds);
        m_docPanel = new DocPanel(cmds);
        m_wfEditor = new WorkflowModelEditor(cmds);

        m_iconFactory = new IconFactory(cmds);
        m_tableProfiles = new ExplorerTableProfileCollection(m_iconFactory);
        m_folderTree = new FolderTree(cmds, m_workspaceId, m_login, m_dndController);

        m_menuDocumentBarTop = new ExplorerDocumentMenuBar(cmds, this, false);
        TableNavigator tmp1 = new TableNavigator(pageManager);
        pageManager.addPageHandler(tmp1);
        m_menuDocumentBarTop.addExtension(tmp1);
        m_menuDocumentBarBottom = new ExplorerDocumentMenuBar(cmds, this, true);
        TableNavigator tmp2 = new TableNavigator(pageManager);
        m_menuDocumentBarBottom.addExtension(tmp2);
        pageManager.addPageHandler(tmp2);
        m_menuBarTop = new ExplorerMenuBar(cmds, this, false);
        TableNavigator tmp3 = new TableNavigator(pageManager);
        pageManager.addPageHandler(tmp3);
        m_menuBarTop.addExtension(tmp3);
        m_menuBarBottom = new ExplorerMenuBar(cmds, this, true);
        TableNavigator tmp4 = new TableNavigator(pageManager);
        pageManager.addPageHandler(tmp4);
        m_menuBarBottom.addExtension(tmp4);

        m_elementTable = new ExplorerTable(m_menuBarTop, m_menuBarBottom, m_menuDocumentBarTop, m_menuDocumentBarBottom, m_dndController);
        m_elementTable.addTableClickHandler(new TableClickHandler() {

            public void onClick(TableClickEvent event) {
                TableModel model = m_elementTable.getTableModel();
                if (model instanceof DocMTableModel) {
                    m_lastOpenedDocM = ((DocMTableModel) model).getValueAt(event.getTableModelIndex().getRow());
                    showEditDocPanel();
                } else if (model instanceof DocMTemplateTableModel) {
                    m_lastOpenedDocMTemplate = ((DocMTemplateTableModel) model).getValueAt(event.getTableModelIndex().getRow());
                    showEditDocMTemplatePanel();
                } else if (model instanceof WorkflowModelTableModel) {
                    m_lastOpenedWorkflowModel = ((WorkflowModelTableModel) model).getValueAt(event.getTableModelIndex().getRow());
                    showEditWorkflowModelPanel();
                }

            }
        });

        m_dndController.setTable(m_elementTable.getInnerTable());
        m_elementTable.getInnerTable().setStyleName("docdoku-TableWidget");
        m_elementTable.setWidth("100%");

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
        //northPanel.setWidth("100%");
        northPanel.add(m_searchPanel);
        northPanel.add(m_completeSearchPanel);
        add(northPanel, DockPanel.NORTH);
        setCellHorizontalAlignment(northPanel, ALIGN_RIGHT);

        VerticalPanel menuPanel = new VerticalPanel();
        m_group = new TabMenuGroup();
        TabMenu newDocTab = new TabMenu(i18n.actionNewDocument(), m_group);
        newDocTab.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                showCreateDocMPanel();
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
                showCreateDocMTemplatePanel();
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
        fetchDocMsByFolder(m_workspaceId);

        m_createDocMPanel.getTemplateListBox().addChangeHandler(new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                fetchDocMId(m_workspaceId, m_createDocMPanel.getTemplateId());
            }
        });

        pageManager.addPageHandler(this);
    }

    public void setDocFiles(Map<String, String> files) {
        m_docPanel.setFiles(files);
    }

    public void setEditTemplateFiles(Map<String, String> files) {
        m_docMTemplatePanel.setFiles(files);
    }

    public void showCreateFolderPanel() {
        if (elementTableDecPanel != null) {
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel();
        inputPanel.clear();
        m_createFolderPanel.clearInputs();
        String selectedFolder = m_folderTree.getSelectedFolderPath();
        m_createFolderPanel.setParentFolder(selectedFolder == null ? "" : selectedFolder);
        inputPanel.setWidget(m_createFolderPanel);
    }

    public void showCreateVersionPanel(String workspaceId, String id, String version) {
        if (elementTableDecPanel != null) {
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel();
        inputPanel.clear();
        m_createVersionPanel.clearInputs();
        m_createVersionPanel.setWorkspaceId(workspaceId);
        m_createVersionPanel.setDocMId(id);
        m_createVersionPanel.setDocMVersion(version);
        fetchWorkflowModels();
        //fetchUserGroupMemberships();
        //fetchUserMemberships();
        inputPanel.setWidget(m_createVersionPanel);
    }

    public void showEditDocPanel() {
        if (elementTableDecPanel != null) {
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel();
        DocumentDTO iteration = null;//m_lastOpenedDocM.getLastIteration();
        if (iteration != null) {
            inputPanel.clear();
            m_docPanel.clearInputs();
            m_docPanel.setDocM(m_lastOpenedDocM);
            //m_docPanel.setEditionMode(m_lastOpenedDocM.getCheckOutUser() != null && m_login.equals(m_lastOpenedDocM.getCheckOutUser().getLogin()));
            m_docPanel.setFiles(iteration.getAttachedFiles());
            m_docPanel.setAttributes(iteration.getInstanceAttributes());
            m_docPanel.setLinks(iteration.getLinkedDocuments(), m_lastOpenedDocM.getWorkspaceId());
            inputPanel.setWidget(m_docPanel);
        }

    }

    public void showEditDocPanel(int iter) {
        if (elementTableDecPanel != null) {
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel();
        /*
        if (iter != m_lastOpenedDocM.getDocumentIterations().size() - 1) {
            DocumentDTO iteration = m_lastOpenedDocM.getDocumentIterations().get(iter);
            if (iteration != null) {
                inputPanel.clear();
                m_docPanel.clearInputs();
                m_docPanel.setDocM(m_lastOpenedDocM, iter);
                m_docPanel.setEditionMode(false);
                m_docPanel.setFiles(iteration.getAttachedFiles());
                m_docPanel.setAttributes(iteration.getInstanceAttributes());
                m_docPanel.setLinks(iteration.getLinkedDocuments(), m_lastOpenedDocM.getWorkspaceId());
                inputPanel.setWidget(m_docPanel);
            }
        } else {
            showEditDocPanel();
        }*/

    }

    public void showEditWorkflowModelPanel() {
        if (elementTableDecPanel != null) {
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel();
        int spaceAvailable = inputPanel.getOffsetWidth();
        inputPanel.clear();
        WorkflowModelModel model = new WorkflowModelModel(m_lastOpenedWorkflowModel, m_workspaceId);
        inputPanel.setWidget(m_wfEditor);
        m_wfEditor.setWidth(spaceAvailable);
        m_wfEditor.setWorkflowModel(model);

    }

    public void showCreateWorkflowModelPanel() {
        if (elementTableDecPanel != null) {
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel();

        int spaceAvailable = inputPanel.getOffsetWidth() - 10;

        inputPanel.clear();
        inputPanel.setWidget(m_wfEditor);
        m_wfEditor.setWidth(spaceAvailable);
        m_wfEditor.setWorkflowModel(new WorkflowModelModel(m_workspaceId));
    }

    public void showEditDocMTemplatePanel() {
        if (elementTableDecPanel != null) {
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel();
        inputPanel.clear();
        m_docMTemplatePanel.clearInputs();
        m_docMTemplatePanel.setTemplate(m_lastOpenedDocMTemplate);
        m_docMTemplatePanel.setCreationMode(false);
        inputPanel.setWidget(m_docMTemplatePanel);
    }

    public void showSearchResult(DocumentMasterDTO[] result) {
        inputPanel.clear();
        DocMTableModel source = new DocMTableModel(result, m_login, true);
        m_elementTable.setModel(source, m_tableProfiles.getProfile("searchProfile"));
        m_menuDocumentBarBottom.setStyleName("myMenuBarSearch");
        m_menuDocumentBarTop.setStyleName("myMenuBarSearch");
        elementTableDecPanel.addStyleName("searchTable");
        showTablePanel(false);
    }

    public void showSearchResult(PageManagerBackend backend) {
        inputPanel.clear();
        desiredProfile = m_tableProfiles.getProfile("searchProfile");
        elementTableDecPanel.addStyleName("searchTable");
        pageManager.setPageManagerBackend(backend);
    }

    public void showCreateDocMTemplatePanel() {
        if (elementTableDecPanel != null) {
            elementTableDecPanel.removeStyleName("searchTable");
        }
        showBasicSearchPanel();
        inputPanel.clear();
        m_docMTemplatePanel.clearInputs();
        m_docMTemplatePanel.setCreationMode(true);
        inputPanel.setWidget(m_docMTemplatePanel);

    }

    public void showCreateDocMPanel() {
        showBasicSearchPanel();
        if (elementTableDecPanel != null) {
            elementTableDecPanel.removeStyleName("searchTable");
        }
        inputPanel.clear();
        m_createDocMPanel.clearInputs();
        String selectedFolder = m_folderTree.getSelectedFolderPath();
        m_createDocMPanel.setParentFolder(selectedFolder == null ? "" : selectedFolder);
        fetchWorkflowModels();
        fetchTemplates();
        //fetchUserGroupMemberships();
        //fetchUserMemberships();
        inputPanel.setWidget(m_createDocMPanel);
    }

    public void showTablePanel() {
        if (pageManager.getCurrentBackend() instanceof DocMSearchBackend) {
            elementTableDecPanel.addStyleName("searchTable");
        } else {
            if (elementTableDecPanel != null) {
                elementTableDecPanel.removeStyleName("searchTable");
            }
        }
        showTablePanel(true);
    }

    public void showTablePanel(boolean maskSearchComplete) {
        if (maskSearchComplete) {
            showBasicSearchPanel();
        }
        inputPanel.clear();
        m_group.unselect();
        inputPanel.setWidget(m_elementTable);
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

    public DocumentMasterDTO getLastOpenedDocM() {
        return m_lastOpenedDocM;
    }

    public DocumentMasterTemplateDTO getLastOpenedDocMTemplate() {
        return m_lastOpenedDocMTemplate;
    }

    public List<DocumentMasterDTO> getSelectedDocMs() {
        List<DocumentMasterDTO> docMs = new ArrayList<DocumentMasterDTO>();
        TableModel source = m_elementTable.getTableModel();
        if (source instanceof DocMTableModel) {
            DocMTableModel docMSource = (DocMTableModel) source;
            for (int row : m_elementTable.getSelectedRows()) {
                docMs.add(docMSource.getValueAt(row));
            }
        }
        return docMs;
    }

    public List<DocumentMasterTemplateDTO> getSelectedDocMTemplates() {
        List<DocumentMasterTemplateDTO> templates = new ArrayList<DocumentMasterTemplateDTO>();
        TableModel source = m_elementTable.getTableModel();
        if (source instanceof DocMTemplateTableModel) {
            DocMTemplateTableModel templateSource = (DocMTemplateTableModel) source;
            for (int row : m_elementTable.getSelectedRows()) {
                templates.add(templateSource.getValueAt(row));
            }
        }
        return templates;
    }

    public List<WorkflowModelDTO> getSelectedWorkflowModels() {
        List<WorkflowModelDTO> workflows = new ArrayList<WorkflowModelDTO>();
        TableModel source = m_elementTable.getTableModel();
        if (source instanceof WorkflowModelTableModel) {
            WorkflowModelTableModel workflowSource = (WorkflowModelTableModel) source;
            for (int row : m_elementTable.getSelectedRows()) {
                workflows.add(workflowSource.getValueAt(row));
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
            fetchDocMsByFolder(rootItem.getWorkspaceId());
        } else if (item instanceof FolderTreeItem) {
            FolderTreeItem folderItem = (FolderTreeItem) item;
            fetchDocMsByFolder(folderItem.getCompletePath());
        } else if (item instanceof CheckedOutTreeItem) {
            CheckedOutTreeItem checkedOutItem = (CheckedOutTreeItem) item;
            fetchCheckedOutDocMs(checkedOutItem.getWorkspaceId());
        } else if (item instanceof HomeTreeItem) {
            HomeTreeItem homeItem = (HomeTreeItem) item;
            fetchDocMsByFolder(homeItem.getCompletePath());
        } else if (item instanceof TagTreeItem) {
            TagTreeItem tagItem = (TagTreeItem) item;
            fetchDocMsByTag(tagItem.getWorkspaceId(), tagItem.getLabel());
        } else if (item instanceof TemplateTreeItem) {
            TemplateTreeItem templateItem = (TemplateTreeItem) item;
            fetchDocMTemplates(templateItem.getWorkspaceId());
        } else if (item instanceof WorkflowModelTreeItem) {
            WorkflowModelTreeItem workflowItem = (WorkflowModelTreeItem) item;
            fetchWorkflowModels(workflowItem.getWorkspaceId());
        } else {
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
        if (m_elementTable.getTableModel() != null) {
            m_elementTable.selectAllRows();
        }
    }

    public void unselectAllElementsInTable() {
        if (m_elementTable.getTableModel() != null) {
            m_elementTable.unselectAllRows();
        }

    }

    public void selectAllCheckedInDocs() {
        m_elementTable.unselectAllRows();
        m_elementTable.setSelectionForRows(((DocMTableModel) m_elementTable.getTableModel()).getCheckedInDocuments(), true);
    }

    public void selectAllCheckedOutDocs() {
        m_elementTable.unselectAllRows();
        m_elementTable.setSelectionForRows(((DocMTableModel) m_elementTable.getTableModel()).getCheckedOutDocuments(), true);
    }

    private void fetchWorkflowModels(String workspaceId) {
        desiredProfile = m_tableProfiles.getProfile("workflowsProfile");
        elementTableDecPanel.removeStyleName("searchTable");

        pageManager.setPageManagerBackend(new WorkflowModelBackend(workspaceId));
    }

    private void fetchDocMTemplates(String workspaceId) {
        desiredProfile = m_tableProfiles.getProfile("templatesProfile");
        pageManager.setPageManagerBackend(new DocMTemplateBackend(workspaceId));
    }

    private void fetchCheckedOutDocMs(String workspaceId) {
        desiredProfile = m_tableProfiles.getProfile("documentsProfile");
        elementTableDecPanel.removeStyleName("searchTable");

        pageManager.setPageManagerBackend(new DocMCheckedOutBackend(m_login, workspaceId));
    }

    private void fetchDocMsByFolder(String completePath) {
        desiredProfile = m_tableProfiles.getProfile("documentsProfile");
        elementTableDecPanel.removeStyleName("searchTable");

        pageManager.setPageManagerBackend(new DocMFolderBackend(completePath, m_login));
    }

    private void fetchDocMsByTag(String workspaceId, String label) {
        desiredProfile = m_tableProfiles.getProfile("documentsProfile");
        pageManager.setPageManagerBackend(new DocMTagBackend(m_login, label, workspaceId));
    }

    private void fetchWorkflowModels() {
        AsyncCallback<WorkflowModelDTO[]> callback = new AsyncCallback<WorkflowModelDTO[]>() {

            public void onSuccess(WorkflowModelDTO[] wks) {
                m_createDocMPanel.setWorkflowModels(wks);
                m_createVersionPanel.setWorkflowModels(wks);
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getWorkflowModels(getWorkspaceId(), callback);
    }

   
    private void fetchTemplates() {
        AsyncCallback<DocumentMasterTemplateDTO[]> callback = new AsyncCallback<DocumentMasterTemplateDTO[]>() {

            public void onSuccess(DocumentMasterTemplateDTO[] templates) {
                m_createDocMPanel.setTemplates(templates);

            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getDocMTemplates(getWorkspaceId(), callback);
    }

    private void fetchDocMId(String workspaceId, String templateId) {
        final String tId = templateId ;
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onSuccess(String docMId) {
                if (docMId != null && !docMId.equals("")){
                    m_createDocMPanel.setDocMId(docMId);
                }else{
                   AsyncCallback<DocumentMasterTemplateDTO> cb = new AsyncCallback<DocumentMasterTemplateDTO>() {

                        public void onFailure(Throwable caught) {
                            String s ;
                            s = "bnnj" ;
                        }

                        public void onSuccess(DocumentMasterTemplateDTO result) {
                            m_createDocMPanel.setDocMIdMask(result.getMask());
                        }
                    };
                    ServiceLocator.getInstance().getExplorerService().getDocMTemplate(m_workspaceId, tId, cb);
                }                
            }

            public void onFailure(Throwable caught) {
            }
        };
        if (templateId != null) {
            ServiceLocator.getInstance().getExplorerService().generateId(workspaceId, templateId, callback);
        }else{
            m_createDocMPanel.setDocMIdEnabled(true) ;
        }
    }

    public void onResize(ResizeEvent event) {
        if (m_wfEditor.isVisible() && m_wfEditor.isAttached()) {
            m_wfEditor.setVisible(false);
            m_wfEditor.setWidth(inputPanel.getOffsetWidth() - 10);
            m_wfEditor.setVisible(true);
        }
    }

    public void onPageChanged(PageManagerEvent event) {

        if (pageManager.getCurrentBackend() instanceof DocMSearchBackend) {
            m_elementTable.setModelSearch(pageManager.getCurrentModel(), desiredProfile);
            showTablePanel(false);
        } else {
            m_elementTable.setModel(pageManager.getCurrentModel(), desiredProfile);
            showTablePanel();
        }
    }

    public String getLogin() {
        return m_login;
    }

    /**
     * does not update the view !
     * @param doc
     */
    public void setCurrentDocM(DocumentMasterDTO doc){
        m_lastOpenedDocM = doc;
    }
}
