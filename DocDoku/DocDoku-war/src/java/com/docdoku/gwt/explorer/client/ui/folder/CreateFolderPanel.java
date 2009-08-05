package com.docdoku.gwt.explorer.client.ui.folder;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;

/**
 *
 * @author Florent GARIN
 */
public class CreateFolderPanel extends FlexTable {

    private Button m_okBtn;
    private Label m_backAction;
    private CreateFolderMainPanel m_mainPanel;

    public CreateFolderPanel(final Map<String, Action> cmds) {
        ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();
        FlexCellFormatter cellFormatter = getFlexCellFormatter();
        m_mainPanel = new CreateFolderMainPanel();

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(5);
        m_backAction=new Label(i18n.btnBack());
        m_backAction.setStyleName("normalLinkAction");
        m_backAction.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                cmds.get("BackCommand").execute();
            }
        });
        m_okBtn = new Button(i18n.btnCreate());
        m_okBtn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                cmds.get("CreateFolderCommand").execute(m_mainPanel.getParentFolderText(),m_mainPanel.getFolderText());
            }
        });
        buttonsPanel.add(m_backAction);
        buttonsPanel.add(m_okBtn);
        

        setWidget(0,0, m_mainPanel);
        setWidget(1,0,buttonsPanel);
        cellFormatter.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
    }

    public void setParentFolder(String parentFolder){
        m_mainPanel.setParentFolder(parentFolder);
    }
    public void clearInputs() {
        m_mainPanel.clearInputs();
    }
}
