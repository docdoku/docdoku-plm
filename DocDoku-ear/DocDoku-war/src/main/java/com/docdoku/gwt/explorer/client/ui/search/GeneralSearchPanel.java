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

package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.shared.MasterDocumentTemplateDTO;
import com.docdoku.gwt.explorer.shared.UserDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;

public class GeneralSearchPanel extends FlexTable  {

    private TextBox refField;
    private TextBox titleField;
    private TextBox version;
    private ListBox authorList;
    private SuggestBox typeField ;
    private MultiWordSuggestOracle oracle ;
    
    private String workspaceId;

    public GeneralSearchPanel(String workspaceId) {
        this.workspaceId = workspaceId;
        setupUi();
        AsyncCallback<MasterDocumentTemplateDTO[]> callback = new AsyncCallback<MasterDocumentTemplateDTO[]>() {

            @Override
            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }

            @Override
            public void onSuccess(MasterDocumentTemplateDTO[] result) {
                for (MasterDocumentTemplateDTO template : result) {
                    oracle.add(template.getDocumentType());
                }
            }
        };
        ServiceLocator.getInstance().getExplorerService().getMDocTemplates(workspaceId, callback);
    }

    private void setupUi() {
        ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants() ;
        refField = new TextBox();
        setText(0, 0, constants.referenceLabel());
        setWidget(0, 1, refField);
        titleField = new TextBox();
        setText(1, 0, constants.titleLable());
        setWidget(1, 1, titleField);
        oracle = new MultiWordSuggestOracle();
        typeField = new SuggestBox(oracle);


        //typeField.setDataSource(this);
        setText(2, 0, constants.typeLabel());
        setWidget(2, 1, typeField);
        version = new TextBox();
        setText(3, 0, constants.versionLabel());
        setWidget(3, 1, version);
        authorList = new ListBox();
        setText(4, 0, constants.authorLabel());
        setWidget(4, 1, authorList);

        // async calls :
        AsyncCallback<UserDTO[]> callback = new AsyncCallback<UserDTO[]>() {

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }

            public void onSuccess(UserDTO[] result) {
                authorList.addItem(ServiceLocator.getInstance().getExplorerI18NConstants().notSpecifiedOption());
                for (UserDTO u : result) {
                    authorList.addItem(u.getName(), u.getLogin());
                }
            }
        };
        ServiceLocator.getInstance().getExplorerService().getUsers(workspaceId, callback);

    }

    public String getReference() {
        return refField.getText();
    }

    public String getTitleField() {
        return titleField.getText();
    }

    public String getAuthor() {
        if (!authorList.getItemText(authorList.getSelectedIndex()).equals(ServiceLocator.getInstance().getExplorerI18NConstants().notSpecifiedOption())) {
            return authorList.getValue(authorList.getSelectedIndex());
        } else {
            return null;
        }

    }

    public String getType() {
        if(typeField.getText().trim().isEmpty()){
            return null;
        }
        return typeField.getText().trim();
    }

    public String getVersion() {
        if (version.getText().trim().isEmpty()){
            return null ;
        }
        return version.getText().trim();
    }



    @Override
    public void setVisible(boolean visible) {
        
        if (visible) {
            AsyncCallback<MasterDocumentTemplateDTO[]> callback = new AsyncCallback<MasterDocumentTemplateDTO[]>() {

                public void onFailure(Throwable caught) {
                    HTMLUtil.showError(caught.getMessage());
                }

                public void onSuccess(MasterDocumentTemplateDTO[] result) {
                    for (MasterDocumentTemplateDTO template : result) {
                        oracle.add(template.getDocumentType());
                    }
                }
            };
            oracle.clear();
            ServiceLocator.getInstance().getExplorerService().getMDocTemplates(workspaceId, callback);
            super.setVisible(visible);
        }
    }
}
