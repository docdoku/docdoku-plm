package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;


/**
 *
 * @author Florent GARIN
 */
public class SearchPanel extends HorizontalPanel{

    private Button m_searchBtn;
    private TextBox m_fullTextSearch;
    private Label m_moreOptionsLink;
    private ExplorerPage m_mainPage ;
    
    public SearchPanel(final Map<String,Action> cmds, ExplorerPage mainPage){
        ExplorerI18NConstants explorerConstants=ServiceLocator.getInstance().getExplorerI18NConstants();
        m_mainPage = mainPage;
        m_fullTextSearch=new TextBox();
        m_moreOptionsLink=new Label(explorerConstants.moreSearchOptions());
        m_moreOptionsLink.setStyleName("smallLinkAction");
        m_moreOptionsLink.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_mainPage.showCompleteSearchPanel();
            }
        }) ;
        m_searchBtn=new Button(explorerConstants.actionSearch());
        m_searchBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("SearchCommand").execute(m_mainPage.getWorkspaceId(), null,
                null, null, null, null,null,null,null,null, m_fullTextSearch.getText());
            }
        });
        createLayout();
    }

    private void createLayout() {
        setSpacing(5);
        add(m_fullTextSearch);
        add(m_searchBtn);
        add(m_moreOptionsLink);
    }
}
