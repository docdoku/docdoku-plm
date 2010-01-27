package com.docdoku.client.ui.template;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.entities.InstanceAttributeTemplate;
import com.docdoku.core.entities.MasterDocumentTemplate;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.docdoku.client.localization.I18N;

public class EditAttributeTemplatesPanel extends JPanel implements ActionListener {

    private JScrollPane mAttributesScrollPane;
    private JList mAttributesList;
    private JButton mAddButton;
    private JButton mRemoveButton;
    private DefaultListModel mAttributesListModel;
    private MasterDocumentTemplate mEditedMDocTemplate;
    private ActionListener mAddAttributeAction;

    public EditAttributeTemplatesPanel(MasterDocumentTemplate pEditedMDocTemplate,ActionListener pAddAttributeAction) {
        this(pAddAttributeAction);
        mEditedMDocTemplate = pEditedMDocTemplate;
        for(InstanceAttributeTemplate attr:mEditedMDocTemplate.getAttributeTemplates()) {
            mAttributesListModel.addElement(attr);
        }
    }
    
    public EditAttributeTemplatesPanel(ActionListener pAddAttributeAction) {
        mAttributesListModel = new DefaultListModel();
        mAddAttributeAction =pAddAttributeAction;
        Image img =
                Toolkit.getDefaultToolkit().getImage(EditAttributeTemplatesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_plus.png"));
        ImageIcon addIcon = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(EditAttributeTemplatesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_minus.png"));
        ImageIcon removeIcon = new ImageIcon(img);

        mAddButton = new JButton(I18N.BUNDLE.getString("AddAttribute_button"), addIcon);
        mRemoveButton = new JButton(I18N.BUNDLE.getString("RemoveAttribute_button"), removeIcon);
        
        mAttributesScrollPane = new JScrollPane();
        mAttributesList = new JList(mAttributesListModel);
        createLayout();
        createListener();
    }



    public DefaultListModel getAttributesListModel() {
        return mAttributesListModel;
    }

    private void createLayout() {
        mAddButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setEnabled(false);
        mAttributesScrollPane.getViewport().add(mAttributesList);
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
        add(mAttributesScrollPane, constraints);

        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridx = 1;
        add(mAddButton, constraints);

        constraints.gridy = 1;
        add(mRemoveButton, constraints);
    }

    public MasterDocumentTemplate getEditedMDocTemplate() {
        return mEditedMDocTemplate;
    }



    private void createListener() {
        mAttributesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent pE) {
                mRemoveButton.setEnabled(!mAttributesList.isSelectionEmpty());
            }
        });
        mAddButton.addActionListener(this);
        mRemoveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                Object[] selectedObjects = mAttributesList.getSelectedValues();
                for (int i = 0; i < selectedObjects.length; i++) {
                    mAttributesListModel.removeElement(selectedObjects[i]);
                }
                mRemoveButton.setEnabled(false);
            }
        });
    }

    public void actionPerformed(ActionEvent pAE) {
        mAddAttributeAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
