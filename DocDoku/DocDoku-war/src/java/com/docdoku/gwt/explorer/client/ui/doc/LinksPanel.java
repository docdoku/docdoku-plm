package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.data.DocOracle;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.docdoku.gwt.explorer.shared.DocumentDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Florent GARIN
 */
public class LinksPanel extends DataRoundedPanel implements ClickHandler {
    
    private Grid m_linkList;
    private Label m_addLink;
    private Label m_delLink;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();
    private boolean m_editionMode;
    private DocOracle oracle;

    public LinksPanel() {
        super(ServiceLocator.getInstance().getExplorerI18NConstants().tabLinks());
        m_linkList = new Grid();
        m_delLink = new Label(i18n.btnRemove());
        m_delLink.setStyleName("normalLinkAction");
        m_delLink.addClickHandler(this);

        m_addLink = new Label(i18n.btnAdd());
        m_addLink.setStyleName("normalLinkAction");
        m_addLink.addClickHandler(this);

        HorizontalPanel widgetFormPanel = new HorizontalPanel();
        widgetFormPanel.setSpacing(5);
        widgetFormPanel.add(m_addLink);
        widgetFormPanel.add(m_delLink);
        inputPanel.setWidget(0, 0, widgetFormPanel);
        ScrollPanel scroll = new ScrollPanel(m_linkList);
        scroll.setHeight("10em");
        inputPanel.setWidget(1, 0, scroll);
        oracle = new DocOracle();
        m_editionMode = true ;
    }

    public void setEditionMode(boolean editionMode) {
        if (editionMode != m_editionMode) {
            m_editionMode = editionMode;
            m_addLink.setVisible(editionMode);
            m_delLink.setVisible(editionMode);
            for (int i = 0; i < m_linkList.getRowCount(); i++) {
                Widget linkCheckBox = m_linkList.getWidget(i, 0);
                if (linkCheckBox instanceof CheckBox) {
                    ((CheckBox) linkCheckBox).setVisible(editionMode);
                    if (editionMode) {
                        // change read only to edition
                        ChooseLinkPanel newLinkPanel = new ChooseLinkPanel(oracle);
                        m_linkList.clearCell(i, 1);
                        m_linkList.setWidget(i, 1, newLinkPanel);
                    } else {
                        // change edition to read only
                        ChooseLinkPanel s = (ChooseLinkPanel) m_linkList.getWidget(i, 1);
                        m_linkList.clearCell(i, 1);
                        m_linkList.setText(i, 1, s.getSelectedDocument().toString());
                    }
                }
            }
        }
    }

    public void setLinks(Set<DocumentDTO> links, String workspaceId) {
        oracle.setWorkspaceId(workspaceId);
        m_linkList.clear();
        m_linkList.resize(links.size(), 2);
        int i = 0;
        for (DocumentDTO link : links) {
            CheckBox linkCheckBox = new CheckBox();
            linkCheckBox.setVisible(m_editionMode);
            m_linkList.setWidget(i, 0, linkCheckBox);
            if (m_editionMode) {
                ChooseLinkPanel p = new ChooseLinkPanel(oracle, link);
                m_linkList.setWidget(i, 1, p);
            } else {
                m_linkList.setText(i, 1, link.toString());
            }
            i++;
        }
    }

    public void addLinkPanel() {
        m_linkList.insertRow(m_linkList.getRowCount());
        CheckBox linkCheckBox = new CheckBox();
        m_linkList.setWidget(m_linkList.getRowCount() - 1, 0, linkCheckBox);
        ChooseLinkPanel p = new ChooseLinkPanel(oracle);
        m_linkList.setWidget(m_linkList.getRowCount() - 1, 1, p);
    }

    public void removeLinkPanel() {
        for (int i = m_linkList.getRowCount() - 1; i >= 0; i--) {
            CheckBox c = (CheckBox) m_linkList.getWidget(i, 0);
            if (c.getValue()) {
                //remove
                m_linkList.removeRow(i);
            }
        }
    }

    public void setOracle(DocOracle oracle) {
        this.oracle = oracle;
    }

    public void onClick(ClickEvent event) {
        if (event.getSource() == m_addLink) {
            addLinkPanel();
        } else {
            removeLinkPanel();
        }
    }

    public DocumentDTO[] getLinks(){
        Set<DocumentDTO> resultTmp = new HashSet<DocumentDTO>() ;
        for (int i = 0 ; i < m_linkList.getRowCount() ; i++){
            ChooseLinkPanel p = (ChooseLinkPanel) m_linkList.getWidget(i, 1) ;
            if (p.getSelectedDocument() != null){
                resultTmp.add(p.getSelectedDocument()) ;
            }
        }
        DocumentDTO result[] = new DocumentDTO[resultTmp.size()] ;
        resultTmp.toArray(result) ;
        return result ;
    }
}