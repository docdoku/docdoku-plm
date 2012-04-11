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

package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.data.ShortDateFormater;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.docdoku.gwt.client.ui.widget.input.NumberConstrainedTextBox;
import com.docdoku.gwt.client.ui.widget.input.EditableURL;
import com.docdoku.gwt.client.ui.widget.input.EditableLabel;
import com.docdoku.gwt.client.ui.widget.util.NotEmptyValidator;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import com.docdoku.server.rest.dto.InstanceAttributeTemplateDTO;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
public class InstanceAttributesPanel extends DataRoundedPanel {

    private FlexTable m_attributeList;
    private boolean m_editionMode;
    private Map<String, InstanceAttributeDTO> m_attrs;
    private Label m_addLink;
    private Label m_delLink;

    private final static int HEIGHT=200;
    
    public InstanceAttributesPanel() {
        super(ServiceLocator.getInstance().getExplorerI18NConstants().tabAttributes());
        setHeight(HEIGHT);
        ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

        m_attributeList = new FlexTable();

        m_addLink = new Label(i18n.btnAdd());
        m_delLink = new Label(i18n.btnRemove());

        m_addLink.setStyleName("normalLinkAction");
        m_delLink.setStyleName("normalLinkAction");
        
        HorizontalPanel widgetFormPanel = new HorizontalPanel();
        widgetFormPanel.setSpacing(5);
        widgetFormPanel.add(m_addLink);
        widgetFormPanel.add(m_delLink);
        inputPanel.setWidget(0, 0, widgetFormPanel);
        ScrollPanel scroll = new ScrollPanel(m_attributeList);
        scroll.setHeight("10em");
        inputPanel.setWidget(1, 0, scroll);

        

        m_addLink.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addAttribute();
            }
        });

        m_delLink.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                removeAttributes();
            }
        });


    }

    public void setAttributes(Map<String, InstanceAttributeDTO> attrs) {

        int i = 1;
        m_attrs = attrs;
        m_attributeList.clear();
        m_addLink.setVisible(m_editionMode);
        m_delLink.setVisible(m_editionMode);
        for (Map.Entry<String, InstanceAttributeDTO> attr : attrs.entrySet()) {
            CheckBox selectionBox = new CheckBox();
            selectionBox.setVisible(m_editionMode);
            m_attributeList.setWidget(i, 0, selectionBox);

            EditableLabel l = new EditableLabel();
            l.setStyleName("instanceAttributes");
            l.setText(attr.getKey());
            l.setEnabled(m_editionMode);

            m_attributeList.setWidget(i, 1, l);

            Widget attrWidget = null;
           /*
            if (attr.getValue() instanceof InstanceBooleanAttributeDTO) {
                CheckBox checkBox = new CheckBox();
                checkBox.setEnabled(m_editionMode);
                checkBox.setValue((Boolean) attr.getValue().getValue());
                attrWidget = checkBox;
            } else if (attr.getValue() instanceof InstanceDateAttributeDTO) {
                DateBox dateBox = new DateBox();
                dateBox.setFormat(new ShortDateFormater());
                dateBox.setEnabled(m_editionMode);
                dateBox.setValue((Date) attr.getValue().getValue());
                attrWidget = dateBox;
            } else if (attr.getValue() instanceof InstanceNumberAttributeDTO) {
                NumberConstrainedTextBox numberBox = new NumberConstrainedTextBox();
                numberBox.setText(attr.getValue().getValue() == null ? "" : attr.getValue().getValue().toString());
                numberBox.setEnabled(m_editionMode);
                attrWidget = numberBox;
            } else if (attr.getValue() instanceof InstanceURLAttributeDTO) {
                EditableURL urlEdit = new EditableURL();
                urlEdit.setText(attr.getValue() == null ? "" : attr.getValue().getValue().toString());
                urlEdit.setEnabled(m_editionMode);
                attrWidget = urlEdit;
            } else {
                TextBox textBox = new TextBox();
                textBox.setEnabled(m_editionMode);
                textBox.setText(attr.getValue().getValue() == null ? "" : attr.getValue().getValue().toString());
                attrWidget = textBox;
            }
            m_attributeList.setWidget(i, 2, attrWidget);
            i++;
            
            */
        }
    }

    public List<InstanceAttributeDTO> getAttributes() {
        List<InstanceAttributeDTO> result = new LinkedList<InstanceAttributeDTO>();
        for (int i = 1; i < m_attributeList.getRowCount(); i++) {
            TextBox name = (TextBox) m_attributeList.getWidget(i, 1);
            if (!name.getText().isEmpty()) {
                InstanceAttributeDTO attribute = null;
                Widget attrWidget = m_attributeList.getWidget(i, 2);
                /*
                if (attrWidget instanceof CheckBox) {
                    InstanceBooleanAttributeDTO tmp = new InstanceBooleanAttributeDTO() ;
                    tmp.setBooleanValue(((CheckBox) attrWidget).getValue());
                    attribute = tmp ;
                } else if (attrWidget instanceof DateBox) {
                    InstanceDateAttributeDTO tmp = new InstanceDateAttributeDTO() ;
                   tmp.setValue(((DateBox) attrWidget).getValue());
                   attribute = tmp ;
                } else if (attrWidget instanceof NumberConstrainedTextBox) {
                    InstanceNumberAttributeDTO tmp = new InstanceNumberAttributeDTO() ;
                    tmp.setValue(((NumberConstrainedTextBox) attrWidget).getText());
                    attribute = tmp ;
                } else if (attrWidget instanceof EditableURL) {
                    InstanceURLAttributeDTO tmp = new InstanceURLAttributeDTO() ;
                    tmp.setValue(((EditableURL) attrWidget).getText());
                    attribute = tmp ;
                } else if (attrWidget instanceof TextBox) {
                    InstanceTextAttributeDTO tmp = new InstanceTextAttributeDTO() ;
                    tmp.setValue(((TextBox) attrWidget).getValue());
                    attribute = tmp ;
                }
                 * */
                if (! (attrWidget instanceof ListBox)){
                    attribute.setName(name.getText());
                    result.add(attribute);
                }
            }
        }
        return result;
    }

    public void clearInputs() {
        removeAllRows();
    }

    private void removeAllRows() {
        for (int i = m_attributeList.getRowCount() - 1; i >= 0; i--) {
            m_attributeList.removeRow(i);
        }
    }

    public void setEditionMode(boolean editionMode) {
        m_editionMode = editionMode;
        m_addLink.setVisible(editionMode);
        m_delLink.setVisible(editionMode);
        for (int i = 1; i < m_attributeList.getRowCount(); i++) {
            Widget checkBox = m_attributeList.getWidget(i, 0);
            checkBox.setVisible(editionMode);
            Widget attrWidget = m_attributeList.getWidget(i, 2);
            if (attrWidget instanceof CheckBox) {
                ((CheckBox) attrWidget).setEnabled(editionMode);
            } else if (attrWidget instanceof DateBox) {
                ((DateBox) attrWidget).setEnabled(editionMode);
            } else if (attrWidget instanceof TextBox) {
                ((TextBox) attrWidget).setEnabled(editionMode);
            }
        }
    }

    private void addAttribute() {
        final int row = m_attributeList.getRowCount();

        CheckBox select = new CheckBox();
        EditableLabel l = new EditableLabel();
        l.setValidator(new NotEmptyValidator());
        l.setText(ServiceLocator.getInstance().getExplorerI18NConstants().newAttributeLabel());
        l.setStyleName("instanceAttributes");
        final AttributeTypeChooser choiceList = new AttributeTypeChooser();

        m_attributeList.setWidget(row, 0, select);
        m_attributeList.setWidget(row, 1, l);
        m_attributeList.setWidget(row, 2, choiceList);

        choiceList.addChangeHandler(new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                Widget toAdd = null;
                InstanceAttributeTemplateDTO.AttributeType type = choiceList.getSelected();
                switch (type) {
                    case BOOLEAN:
                        toAdd = new CheckBox();
                        break;
                    case DATE:
                        toAdd = new DateBox();
                        break;
                    case NUMBER:
                        toAdd = new NumberConstrainedTextBox();
                        break;
                    case TEXT:
                        toAdd = new TextBox();
                        break;
                    case URL:
                        toAdd = new EditableURL() ;
                        break;
                }

                m_attributeList.setWidget(row, 2, toAdd);
            }
        });
    }

    private void removeAttributes() {
        for (int i = m_attributeList.getRowCount() - 1; i > 0; i--) {
            CheckBox c = (CheckBox) m_attributeList.getWidget(i, 0);
            if (c != null) {
                if (c.getValue()) {
                    TextBox name = (TextBox) m_attributeList.getWidget(i, 1);
                    if (m_attrs.containsKey(name.getText())){
                        m_attrs.remove(name.getText()) ;
                    }
                    m_attributeList.removeRow(i);

                }
            }
        }
    }

    private class AttributeTypeChooser extends ListBox {

        public AttributeTypeChooser() {
            ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants();
            addItem(constants.selectTypeLabel());
            addItem(constants.textSearchAttribute());
            addItem(constants.numberSearchAttribute());
            addItem(constants.dateSearchAttribute());
            addItem(constants.booleanSearchAttribute());
            addItem(constants.urlSearchAttribute());
            setSelectedIndex(0);
        }

        public InstanceAttributeTemplateDTO.AttributeType getSelected() {
            InstanceAttributeTemplateDTO.AttributeType result = null;
            switch (getSelectedIndex()) {
                case 1:
                    result = InstanceAttributeTemplateDTO.AttributeType.TEXT;
                    break;
                case 2:
                    result = InstanceAttributeTemplateDTO.AttributeType.NUMBER;
                    break;
                case 3:
                    result = InstanceAttributeTemplateDTO.AttributeType.DATE;
                    break;
                case 4:
                    result = InstanceAttributeTemplateDTO.AttributeType.BOOLEAN;
                    break;
                case 5:
                    result = InstanceAttributeTemplateDTO.AttributeType.URL;
                    break;
            }

            return result;
        }
    }
}
