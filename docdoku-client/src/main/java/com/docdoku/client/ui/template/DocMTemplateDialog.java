/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.client.ui.template;

import com.docdoku.client.localization.I18N;

import javax.swing.*;
import java.awt.*;

public abstract class DocMTemplateDialog extends JDialog {
    
    public DocMTemplateDialog(Frame pOwner, String pTitle) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
    }
    
    public DocMTemplateDialog(Dialog pOwner, String pTitle) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
    }
        
    protected void createLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        Box centerPanel = new Box(BoxLayout.Y_AXIS);
        centerPanel.add(getDocMTemplatePanel());
        centerPanel.add(Box.createVerticalStrut(15));
        JTabbedPane tabs=new JTabbedPane(JTabbedPane.TOP);
        Image img =
                Toolkit.getDefaultToolkit().getImage(DocMTemplateDialog.class.getResource(
                "/com/docdoku/client/resources/icons/paperclip.png"));
        tabs.addTab(I18N.BUNDLE.getString("Files_border"),new ImageIcon(img),getFilesPanel());
        
        img =
                Toolkit.getDefaultToolkit().getImage(DocMTemplateDialog.class.getResource(
                "/com/docdoku/client/resources/icons/document_info.png"));
        tabs.addTab(I18N.BUNDLE.getString("Attributes_border"),new ImageIcon(img),getAttributesPanel());
        centerPanel.add(tabs);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(getSouthPanel(), BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }
    
    protected abstract JPanel getSouthPanel();
    protected abstract JPanel getFilesPanel();
    protected abstract JPanel getAttributesPanel();
    protected abstract JPanel getDocMTemplatePanel();
}
