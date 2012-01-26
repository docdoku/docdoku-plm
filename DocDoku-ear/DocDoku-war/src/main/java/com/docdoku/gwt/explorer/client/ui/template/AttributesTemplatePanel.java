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

package com.docdoku.gwt.explorer.client.ui.template;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.docdoku.gwt.explorer.shared.InstanceAttributeTemplateDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Florent Garin
 */
public class AttributesTemplatePanel extends DataRoundedPanel {

    private FlexTable m_attributeList;
    private Label m_addLink;
    private Label m_delLink;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public AttributesTemplatePanel() {
        super(ServiceLocator.getInstance().getExplorerI18NConstants().tabAttributes());
        m_attributeList = new FlexTable();
        m_delLink = new Label(i18n.btnRemove());
        m_delLink.setStyleName("normalLinkAction");
        m_delLink.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                for(int i=m_attributeList.getRowCount()-1; i>=0;i--){
                    CheckBox attrBox=(CheckBox) m_attributeList.getWidget(i, 0);
                    if(attrBox.getValue())
                        m_attributeList.removeRow(i);           
                }
            }

        });

        m_addLink = new Label(i18n.btnAdd());
        m_addLink.setStyleName("normalLinkAction");
        m_addLink.addClickHandler(new ClickHandler(){

            public void onClick(ClickEvent event) {
                addAttribute("");
            }

        });
        HorizontalPanel widgetFormPanel = new HorizontalPanel();
        widgetFormPanel.setSpacing(5);
        widgetFormPanel.add(m_addLink);
        widgetFormPanel.add(m_delLink);
        inputPanel.setWidget(0, 0, widgetFormPanel);

        ScrollPanel scroll = new ScrollPanel(m_attributeList);
        scroll.setHeight("10em");
        inputPanel.setWidget(1, 0, scroll);
    }

    public Set<InstanceAttributeTemplateDTO> getAttributes() {
        Set<InstanceAttributeTemplateDTO> attrs = new HashSet<InstanceAttributeTemplateDTO>();

        for(int i=0; i <m_attributeList.getRowCount();i++){
            InstanceAttributeTemplateDTO attr= new InstanceAttributeTemplateDTO();
            TextBox attrNameText=(TextBox) m_attributeList.getWidget(i, 1);
            ListBox type = (ListBox) m_attributeList.getWidget(i, 2);
            attr.setName(attrNameText.getText());
            attr.setAttributeType(InstanceAttributeTemplateDTO.AttributeType.valueOf(type.getValue(type.getSelectedIndex())));
            attrs.add(attr);
        }

        return attrs;
    }

    public void clearInputs() {
        removeAllRows();
    }

    private void addAttribute(String name) {
        int i = m_attributeList.getRowCount();
        CheckBox attrBox = new CheckBox();
        m_attributeList.setWidget(i, 0, attrBox);
        TextBox attrNameText=new TextBox();
        attrNameText.setText(name);
        m_attributeList.setWidget(i, 1, attrNameText);
        m_attributeList.setWidget(i, 2, createAttributeTypeListBox(InstanceAttributeTemplateDTO.AttributeType.TEXT));
    }

    
    private void removeAllRows(){
        for(int i=m_attributeList.getRowCount()-1; i>=0;i--){
            m_attributeList.removeRow(i);
        }
    }

    public void setAttributes(Set<InstanceAttributeTemplateDTO> attrs) {
        int i = 0;
        removeAllRows();
        for (InstanceAttributeTemplateDTO attr : attrs) {
            CheckBox attrBox = new CheckBox();
            m_attributeList.setWidget(i, 0, attrBox);
            TextBox attrNameText=new TextBox();
            attrNameText.setText(attr.getName());
            m_attributeList.setWidget(i, 1, attrNameText);
            m_attributeList.setWidget(i, 2, createAttributeTypeListBox(attr.getAttributeType()));
            i++;
        }
    }

    private ListBox createAttributeTypeListBox(InstanceAttributeTemplateDTO.AttributeType selectedType) {
        ListBox list = new ListBox();
        int i=0;
        for (InstanceAttributeTemplateDTO.AttributeType type : InstanceAttributeTemplateDTO.AttributeType.values()) {
            list.addItem(getTypeLabel(type), type.name());
            if(type.equals(selectedType))
                list.setSelectedIndex(i);

            i++;
        }

        return list;
    }

    private String getTypeLabel(InstanceAttributeTemplateDTO.AttributeType selectedType) {
        String typeLabel = null;
        switch (selectedType) {
            case BOOLEAN:
                typeLabel = i18n.fieldAttrBoolean();
                break;
            case DATE:
                typeLabel = i18n.fieldAttrDate();
                break;
            case NUMBER:
                typeLabel = i18n.fieldAttrNumber();
                break;
            case TEXT:
                typeLabel = i18n.fieldAttrText();
                break;
            case URL:
                typeLabel = i18n.fieldAttrUrl();
        }
        return typeLabel;
    }
}