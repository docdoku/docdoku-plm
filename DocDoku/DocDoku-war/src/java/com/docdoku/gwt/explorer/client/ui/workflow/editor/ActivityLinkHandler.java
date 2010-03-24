/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.google.gwt.event.shared.EventHandler;

/**
 *
 * @author manu
 */
public interface ActivityLinkHandler extends EventHandler {

        void onAddActivityClicked(ActivityLinkEvent ev);
}