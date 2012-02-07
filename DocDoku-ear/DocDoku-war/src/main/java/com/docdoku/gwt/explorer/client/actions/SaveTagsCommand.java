/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.ui.ExplorerPage;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.server.rest.dto.DocumentMasterDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Florent GARIN
 */
public class SaveTagsCommand implements Action {

    private ExplorerPage m_mainPage;
    private int nbCalls ;

    public SaveTagsCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
        nbCalls = 0 ;
    }

    public void execute(Object... userObject) {

        final int nbSelectedDocs = m_mainPage.getSelectedDocMs().size() ;
        nbCalls = 0 ;
        for (DocumentMasterDTO docM : m_mainPage.getSelectedDocMs()) {
            String workspaceId = docM.getWorkspaceId();
            String id = docM.getId();
            String version = docM.getVersion();

            final boolean isNew=(Boolean)userObject[0];
            boolean add=(Boolean)userObject[1];
            String tag=(String)userObject[2];

            Set<String> setTags=new HashSet<String>();
            Collections.addAll(setTags, docM.getTags());
            
            if(add)
                setTags.add(tag);
            else
                setTags.remove(tag);

            AsyncCallback<DocumentMasterDTO> callback = new AsyncCallback<DocumentMasterDTO>() {

                public void onSuccess(DocumentMasterDTO docM) {
                    nbCalls++ ;
                    m_mainPage.refreshElementTable();
                    if(isNew && nbCalls == nbSelectedDocs)
                        m_mainPage.refreshTags();
                }

                public void onFailure(Throwable caught) {
                    HTMLUtil.showError(caught.getMessage());
                }
            };
            ServiceLocator.getInstance().getExplorerService().saveTags(workspaceId, id, version, setTags.toArray(new String[setTags.size()]), callback);
        }
    }
}
