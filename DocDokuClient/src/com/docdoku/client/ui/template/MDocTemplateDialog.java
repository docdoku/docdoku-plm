package com.docdoku.client.ui.template;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.EditFilesPanel;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.WorkflowModel;
import com.docdoku.core.entities.Folder;
import java.io.File;
import java.util.Collection;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class MDocTemplateDialog extends JDialog {
    
    public MDocTemplateDialog(Frame pOwner, String pTitle) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
    }
    
    public MDocTemplateDialog(Dialog pOwner, String pTitle) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
    }
        
    protected void createLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        Box centerPanel = new Box(BoxLayout.Y_AXIS);
        centerPanel.add(getMDocTemplatePanel());
        centerPanel.add(Box.createVerticalStrut(15));
        JTabbedPane tabs=new JTabbedPane(JTabbedPane.TOP);
        Image img =
                Toolkit.getDefaultToolkit().getImage(MDocTemplateDialog.class.getResource(
                "/com/docdoku/client/resources/icons/paperclip.png"));
        tabs.addTab(I18N.BUNDLE.getString("Files_border"),new ImageIcon(img),getFilesPanel());
        
        img =
                Toolkit.getDefaultToolkit().getImage(MDocTemplateDialog.class.getResource(
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
    protected abstract JPanel getMDocTemplatePanel();
}
