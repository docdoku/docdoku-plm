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
package com.docdoku.client.ui.common;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.FileHolder;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import com.docdoku.client.localization.I18N;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.filechooser.FileSystemView;

public class EditFilesPanel extends JPanel implements ActionListener {

    private JScrollPane mFilesScrollPane;
    private JList mFilesList;
    private JButton mAddButton;
    private JButton mAcquireButton;
    private JButton mRemoveButton;
    private JButton mEditButton;
    private JFileChooser mFileChooser;
    private DefaultListModel mFilesListModel;
    private Set<File> mFilesToAdd;
    private Set<BinaryResource> mFilesToRemove;
    private Map<BinaryResource, Long> mFilesToUpdate;
    private FileHolder mFileHolder;
    private ActionListener mEditAction;
    private ActionListener mScanAction;

    public EditFilesPanel(ActionListener pEditAction, ActionListener pScanAction) {
        mEditAction = pEditAction;
        mScanAction = pScanAction;
        mFileChooser = new JFileChooser();
        mFileChooser.setMultiSelectionEnabled(true);
        mFilesListModel = new DefaultListModel();

        Image img =
                Toolkit.getDefaultToolkit().getImage(EditFilesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_plus.png"));
        ImageIcon addIcon = new ImageIcon(img);

        img =
                Toolkit.getDefaultToolkit().getImage(EditFilesPanel.class.getResource("/com/docdoku/client/resources/icons/scanner.png"));
        ImageIcon scanIcon = new ImageIcon(img);


        img = Toolkit.getDefaultToolkit().getImage(EditFilesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_minus.png"));
        ImageIcon removeIcon = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(EditFilesPanel.class.getResource("/com/docdoku/client/resources/icons/gears.png"));
        ImageIcon editIcon = new ImageIcon(img);

        mAddButton = new JButton(I18N.BUNDLE.getString("AddFile_button"), addIcon);
        mAcquireButton = new JButton(I18N.BUNDLE.getString("Acquire_button"), scanIcon);
        mRemoveButton = new JButton(I18N.BUNDLE.getString("RemoveFile_button"), removeIcon);

        mEditButton = new JButton(I18N.BUNDLE.getString("EditFile_button"), editIcon);

        mFilesScrollPane = new JScrollPane();
        mFilesList = new JList(mFilesListModel);
        mFilesList.setDragEnabled(true);
        mFilesList.setDropMode(DropMode.ON);
        mFilesList.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                Transferable t = support.getTransferable();
                try {
                    java.util.List localFiles =
                            (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);

                    for (Object localFile : localFiles) {
                        addFile((File) localFile);
                    }
                } catch (UnsupportedFlavorException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }

                return true;
            }
        });
        mFilesToAdd = new HashSet<File>();
        mFilesToRemove = new HashSet<BinaryResource>();
        mFilesToUpdate = new HashMap<BinaryResource, Long>();
        createLayout();
        createListener();
    }

    public EditFilesPanel(FileHolder pFileHolder, ActionListener pEditAction, ActionListener pScanAction) {
        this(pEditAction, pScanAction);
        mFileHolder = pFileHolder;
        for (BinaryResource file : pFileHolder.getAttachedFiles()) {
            mFilesListModel.addElement(file);
        }
    }

    public void addFile(File fileToAdd) {
        mFilesToAdd.add(fileToAdd);
        mFilesListModel.addElement(fileToAdd);
    }

    public Object getSelectedFile() {
        return mFilesList.getSelectedValue();
    }

    public FileHolder getFileHolder() {
        return mFileHolder;
    }

    private void createLayout() {
        mFilesList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    setIcon(FileSystemView.getFileSystemView().getSystemIcon((File) value));
                }
                return this;
            }
        });
        mAddButton.setHorizontalAlignment(SwingConstants.LEFT);
        mAcquireButton.setHorizontalAlignment(SwingConstants.LEFT);
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

        constraints.gridheight = 5;
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
        add(mAcquireButton, constraints);

        constraints.gridy = 2;
        add(mRemoveButton, constraints);

        constraints.gridy = 3;
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
        mAcquireButton.addActionListener(this);
        mFilesList.addListSelectionListener(new ListSelectionListener() {

            @Override
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

            @Override
            public void actionPerformed(ActionEvent pAE) {
                int state = mFileChooser.showOpenDialog(mAddButton);
                if (state == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = mFileChooser.getSelectedFiles();
                    for (int i = 0; i < selectedFiles.length; i++) {
                        addFile(selectedFiles[i]);
                    }
                }
            }
        });
        mRemoveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent pAE) {
                Object[] selectedObjects = mFilesList.getSelectedValues();
                for (int i = 0; i < selectedObjects.length; i++) {
                    if (selectedObjects[i] instanceof BinaryResource) {
                        mFilesToRemove.add((BinaryResource) selectedObjects[i]);
                        mFilesToUpdate.remove((BinaryResource) selectedObjects[i]);
                    } else {
                        mFilesToAdd.remove(selectedObjects[i]);
                    }
                    mFilesListModel.removeElement(selectedObjects[i]);
                }
                mRemoveButton.setEnabled(false);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        Object button = pAE.getSource();
        if (button == mEditButton) {
            mEditAction.actionPerformed(new ActionEvent(this, 0, null));
        } else if (button == mAcquireButton) {
            mScanAction.actionPerformed(new ActionEvent(this, 0, null));
        }
    }
}
