/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.actions;

import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import java.util.HashMap;

/**
 *
 * @author Florent GARIN
 */
public class ActionMap extends HashMap<String,Action>{

    
    public ActionMap(){
        
    }
    
    public void init(ExplorerPage mainPage){
        put("CheckOutCommand", new CheckOutCommand(mainPage));
        put("CheckInCommand", new CheckInCommand(mainPage));
        put("UndoCheckOutCommand", new UndoCheckOutCommand(mainPage));
        put("DeleteElementCommand", new DeleteElementCommand(mainPage));
        put("CreateFolderCommand", new CreateFolderCommand(mainPage));
        put("BackCommand", new BackCommand(mainPage));
        put("CreateMDocCommand", new CreateMDocCommand(mainPage));
        put("CreateMDocTemplateCommand", new CreateMDocTemplateCommand(mainPage));
        put("UpdateMDocTemplateCommand", new UpdateMDocTemplateCommand(mainPage));
        put("CreateVersionCommand", new CreateVersionCommand(mainPage));
        put("DeleteDocFileCommand", new DeleteDocFileCommand(mainPage));
        put("DeleteTemplateFileCommand", new DeleteTemplateFileCommand(mainPage));
        put("UploadDocFileCommand", new UploadDocFileCommand(mainPage));
        put("UploadTemplateFileCommand", new UploadTemplateFileCommand(mainPage));
        put("UploadCompleteDocFileCommand", new UploadCompleteDocFileCommand(mainPage));
        put("UploadCompleteTemplateFileCommand", new UploadCompleteTemplateFileCommand(mainPage));
        put("EditElementCommand", new EditElementCommand(mainPage));
        put("IterationSubscriptionCommand", new IterationSubscriptionCommand(mainPage));
        put("StateSubscriptionCommand", new StateSubscriptionCommand(mainPage));
        put("SaveTagsCommand", new SaveTagsCommand(mainPage));
        put("SaveWorkflowModelCommand", new SaveWorkflowModelCommand(mainPage));
        put("SearchCommand", new SearchCommand(mainPage));
        put("MoveCommand", new MoveMDocCommand(mainPage)) ;
        put("ShowIterationCommand", new ShowDocumentIterationCommand(mainPage));
        put("ShowCreateVersionPanelCommand", new ShowCreateVersionPanelCommand(mainPage)) ;
    }
    
}
