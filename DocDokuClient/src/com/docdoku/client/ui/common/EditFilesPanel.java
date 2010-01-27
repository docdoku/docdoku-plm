package com.docdoku.client.ui.common;

import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.FileHolder;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import com.docdoku.client.localization.I18N;
import javax.swing.filechooser.FileSystemView;

public class EditFilesPanel extends JPanel implements ActionListener {

    private JScrollPane mFilesScrollPane;
    private JList mFilesList;
    private JButton mAddButton;
    private JButton mRemoveButton;
    private JButton mEditButton;
    private JFileChooser mFileChooser;
    private DefaultListModel mFilesListModel;

    private Set<File> mFilesToAdd;
    private Set<BinaryResource> mFilesToRemove;
    private Map<BinaryResource, Long> mFilesToUpdate;
    private FileHolder mFileHolder;
    
    private ActionListener mEditAction;

    public EditFilesPanel(ActionListener pEditAction) {
        mEditAction=pEditAction;
        mFileChooser = new JFileChooser();
        mFileChooser.setMultiSelectionEnabled(true);
        mFilesListModel = new DefaultListModel();

        Image img =
                Toolkit.getDefaultToolkit().getImage(EditFilesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_plus.png"));
        ImageIcon addIcon = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(EditFilesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_minus.png"));
        ImageIcon removeIcon = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(EditFilesPanel.class.getResource("/com/docdoku/client/resources/icons/gears.png"));
        ImageIcon editIcon = new ImageIcon(img);

        mAddButton = new JButton(I18N.BUNDLE.getString("AddFile_button"), addIcon);
        mRemoveButton = new JButton(I18N.BUNDLE.getString("RemoveFile_button"), removeIcon);

        mEditButton = new JButton(I18N.BUNDLE.getString("EditFile_button"), editIcon);

        mFilesScrollPane = new JScrollPane();
        mFilesList = new JList(mFilesListModel);
        mFilesToAdd = new HashSet<File>();
        mFilesToRemove = new HashSet<BinaryResource>();
        mFilesToUpdate = new HashMap<BinaryResource, Long>();
        createLayout();
        createListener();
    }

    public EditFilesPanel(FileHolder pFileHolder, ActionListener pEditAction) {
        this(pEditAction);
        mFileHolder=pFileHolder;
        for (BinaryResource file : pFileHolder.getAttachedFiles()) {
            mFilesListModel.addElement(file);
        }
    }

    public Object getSelectedFile() {
        return mFilesList.getSelectedValue();
    }

    public FileHolder getFileHolder() {
        return mFileHolder;
    }


    
    private void createLayout() {
        mFilesList.setCellRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if(value instanceof File)
                    setIcon(FileSystemView.getFileSystemView().getSystemIcon((File)value));
                return this;
            }
        });
        mAddButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setHorizontalAlignment(SwingConstants.LEFT);
        mEditButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setEnabled(false);
        mEditButton.setEnabled(false);
        mFilesScrollPane.getViewport().add(mFilesList);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridwidth = 1;

        constraints.gridheight = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(mFilesScrollPane, constraints);

        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridx = 1;
        add(mAddButton, constraints);

        constraints.gridy = 1;
        add(mRemoveButton, constraints);

        constraints.gridy = 2;
        add(mEditButton, constraints);
    }

    public Collection<File> getFilesToAdd() {
        return mFilesToAdd;
    }

    public Collection<BinaryResource> getFilesToRemove() {
        return mFilesToRemove;
    }

    public Map<BinaryResource, Long> getFilesToUpdate() {
        return mFilesToUpdate;
    }

    private void createListener() {
        mEditButton.addActionListener(this);
        mFilesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent pE) {
                if (mFilesList.isSelectionEmpty()) {
                    mRemoveButton.setEnabled(false);
                    mEditButton.setEnabled(false);
                } else {
                    mRemoveButton.setEnabled(true);
                    mEditButton.setEnabled(true);
                }

            }
        });
        mAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                int state = mFileChooser.showOpenDialog(mAddButton);
                if (state == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = mFileChooser.getSelectedFiles();
                    for (int i = 0; i < selectedFiles.length; i++) {
                        mFilesToAdd.add(selectedFiles[i]);
                        mFilesListModel.addElement(selectedFiles[i]);
                    }
                }
            }
        });
        mRemoveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                Object[] selectedObjects = mFilesList.getSelectedValues();
                for (int i = 0; i < selectedObjects.length; i++) {
                    if (selectedObjects[i] instanceof BinaryResource){
                        mFilesToRemove.add((BinaryResource)selectedObjects[i]);
                        mFilesToUpdate.remove((BinaryResource)selectedObjects[i]);
                    }
                    else{
                        mFilesToAdd.remove(selectedObjects[i]);
                    }
                    mFilesListModel.removeElement(selectedObjects[i]);
                }
                mRemoveButton.setEnabled(false);
            }
        });
    }

    public void actionPerformed(ActionEvent pAE) {
        Object button = pAE.getSource();
        if (button==mEditButton)
            mEditAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
