package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.common.ACLDTO;
import com.docdoku.gwt.explorer.common.MasterDocumentDTO;
import com.docdoku.gwt.explorer.common.UserDTO;
import com.docdoku.gwt.explorer.common.UserGroupDTO;
import com.docdoku.gwt.explorer.common.WorkflowModelDTO;
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
public class CreateVersionPanel extends FlexTable{

    private Button m_okBtn;
    private Label m_backAction;
    private DescriptionPanel m_descriptionPanel;
    private SecurityPanel m_securityPanel;
    private CreateVersionMainPanel m_mainPanel;

    private String m_mdocId;
    private String m_mdocVersion;
    private String m_workspaceId;

    
    public CreateVersionPanel(final Map<String,Action> cmds){
        ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();
        FlexCellFormatter cellFormatter = getFlexCellFormatter();
        m_descriptionPanel = new DescriptionPanel();
        m_securityPanel= new SecurityPanel();
        m_mainPanel = new CreateVersionMainPanel();

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
                ACLDTO acl=m_securityPanel.getACL();
                cmds.get("CreateVersionCommand").execute(m_workspaceId,m_mdocId,m_mdocVersion,m_mainPanel.getMDocTitle(),m_mainPanel.getWorkflowModelId(),m_descriptionPanel.getMDocDescription(),acl);
            }
        });
        buttonsPanel.add(m_backAction);
        buttonsPanel.add(m_okBtn);

        setWidget(0,0, m_mainPanel);
        setWidget(0,1, m_descriptionPanel);
        setWidget(0,2, m_securityPanel);
        setWidget(1,0,buttonsPanel);
        cellFormatter.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        cellFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        cellFormatter.setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
    }

    public void setMDocId(String mdocId) {
        this.m_mdocId = mdocId;
    }

    public void setMDocVersion(String mdocVersion) {
        this.m_mdocVersion = mdocVersion;
    }

    public void setWorkspaceId(String workspaceId) {
        this.m_workspaceId = workspaceId;
    }
    

  
    public void clearInputs() {
        m_mainPanel.clearInputs();
        m_descriptionPanel.clearInputs();
        m_securityPanel.clearInputs();
    }

    public void setWorkflowModels(WorkflowModelDTO[] wks) {
        m_mainPanel.setWorkflowModels(wks);
    }

    public void setUserMemberships(UserDTO[] userMSs){
        m_securityPanel.setUserMemberships(userMSs);
    }

    public void setUserGroupMemberships(UserGroupDTO[] groupMSs){
        m_securityPanel.setUserGroupMemberships(groupMSs);
    }
    
}
