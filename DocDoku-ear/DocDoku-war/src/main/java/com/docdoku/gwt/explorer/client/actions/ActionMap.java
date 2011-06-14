/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
        put("ApproveCommand", new ApprouveTaskCommand(mainPage));
        put("RejectCommand", new RejectTaskCommand(mainPage)) ;
    }
    
}
