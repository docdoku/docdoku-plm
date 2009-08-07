/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009 DocDoku SARL
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
package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.menu.AbstractMenuItem;
import com.docdoku.gwt.explorer.client.ui.widget.menu.ButtonMenu;
import com.docdoku.gwt.explorer.client.ui.widget.menu.DocdokuLabelMenuItem;
import com.docdoku.gwt.explorer.client.util.DocdokuCommand;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;
import org.cobogw.gwt.user.client.ui.ButtonBar;

/**
 *
 * @author Florent GARIN
 */
public class ExplorerDocumentMenuBar extends VerticalPanel {

    private Button m_checkInBtn;
    private Button m_checkOutBtn;
    private Button m_undoCheckOutBtn;
    private Button m_deleteBtn;
    private ButtonMenu m_tagActionsMenu;
    private ExplorerPage m_mainPage;
    private Label m_selectAll;
    private Label m_selectNone;
    private Label m_selectCheckedOut;
    private Label m_selectCheckedIn;
    private Map<String, Action> m_cmds;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public ExplorerDocumentMenuBar(final Map<String, Action> cmds, ExplorerPage mainPage, boolean selectionTop) {
        setStyleName("myMenuBar");
        m_mainPage = mainPage;
        m_cmds = cmds;

        m_checkInBtn = new Button(i18n.actionCheckIn());
        m_checkInBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("CheckInCommand").execute();
            }
        });

        m_checkOutBtn = new Button(i18n.actionCheckOut());
        m_checkOutBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("CheckOutCommand").execute();
            }
        });

        m_undoCheckOutBtn = new Button(i18n.actionUndoCheckOut());
        m_undoCheckOutBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("UndoCheckOutCommand").execute();
            }
        });
        m_deleteBtn = new Button(i18n.actionRemove());
        m_deleteBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("DeleteElementCommand").execute();
            }
        });

        m_tagActionsMenu = new ButtonMenu(i18n.tagsLabel());

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
        m_selectCheckedIn = new Label(i18n.selectCheckedInLabel());
        m_selectCheckedIn.setStyleName("clickableLabel");
        m_selectCheckedIn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_mainPage.selectAllCheckedInDocs();
            }
        });
        m_selectCheckedOut = new Label(i18n.selectCheckedOutLabel());
        m_selectCheckedOut.setStyleName("clickableLabel");
        m_selectCheckedOut.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_mainPage.selectAllCheckedOutDocs();
            }
        });

        createLayout(selectionTop);
        fetchTags();
    }

    private void createLayout(boolean selectionTop) {
        HorizontalPanel top = new HorizontalPanel();

        ButtonBar iterationBar = new ButtonBar();
        iterationBar.add(m_checkInBtn);
        iterationBar.add(m_checkOutBtn);
        iterationBar.add(m_undoCheckOutBtn);
        top.add(iterationBar);
        iterationBar.setStyleName("docOptions") ;

        ButtonBar otherActionsBar = new ButtonBar();
        otherActionsBar.add(m_deleteBtn);
        //otherActionsBar.add(m_selectionMenu);
        otherActionsBar.add(m_tagActionsMenu);
        top.add(otherActionsBar);



        HorizontalPanel bottom = new HorizontalPanel();
        bottom.add(new Label(i18n.selectLabel() + ":"));
        bottom.add(m_selectAll);
        bottom.add(m_selectNone);
        bottom.add(m_selectCheckedIn);
        bottom.add(m_selectCheckedOut);
        if (!selectionTop) {
            add(top);
            add(bottom);
        }else{
            add(bottom);
            add(top);
        }
    }

    private void fetchTags() {
        AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {

            public void onSuccess(String[] tags) {
                m_tagActionsMenu.clear();
                for (int i = 0; i < tags.length; i++) {
                    DocdokuLabelMenuItem item = new DocdokuLabelMenuItem("+ " + tags[i]);
                    item.setAction(m_cmds.get("SaveTagsCommand"));
                    item.setParameters(false, true, tags[i]);
                    m_tagActionsMenu.addItem(item);
                }

                for (int i = 0; i < tags.length; i++) {
                    DocdokuLabelMenuItem item = new DocdokuLabelMenuItem("- " + tags[i]);
                    item.setAction(m_cmds.get("SaveTagsCommand"));
                    item.setParameters(false, false, tags[i]);
                    m_tagActionsMenu.addItem(item);
                }

                AddTagItem item = new AddTagItem();
                m_tagActionsMenu.addItem(item);
            }

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }
        };
        ServiceLocator.getInstance().getExplorerService().getTags(m_mainPage.getWorkspaceId(), callback);
    }

    public void reloadTags() {
        fetchTags();
    }

    private class AddTagItem extends AbstractMenuItem {


        private HorizontalPanel panel ;
        private TextBox labelField ;
        private Button createButton ;

        public AddTagItem() {
            panel = new HorizontalPanel() ;
            labelField = new TextBox() ;
            createButton = new Button(ServiceLocator.getInstance().getExplorerI18NConstants().btnCreate()) ;
            panel.add(labelField);
            panel.add(createButton);
            initWidget(panel);
            DocdokuCommand command = new DocdokuCommand() ;
            command.setAction(m_cmds.get("SaveTagsCommand"));
            setCommand(command);
            createButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    activate();
                }
            });
            addStyleName("createLabelPanel");
            labelField.addStyleName("createLabelField");
            labelField.setVisibleLength(8);

            panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        }

        @Override
        protected boolean beforeCommandCall() {
            String tag = labelField.getText();
            if (tag != null && !tag.trim().equals("")) {
                ((DocdokuCommand)getCommand()).setParameters(true, true, tag);
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void afterCommandCall() {
            
        }

        public void setSelected(boolean selected) {
            
        }

        public void onShowUp() {
            labelField.setText("");
        }

        


    }


}
