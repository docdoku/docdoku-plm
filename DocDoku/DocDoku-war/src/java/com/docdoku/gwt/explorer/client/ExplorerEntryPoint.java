/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client;

import com.docdoku.gwt.explorer.client.actions.ActionMap;
import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.docdoku.gwt.explorer.client.ui.doc.MaskLineEdit;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author Florent GARIN
 */


public class ExplorerEntryPoint implements EntryPoint {

    private Dictionary m_inputs;
    private ExplorerPage m_mainPage;
    public void onModuleLoad() {
    
        m_inputs = Dictionary.getDictionary("inputs");
        String workspaceID = m_inputs.get("workspaceID");
        String login = m_inputs.get("login");
        Window.setMargin("8px");
        ActionMap cmds=new ActionMap();
        m_mainPage=new ExplorerPage(workspaceID, login);
        cmds.init(m_mainPage);
        m_mainPage.init(cmds);
        RootPanel.get("content").add(m_mainPage);
        MaskLineEdit tb = new MaskLineEdit();
        tb.setMask("ref-___-ok");
        RootPanel.get().add(tb);
       
    }
}
