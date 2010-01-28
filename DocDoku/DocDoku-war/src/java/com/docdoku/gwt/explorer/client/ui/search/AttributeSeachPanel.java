package com.docdoku.gwt.explorer.client.ui.search;

import java.util.ArrayList;
import java.util.List;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.ui.AttributesTypesList;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.common.InstanceAttributeTemplateDTO;
import com.docdoku.gwt.explorer.common.MasterDocumentTemplateDTO;
import com.docdoku.gwt.explorer.common.SearchQueryDTO;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Emmanuel Nhan
 * 
 */
public class AttributeSeachPanel extends FlexTable implements ClickHandler, ChangeHandler {

    private Image addAttribute;
    private List<AttributeLine> attributeLines;
    private List<Image> removeButtons;
    private ListBox modelList;
    private String workspaceId;
    private HorizontalPanel modelPanel;

    public AttributeSeachPanel(String workspaceId) {
        this.workspaceId = workspaceId;
        attributeLines = new ArrayList<AttributeLine>();
        removeButtons = new ArrayList<Image>();
        setupUi();
        fetchModelList(ServiceLocator.getInstance().getExplorerI18NConstants().notSpecifiedOption());
    }

    public SearchQueryDTO.AbstractAttributeQueryDTO[] getAttributes() {
        Set<SearchQueryDTO.AbstractAttributeQueryDTO> tempSet = new HashSet<SearchQueryDTO.AbstractAttributeQueryDTO>();

        for (int i = 0; i < attributeLines.size(); i++) {
            if (attributeLines.get(i).getAttribute() != null) {
                tempSet.add(attributeLines.get(i).getAttribute());
            }
        }
        if (tempSet.size() != 0) {
            SearchQueryDTO.AbstractAttributeQueryDTO result[] = new SearchQueryDTO.AbstractAttributeQueryDTO[tempSet.size()];
            tempSet.toArray(result);
            return result;
        }
        return null;

    }

    private void setupUi() {
        // by default : one empty line (text)
        AttributeLine l = new AttributeLine();
        attributeLines.add(l);
        setWidget(0, 1, l);


        // add button (by default : on top )
        addAttribute = new Image();
        ServiceLocator.getInstance().getExplorerImageBundle().getSearchAddImage().applyTo(addAttribute);
        setWidget(1, 0, addAttribute);
        addAttribute.addClickHandler(this);

        // selection box :
        modelPanel = new HorizontalPanel();
        modelPanel.add(new Label(ServiceLocator.getInstance().getExplorerI18NConstants().modelLabel()));
        modelList = new ListBox();
        modelList.addChangeHandler(this);
        modelPanel.add(modelList);
        setWidget(2, 0, modelPanel);
        getFlexCellFormatter().setColSpan(2, 0, 2);
        getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
    }

    private void addAttributeLine(InstanceAttributeTemplateDTO.AttributeType type, String name) {
        // append line
        AttributeLine l = new AttributeLine();
        l.setActive(type);
        l.setAttributeName(name);
        insertRow(attributeLines.size());
        setWidget(attributeLines.size(), 1, l);

        // check if we have to add a minus for the first line :
        if (removeButtons.size() == 0) {
            Image imRemoveTmp = new Image();
            ServiceLocator.getInstance().getExplorerImageBundle().getSearchRemoveImage().applyTo(imRemoveTmp);
            removeButtons.add(imRemoveTmp);
            setWidget(0, 0, imRemoveTmp);
            imRemoveTmp.addClickHandler(this);
        }

        // then, add a button for new line
        Image rmTmp = new Image();
        ServiceLocator.getInstance().getExplorerImageBundle().getSearchRemoveImage().applyTo(rmTmp);
        removeButtons.add(rmTmp);
        setWidget(attributeLines.size(), 0, rmTmp);
        rmTmp.addClickHandler(this);

        // finally, append the new line to the list
        attributeLines.add(l);
    }

    public void onClick(ClickEvent event) {
        if (event.getSource() == addAttribute) {
            // insert a new line at the end
            addAttributeLine(InstanceAttributeTemplateDTO.AttributeType.TEXT, "");

        } else {
            // remove a line at specified index :
            removeLine(removeButtons.indexOf(event.getSource()));
        }
    }

    public void onChange(ChangeEvent event) {
        // get the selected id :
        String templateName = modelList.getItemText(modelList.getSelectedIndex());
        if (templateName.equals(ServiceLocator.getInstance().getExplorerI18NConstants().notSpecifiedOption())) {
            removeAllLines();
            AttributeLine l = new AttributeLine();
            attributeLines.add(l);
            insertRow(0);
            setWidget(0, 1, l);
        } else {
            AsyncCallback<MasterDocumentTemplateDTO> callback = new AsyncCallback<MasterDocumentTemplateDTO>() {

                public void onFailure(Throwable caught) {
                    HTMLUtil.showError(caught.getMessage());
                }

                public void onSuccess(MasterDocumentTemplateDTO result) {
                    removeAllLines();
                    if (result.getAttributeTemplates().size() > 1) {
                        int i = 0;
                        for (InstanceAttributeTemplateDTO att : result.getAttributeTemplates()) {
                            if (i == 0) {
                                AttributeLine l = new AttributeLine();
                                l.setActive(att.getAttributeType());
                                l.setAttributeName(att.getName());
                                attributeLines.add(l);
                                insertRow(0);
                                setWidget(0, 1, l);
                            } else {
                                addAttributeLine(att.getAttributeType(), att.getName());
                            }
                            i++;
                        }
                    } else {
                        if (result.getAttributeTemplates().size() == 1) {
                            // only one line
                            // but a for to get it easily
                            for (InstanceAttributeTemplateDTO att : result.getAttributeTemplates()) {
                                AttributeLine l = new AttributeLine();
                                l.setActive(att.getAttributeType());
                                l.setAttributeName(att.getName());
                                attributeLines.add(l);
                                insertRow(0);
                                setWidget(0, 1, l);
                            }
                        } else {
                            AttributeLine l = new AttributeLine();
                            attributeLines.add(l);
                            insertRow(0);
                            setWidget(0, 1, l);
                        }

                    }
                }
            };
            ServiceLocator.getInstance().getExplorerService().getMDocTemplate(workspaceId, templateName, callback);
        }

    }

    private void removeLine(int index) {
        // remove button :
        removeButtons.remove(index);
        // remove attribute panel :
        attributeLines.remove(index);
        // remove row :
        removeRow(index);
        // if only one line, remove remove button:
        if (attributeLines.size() == 1) {
            remove(removeButtons.get(0));
            removeButtons.clear();
        }
    }

    private void removeAllLines() {
        for (int i = 0; i < attributeLines.size(); i++) {
            removeRow(0);
        }
        attributeLines.clear();
        removeButtons.clear();
    }

    private void fetchModelList(final String selectedItem) {
        AsyncCallback<MasterDocumentTemplateDTO[]> callback = new AsyncCallback<MasterDocumentTemplateDTO[]>() {

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }

            public void onSuccess(MasterDocumentTemplateDTO[] result) {
                modelList.clear();
                modelList.addItem(ServiceLocator.getInstance().getExplorerI18NConstants().notSpecifiedOption());
                int i = 1;
                for (MasterDocumentTemplateDTO template : result) {
                    modelList.addItem(template.getId());
                    if (template.getId().equals(selectedItem)) {
                        modelList.setSelectedIndex(i);
                    }
                    i++;
                }
            }
        };
        ServiceLocator.getInstance().getExplorerService().getMDocTemplates(workspaceId, callback);
    }



    private class AttributeLine extends Composite implements ChangeHandler {

        private AttributesTypesList choiceBox;
        private AbstractAttributePanel attributePanel;
        private HorizontalPanel mainPanel;

        public AttributeLine() {
            mainPanel = new HorizontalPanel();
            choiceBox = new AttributesTypesList();
            attributePanel = new TextAttributePanel();
            mainPanel.add(choiceBox);
            mainPanel.add(attributePanel);
            initWidget(mainPanel);
            choiceBox.addChangeHandler(this);
        }

        public void onChange(ChangeEvent event) {
            setActive(choiceBox.getSelected());
        }

        public void setActive(InstanceAttributeTemplateDTO.AttributeType state) {
            switch (state) {
                case TEXT:
                    attributePanel = new TextAttributePanel();
                    choiceBox.setSelectedIndex(0);
                    break;
                case NUMBER:
                    attributePanel = new NumberAttributePanel();
                    choiceBox.setSelectedIndex(1);
                    break;
                case BOOLEAN:
                    attributePanel = new BooleanAttributePanel();
                    choiceBox.setSelectedIndex(3);
                    break;
                case DATE:
                    attributePanel = new DateAttributePanel();
                    choiceBox.setSelectedIndex(2);
                    break;
                case URL :
                    attributePanel = new UrlAttributePanel() ;
                    choiceBox.setSelectedIndex(4);
            }
            mainPanel.remove(1);
            mainPanel.add(attributePanel);
        }

        public void setAttributeName(String name) {
            attributePanel.setNameValue(name);
        }

        public SearchQueryDTO.AbstractAttributeQueryDTO getAttribute() {
            return attributePanel.getAttribute();
        }
    }
}
