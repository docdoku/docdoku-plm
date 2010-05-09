/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.gwt.explorer.client.actions;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.MasterDocumentDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

/**
 *
 * @author manu
 */
public class MoveMDocCommand implements Action {

    private ExplorerPage mainPage;

    MoveMDocCommand(ExplorerPage mainPage) {
        this.mainPage = mainPage;
    }

    public void execute(Object... userObject) {
        // params :
        // userObject[0] : new folder
        // selected docs to delete are retrieved with the main page
        List<MasterDocumentDTO> selectedDocs = mainPage.getSelectedMDocs();
        for (MasterDocumentDTO doc : selectedDocs) {
            AsyncCallback<MasterDocumentDTO> callback = new AsyncCallback<MasterDocumentDTO>() {

                public void onFailure(Throwable caught) {
                    HTMLUtil.showError(caught.getMessage());
                }

                public void onSuccess(MasterDocumentDTO result) {
                    mainPage.refreshElementTable();
                }
            };
            ServiceLocator.getInstance().getExplorerService().moveMDoc((String) userObject[0], mainPage.getWorkspaceId(), doc.getId(), doc.getVersion(), callback);
        }
    }
}
