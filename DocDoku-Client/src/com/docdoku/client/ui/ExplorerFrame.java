/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.client.ui;

import com.docdoku.client.backbone.ElementSelectedEvent;
import com.docdoku.client.backbone.ElementSelectedListener;
import com.docdoku.client.backbone.HasElementSelectedListeners;
import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.data.FolderBasedElementsTableModel;
import com.docdoku.client.data.TagRootTreeNode;
import com.docdoku.client.data.TagTreeNode;
import com.docdoku.client.data.TemplateTreeNode;
import com.docdoku.client.data.WorkflowModelTreeNode;
import com.docdoku.client.ui.common.ElementsScrollPane;
import com.docdoku.client.ui.help.ShortcutsPanel;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.WorkflowModel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class ExplorerFrame extends JFrame implements HasElementSelectedListeners{
    private static Set<ExplorerFrame> mInstances = new HashSet<ExplorerFrame>();
    
    private ExplorerToolBar mToolBar;
    private JSplitPane mSplitPane;
    private JScrollPane mLeftScrollPane;
    private ElementsScrollPane mRightScrollPane;
    private JTree mFolderTree;
    private ExplorerMenu mMenuBar;
    private ExplorerPopupMenu mPopupMenu;
    private JLabel mStatusLabel;
    private JButton mGuideBtn;
    private FolderTreeNode mSelectedFolder;
    private MasterDocument mSelectedMDoc;
    private WorkflowModel mSelectedWorkflowModel;
    private MasterDocumentTemplate mSelectedMDocTemplate;
    private ActionFactory mActionFactory;
    private TransferHandler mTransferHandler;
    private ActionListener mEditFolderListener;

    private Container mRegularPanel;
    private ShortcutsPanel mShortcutsPanel;

    private List<ElementSelectedListener> mElementSelectedListener;
    
    
    public ExplorerFrame(TransferHandler pTransferHandler, ActionListener pEditFolderListener) {
        this(null,pTransferHandler, pEditFolderListener);
    }
    
    private ExplorerFrame(Component pLocationRelative, TransferHandler pTransferHandler, ActionListener pEditFolderListener) {
        super(MainModel.getInstance().getLogin() + "@"
                + MainModel.getInstance().getWorkspace());
        setLocationByPlatform(true);
        mTransferHandler=pTransferHandler;
        mEditFolderListener=pEditFolderListener;
        mStatusLabel = new JLabel("DocDoku");
        mGuideBtn = new JButton();
        mToolBar = new ExplorerToolBar(mStatusLabel);
        mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mLeftScrollPane = new JScrollPane();
        mRightScrollPane = new ElementsScrollPane(MainModel.getInstance()
        .createElementsTableModel(),pTransferHandler);
        MainModel.getInstance().getElementsTreeModel().setPathChangedAction(pEditFolderListener);
        mFolderTree = new JTree(MainModel.getInstance().getElementsTreeModel());
        mFolderTree.setTransferHandler(pTransferHandler);
        mFolderTree.setEditable(true);
        mMenuBar = new ExplorerMenu(mStatusLabel);
        mPopupMenu = new ExplorerPopupMenu();
        mElementSelectedListener = new LinkedList<ElementSelectedListener>();
        mRegularPanel=new JPanel(new BorderLayout());
        mShortcutsPanel=new ShortcutsPanel(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegularPanel();
            }
        }, ExplorerFrame.this);
        createLayout();
        createListener();
        mInstances.add(this);
    }

    private void createLayout() {
        Image img = Toolkit
                .getDefaultToolkit()
                .getImage(
                ExplorerFrame.class
                .getResource("/com/docdoku/client/resources/icons/logo.png"));
        this.setIconImage(img);
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        main.add(mToolBar, BorderLayout.NORTH);
        main.add(mSplitPane, BorderLayout.CENTER);
        mRegularPanel.add(main, BorderLayout.CENTER);
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(mGuideBtn);
        statusPanel.add(mStatusLabel);
        mRegularPanel.add(statusPanel, BorderLayout.SOUTH);
        
        setJMenuBar(mMenuBar);
        mFolderTree.setComponentPopupMenu(mPopupMenu);
        mRightScrollPane.getElementsTable().setComponentPopupMenu(mPopupMenu);
        mLeftScrollPane.getViewport().add(mFolderTree);
        
        mSplitPane.add(mLeftScrollPane, JSplitPane.LEFT);
        mSplitPane.add(mRightScrollPane, JSplitPane.RIGHT);
        
        mSplitPane.setOneTouchExpandable(true);
        
        mFolderTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        mFolderTree.setCellRenderer(new DefaultTreeCellRenderer() {
            private ImageIcon homeIcon;
            
            {
                Image img = Toolkit
                        .getDefaultToolkit()
                        .getImage(
                        ExplorerFrame.class
                        .getResource("/com/docdoku/client/resources/icons/home.png"));
                homeIcon = new ImageIcon(img);
            }
            
            private ImageIcon rootIcon;
            
            {
                Image img = Toolkit
                        .getDefaultToolkit()
                        .getImage(
                        ExplorerFrame.class
                        .getResource("/com/docdoku/client/resources/icons/safe.png"));
                rootIcon = new ImageIcon(img);
            }
            
            private ImageIcon tagsIcon;
            
            {
                Image img = Toolkit
                        .getDefaultToolkit()
                        .getImage(
                        ExplorerFrame.class
                        .getResource("/com/docdoku/client/resources/icons/tags_blue.png"));
                tagsIcon = new ImageIcon(img);
            }

            private ImageIcon tagIcon;
            
            {
                Image img = Toolkit
                        .getDefaultToolkit()
                        .getImage(
                        ExplorerFrame.class
                        .getResource("/com/docdoku/client/resources/icons/tag_blue.png"));
                tagIcon = new ImageIcon(img);
            }
            
            private ImageIcon templatesIcon;
            
            {
                Image img = Toolkit
                        .getDefaultToolkit()
                        .getImage(
                        ExplorerFrame.class
                        .getResource("/com/docdoku/client/resources/icons/template_folder.png"));
                templatesIcon = new ImageIcon(img);
            }
            
            private ImageIcon workflowsIcon;
            
            {
                Image img = Toolkit
                        .getDefaultToolkit()
                        .getImage(
                        ExplorerFrame.class
                        .getResource("/com/docdoku/client/resources/icons/branch_folder.png"));
                workflowsIcon = new ImageIcon(img);
            }
            
            @Override
            public Component getTreeCellRendererComponent(JTree pTree,
                    Object pValue, boolean pSel, boolean pExpanded,
                    boolean pLeaf, int pRow, boolean pHasFocus) {
                super.getTreeCellRendererComponent(pTree, pValue, pSel,
                        pExpanded, pLeaf, pRow, pHasFocus);
                switch (pRow) {
                    case 0:
                        setIcon(rootIcon);
                        break;
                    case 1:
                        setIcon(homeIcon);
                        break;
                    default:
                        if(pValue instanceof TagRootTreeNode)
                            setIcon(tagsIcon);
                        else if(pValue instanceof TagTreeNode)
                            setIcon(tagIcon);
                        else if(pValue instanceof TemplateTreeNode)
                            setIcon(templatesIcon);
                        else if(pValue instanceof WorkflowModelTreeNode)
                            setIcon(workflowsIcon);
                }
                return this;
            }
        });
        showRegularPanel();
    }

    public void showRegularPanel(){
        setContentPane(mRegularPanel);
    }

    public void showShortcutsPanel(){
        mFolderTree.setSelectionPath(null);
        setContentPane(mShortcutsPanel);
    }

    private void createListener() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        mFolderTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent pTSE) {
                TreePath path = pTSE.getPath();
                FolderTreeNode node = (FolderTreeNode) path
                        .getLastPathComponent();
                if(pTSE.isAddedPath()){
                    ((FolderBasedElementsTableModel) mRightScrollPane.getModel())
                    .setFolder(node);
                    mSelectedFolder = node;
                    fireElementSelected(new ElementSelectedEvent(ExplorerFrame.this,node));
                }else{
                    mSelectedFolder=null;
                    fireElementSelected(new ElementSelectedEvent(ExplorerFrame.this,(FolderTreeNode)null));
                }
            }
        });
        mRightScrollPane.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent pE) {
                Object element = mRightScrollPane.getSelectedElement();
                if (element instanceof MasterDocument) {
                    mSelectedMDoc = (MasterDocument) element;
                    mSelectedWorkflowModel = null;
                    mSelectedMDocTemplate = null;
                    fireElementSelected(new ElementSelectedEvent(ExplorerFrame.this,mSelectedMDoc));
                } else if (element instanceof WorkflowModel) {
                    mSelectedWorkflowModel = (WorkflowModel) element;
                    mSelectedMDoc = null;
                    mSelectedMDocTemplate = null;
                    fireElementSelected(new ElementSelectedEvent(ExplorerFrame.this,mSelectedWorkflowModel));
                }else if (element instanceof MasterDocumentTemplate) {
                    mSelectedMDocTemplate = (MasterDocumentTemplate) element;
                    mSelectedMDoc = null;
                    mSelectedWorkflowModel = null;
                    fireElementSelected(new ElementSelectedEvent(ExplorerFrame.this,mSelectedMDocTemplate));
                } else {
                    if(mSelectedMDoc!=null)
                        fireElementSelected(new ElementSelectedEvent(ExplorerFrame.this,(MasterDocument)null));
                    else if(mSelectedWorkflowModel!=null)
                        fireElementSelected(new ElementSelectedEvent(ExplorerFrame.this,(WorkflowModel)null));
                    else if(mSelectedMDocTemplate!=null)
                        fireElementSelected(new ElementSelectedEvent(ExplorerFrame.this,(MasterDocumentTemplate)null));
                    
                    mSelectedMDoc = null;
                    mSelectedWorkflowModel = null;
                    mSelectedMDocTemplate = null;
                    
                }
            }
        });
        mRightScrollPane.getElementsTable().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()>1){
                    Action editAction = mActionFactory.getEditElementAction();
                    if(editAction.isEnabled())
                        editAction.actionPerformed(new ActionEvent(this, 0, null));
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
            }
            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });
    }
    
    public FolderTreeNode getSelectedFolder() {
        return mSelectedFolder;
    }
    
    public void unselectElement() {
        mRightScrollPane.unselectElement();
    }
    
    public MasterDocument getSelectedMDoc() {
        return mSelectedMDoc;
    }
    
    public MasterDocumentTemplate getSelectedMDocTemplate() {
        return mSelectedMDocTemplate;
    }
    
    public WorkflowModel getSelectedWorkflowModel() {
        return mSelectedWorkflowModel;
    }
    
    public void setActions(ActionFactory pActionFactory) {
        mActionFactory = pActionFactory;
        mToolBar.setActions(pActionFactory);
        mMenuBar.setActions(pActionFactory);
        mPopupMenu.setActions(pActionFactory);
        mShortcutsPanel.setActions(pActionFactory);
        mGuideBtn.setAction(pActionFactory.getDisplayShortcutsAction());
    }
    
    public void showMDoc(MasterDocument pMDoc){
        FolderTreeNode[] path=MainModel.getInstance().getElementsTreeModel().getPath(pMDoc.getLocation().getCompletePath());
        TreePath treePath=new TreePath(path);
        mFolderTree.expandPath(treePath);
        mFolderTree.setSelectionPath(treePath);
        mRightScrollPane.selectElement(pMDoc);
    }
    
    public void duplicate() {
        ExplorerFrame explorerFrame = new ExplorerFrame(this, mTransferHandler, mEditFolderListener);
        ActionFactory connector = mActionFactory.clone(explorerFrame);
        explorerFrame.addElementSelectedListener(connector);
        explorerFrame.setActions(connector);
        explorerFrame.pack();
        explorerFrame.setVisible(true);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        mInstances.remove(this);
        if (numberOfInstances() == 0)
            System.exit(0);
    }
    
    private int numberOfInstances() {
        return mInstances.size();
    }
    
    public static void unselectElementInAllFrame() {
        for (ExplorerFrame explorerFrame : mInstances) {
            explorerFrame.unselectElement();
        }
    }
    
    @Override
    public void addElementSelectedListener(ElementSelectedListener pListener) {
        mElementSelectedListener.add(pListener);
    }
    
    @Override
    public void removeElementSelectedListener(ElementSelectedListener pListener) {
        mElementSelectedListener.remove(pListener);
    }
    
    protected void fireElementSelected(ElementSelectedEvent event) {

        for (ElementSelectedListener listener : new ArrayList<ElementSelectedListener>(mElementSelectedListener)) {
            listener.elementSelected(event);
        }
    }
    
}
