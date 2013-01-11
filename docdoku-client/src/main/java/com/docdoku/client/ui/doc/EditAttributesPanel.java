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

package com.docdoku.client.ui.doc;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceBooleanAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;
import com.docdoku.core.meta.InstanceNumberAttribute;
import com.docdoku.core.meta.InstanceTextAttribute;
import com.docdoku.core.meta.InstanceURLAttribute;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXDatePicker;

/**
 *
 * @author Florent GARIN
 */
public class EditAttributesPanel extends JPanel implements ActionListener{

    private JPanel mAttributesPanel;
    private JScrollPane mAttributesScrollPane;
    private JButton mAddButton;
    private JButton mRemoveButton;
    private Set<String> mSelectedAttributes = new HashSet<String>();
    
    private Map<String, JComponent> mAttributeComponents = new HashMap<String, JComponent>();
    private Map<String, InstanceAttribute> mAttributesWorkingCopy = new HashMap<String, InstanceAttribute>();
    private Map<String, InstanceAttribute> mAttributes;

    private ActionListener mAddAttributeAction;


    public EditAttributesPanel(DocumentIteration pDoc, ActionListener pAddAttributeAction) {
        mAttributes = pDoc.getInstanceAttributes();
        mAttributesWorkingCopy.putAll(mAttributes);
        mAddAttributeAction =pAddAttributeAction;
        createAttributesComponents();
        Image img =
                Toolkit.getDefaultToolkit().getImage(EditAttributesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_plus.png"));
        ImageIcon addIcon = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(EditAttributesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_minus.png"));
        ImageIcon removeIcon = new ImageIcon(img);
        mAddButton = new JButton(I18N.BUNDLE.getString("AddAttribute_button"), addIcon);
        mRemoveButton = new JButton(I18N.BUNDLE.getString("RemoveAttribute_button"), removeIcon);

        mAttributesScrollPane = new JScrollPane();
        mAttributesPanel = new JPanel();
        createLayout();
        createListener();
    }

    public Map<String, InstanceAttribute> getAttributes() {
        mAttributes.clear();
        mAttributes.putAll(mAttributesWorkingCopy);

        for (InstanceAttribute attr : mAttributes.values()) {
            if (attr instanceof InstanceTextAttribute) {
                JTextField componentText = (JTextField) mAttributeComponents.get(attr.getName());
                ((InstanceTextAttribute) attr).setTextValue(componentText.getText());
            } else if (attr instanceof InstanceURLAttribute) {
                JTextField componentUrl = (JTextField) mAttributeComponents.get(attr.getName());
                ((InstanceURLAttribute) attr).setUrlValue(componentUrl.getText());
            } else if (attr instanceof InstanceNumberAttribute) {
                JFormattedTextField componentNumber = (JFormattedTextField) mAttributeComponents.get(attr.getName());
                float floatValue = 0;
                try {
                    floatValue = NumberFormat.getInstance().parse(componentNumber.getText()).floatValue();
                } catch (ParseException pEx) {
                    System.err.println(pEx.getMessage());
                }
                ((InstanceNumberAttribute) attr).setNumberValue(floatValue);
            } else if (attr instanceof InstanceBooleanAttribute) {
                JCheckBox componentBoolean = (JCheckBox) mAttributeComponents.get(attr.getName());
                ((InstanceBooleanAttribute) attr).setBooleanValue(componentBoolean.isSelected());
            } else if (attr instanceof InstanceDateAttribute) {
                JXDatePicker componentDate = (JXDatePicker) mAttributeComponents.get(attr.getName());
                ((InstanceDateAttribute) attr).setDateValue(componentDate.getDate());
            }
        }
        return mAttributes;
    }

    public void addAttributePanel(InstanceAttribute attr) {
        createAttributeComponents(attr);
        mAttributesWorkingCopy.put(attr.getName(), attr);
        mAttributesPanel.removeAll();
        layoutAttributesPanel();

        mAttributesPanel.invalidate();
        mAttributesPanel.repaint();
        
    }

    private void createLayout() {
        mAttributesPanel.setLayout(new GridBagLayout());
        layoutAttributesPanel();
        mAddButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setEnabled(false);
        mAttributesScrollPane.getViewport().add(mAttributesPanel);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = GUIConstants.INSETS;
        constraints.gridwidth = 1;

        constraints.gridheight = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.BOTH;

        add(mAttributesScrollPane, constraints);

        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.gridheight = 1;
        constraints.gridx = 1;
        add(mAddButton, constraints);

        constraints.gridy = 1;
        add(mRemoveButton, constraints);
    }

    private void createListener() {
        mAddButton.addActionListener(this);
        mRemoveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent pAE) {
                for (String selectedAttr : mSelectedAttributes) {
                    mAttributesWorkingCopy.remove(selectedAttr);
                    mAttributeComponents.remove(selectedAttr);
                }
                mSelectedAttributes.clear();
                mAttributesPanel.removeAll();
                layoutAttributesPanel();
                mRemoveButton.setEnabled(false);
                mAttributesPanel.invalidate();
                mAttributesPanel.repaint();
            }
        });
    }

    private JComponent createAttributeComponents(InstanceAttribute attr) {
        if (attr instanceof InstanceTextAttribute) {
            String text = ((InstanceTextAttribute) attr).getTextValue();
            if (text == null) {
                text = "";
            }
            return mAttributeComponents.put(attr.getName(), new JTextField(new MaxLengthDocument(255), text, 10));
        } else if (attr instanceof InstanceURLAttribute) {
            String url = ((InstanceURLAttribute) attr).getUrlValue();
            if (url == null) {
                url = "";
            }
            return mAttributeComponents.put(attr.getName(), new JTextField(new MaxLengthDocument(255), url, 10));
        } else if (attr instanceof InstanceNumberAttribute) {
            JFormattedTextField componentNumber = new JFormattedTextField(NumberFormat.getInstance());
            componentNumber.setValue(attr.getValue());
            return mAttributeComponents.put(attr.getName(), componentNumber);
        } else if (attr instanceof InstanceBooleanAttribute) {
            JCheckBox componentBoolean = new JCheckBox();
            componentBoolean.setSelected(((InstanceBooleanAttribute) attr).isBooleanValue());
            return mAttributeComponents.put(attr.getName(), componentBoolean);
        } else if (attr instanceof InstanceDateAttribute) {
            Date date = ((InstanceDateAttribute) attr).getDateValue();
            JXDatePicker datePicker = new JXDatePicker();
            if (date != null) {
                datePicker.setDate(date);
            }
            return mAttributeComponents.put(attr.getName(), datePicker);
        }
        return null;
    }

    private void createAttributesComponents() {
        for (InstanceAttribute attr : mAttributes.values()) {
            createAttributeComponents(attr);
        }
    }

    private void layoutAttributesPanel() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.weighty = 0;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        for (final Map.Entry<String, JComponent> compEntry : mAttributeComponents.entrySet()) {
            final JCheckBox attrBox = new JCheckBox();
            attrBox.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    if (attrBox.isSelected()) {
                        mSelectedAttributes.add(compEntry.getKey());
                    } else {
                        mSelectedAttributes.remove(compEntry.getKey());
                    }

                    mRemoveButton.setEnabled(mSelectedAttributes.size() > 0);
                }
            });
            JLabel label = new JLabel(compEntry.getKey() + " :");
            label.setLabelFor(compEntry.getValue());
            constraints.gridx = 0;
            mAttributesPanel.add(attrBox, constraints);
            constraints.gridx = 1;
            mAttributesPanel.add(label, constraints);
            constraints.gridy++;
        }

        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        for (JComponent comp : mAttributeComponents.values()) {
            mAttributesPanel.add(comp, constraints);
            constraints.gridy = GridBagConstraints.RELATIVE;
        }

        constraints.gridx = 0;
        constraints.weighty = 1;
        constraints.gridwidth = 3;
        mAttributesPanel.add(new JPanel(), constraints);
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        mAddAttributeAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
