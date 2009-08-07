/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;

/**
 *
 * @author Emmanuel Nhan
 */
public class ExplorerMenuBar extends VerticalPanel{
    private Button m_deleteBtn;
    private ExplorerPage m_mainPage;
    private Label m_selectAll ;
    private Label m_selectNone ;
    private Map<String, Action> m_cmds;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();
    private HorizontalPanel buttonBar;

    public ExplorerMenuBar(final Map<String, Action> cmds, ExplorerPage mainPage, boolean selectionTop){
        setStyleName("myMenuBar");
        m_mainPage = mainPage;
        m_cmds = cmds;

        m_deleteBtn = new Button(i18n.actionRemove());
        m_deleteBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("DeleteElementCommand").execute();
            }
        });


        buttonBar= new HorizontalPanel();

        m_selectAll = new Label(i18n.selectAllLabel());
        m_selectAll.setStyleName("clickableLabel");
        m_selectAll.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_mainPage.selectAllElementsInTable();
            }
        });
        m_selectNone = new Label(i18n.selectNoneLabel());
        m_selectNone.setStyleName("clickableLabel");
        m_selectNone.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_mainPage.unselectAllElementsInTable();
            }
        });
        HorizontalPanel selection = new HorizontalPanel();
        selection.add(new Label(i18n.selectLabel()+": "));
        selection.add(m_selectAll);
        selection.add(m_selectNone);
        
        buttonBar.add(m_deleteBtn);
        if (!selectionTop){
            add(buttonBar);
            add(selection);
        }else{
            add(selection);
            add(buttonBar);
        }
        

    }

    public void addExtension(Widget w) {
        buttonBar.add(w);
    }

}
