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

package com.docdoku.client.ui.help;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

/**
 *
 * @author Florent GARIN
 */
public class ShortcutsPanel extends JPanel{

    private ImageIcon mWorkflowShortcut;
    private ImageIcon mMDocShortcut;
    private ImageIcon mTemplateShortcut;
    private ImageIcon mFolderShortcut;

    public ShortcutsPanel(){
        Image img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/branch_element_new_big.png"));
        mWorkflowShortcut = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/document_new_big.png"));
        mMDocShortcut = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/document_notebook_big.png"));
        mTemplateShortcut = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(ShortcutsPanel.class.getResource("/com/docdoku/client/resources/icons/folder_new_big.png"));
        mFolderShortcut = new ImageIcon(img);

        createLayout();
    }

    private void createLayout() {
        setLayout(new GridLayout(2,2));

        add(new JButton(mMDocShortcut));
        add(new JButton(mFolderShortcut));
        add(new JButton(mTemplateShortcut));
        add(new JButton(mWorkflowShortcut));
    }
}
