/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.client.actions;

import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.client.localization.I18N;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author flo
 */
public class EditFolderActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        try {
            String newName = e.getActionCommand();
            TreePath path = (TreePath) e.getSource();
            String completePath = ((FolderTreeNode) path.getLastPathComponent()).getCompletePath();
            MainController.getInstance().renameFolder(completePath, newName);
        } catch (Exception pEx) {
            String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
            JOptionPane.showMessageDialog(null,
                    message, I18N.BUNDLE.getString("Error_title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
