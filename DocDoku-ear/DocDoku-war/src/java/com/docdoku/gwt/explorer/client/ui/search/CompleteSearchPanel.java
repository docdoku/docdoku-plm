package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

public class CompleteSearchPanel extends DataRoundedPanel {

    private GeneralSearchPanel generalPanel;
    private AttributeSeachPanel attributePanel;
    private AdvancedSearchPanel extended;
    private Button searchButton;
    private Button cancelButton;
    private ExplorerPage mainPage;
    private String workspaceId;
    private Map<String, Action> m_cmds;
    private final ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants() ;

    public CompleteSearchPanel(Map<String, Action> cmds, ExplorerPage mainPage) {
        super(ServiceLocator.getInstance().getExplorerI18NConstants().generalTitle(), RoundedPanel.TOPLEFT);
        this.mainPage = mainPage;
        m_cmds = cmds;
        this.workspaceId = mainPage.getWorkspaceId();
        headerLabel.addStyleName("searchPanel");
        rp.addStyleName("searchPanel");
        inputPanel.addStyleName("searchPanel");
        vp.addStyleName("searchPanel");
        setupUi();
    }

    public void setupUi() {

        generalPanel = new GeneralSearchPanel(mainPage.getWorkspaceId());
        attributePanel = new AttributeSeachPanel(mainPage.getWorkspaceId());
        extended = new AdvancedSearchPanel();
        DisclosurePanel containerAdvanced = new DisclosurePanel(constants.advancedLabel());
        containerAdvanced.addStyleName("searchPanel");
        containerAdvanced.setAnimationEnabled(true);
        containerAdvanced.setContent(extended);
        containerAdvanced.setOpen(true);
        inputPanel.setWidget(0, 0, generalPanel);
        ScrollPanel scrollAttributes = new ScrollPanel(attributePanel);
        scrollAttributes.setHeight("120px");
        DisclosurePanel containerAttributes = new DisclosurePanel(constants.attributesTitle());
        containerAttributes.addStyleName("searchPanel");
        containerAttributes.setAnimationEnabled(true);
        containerAttributes.setContent(scrollAttributes);
        containerAttributes.setOpen(true);
        inputPanel.setWidget(0, 1, containerAttributes);
        inputPanel.setWidget(0, 2, containerAdvanced);
        searchButton = new Button(ServiceLocator.getInstance().getExplorerI18NConstants().searchLabel());
        searchButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_cmds.get("SearchCommand").execute(workspaceId, generalPanel.getReference(),
                        generalPanel.getTitleField(), generalPanel.getVersion(), generalPanel.getAuthor(), generalPanel.getType(),
                        extended.getFromDate(), extended.getToDate(), attributePanel.getAttributes(), extended.getTags(), extended.getContent());
            }
        });
        cancelButton = new Button(ServiceLocator.getInstance().getExplorerI18NConstants().cancelLabel());
        cancelButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                mainPage.showBasicSearchPanel();
            }
        });

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(5);
        buttonsPanel.add(searchButton);
        buttonsPanel.add(cancelButton);
        FlexTable.FlexCellFormatter cellFormatter = inputPanel.getFlexCellFormatter();

        inputPanel.setWidget(1, 2, buttonsPanel);
        cellFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        cellFormatter.setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
        //cellFormatter.setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_RIGHT);

    }
}
