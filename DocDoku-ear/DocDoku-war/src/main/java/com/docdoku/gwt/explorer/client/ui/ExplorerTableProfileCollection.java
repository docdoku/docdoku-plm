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

package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.ui.delegates.IterationDelegate;
import com.docdoku.gwt.explorer.client.ui.delegates.DocMIdDelegate;
import com.docdoku.gwt.explorer.client.ui.delegates.DocMTemplateIdDelegate;
import com.docdoku.gwt.explorer.client.ui.delegates.StateDelegate;
import com.docdoku.gwt.explorer.client.ui.delegates.VersionDelegate;
import com.docdoku.gwt.explorer.client.ui.delegates.WorkflowIdDelegate;
import com.docdoku.gwt.client.ui.widget.table.TableDelegate;
import com.docdoku.gwt.client.ui.widget.table.TableProfile;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ExplorerTableProfileCollection {

    private Map<String, TableProfile> profiles;

    public ExplorerTableProfileCollection(IconFactory iconFactory) {
        // create delegates :
        TableDelegate iterationDelegate = new IterationDelegate(iconFactory);
        TableDelegate docMIdDelegate = new DocMIdDelegate();
        TableDelegate docMTemplateIdDelegate = new DocMTemplateIdDelegate();
        TableDelegate stateDelegate = new StateDelegate(iconFactory);
        TableDelegate versionDelegate = new VersionDelegate(iconFactory);
        TableDelegate workflowIdDelegate = new WorkflowIdDelegate();

        profiles = new HashMap<String, TableProfile>();

        // documents profile
        TableProfile documents = new TableProfile();
        documents.setDndEnabled(true);
        documents.setSelectionEnabled(true);
        documents.setDelegate(0, iterationDelegate);
        documents.setDelegate(1, stateDelegate);
        documents.setDelegate(2, docMIdDelegate);
        documents.setDelegate(8, versionDelegate);
        documents.addColumnNotToEmitClick(0);
        documents.addColumnNotToEmitClick(1);
        documents.addColumnNotToEmitClick(8);

        // search profile
        TableProfile search = new TableProfile();
        search.setDndEnabled(true);
        search.setSelectionEnabled(true);
        search.setDelegate(0, iterationDelegate);
        search.setDelegate(1, stateDelegate);
        search.setDelegate(2, docMIdDelegate);
        search.setDelegate(8, versionDelegate);
        search.addColumnNotToEmitClick(0);
        search.addColumnNotToEmitClick(1);
        search.addColumnNotToEmitClick(8);
        search.setStylePrefix("searchTable");

        // templates profile
        TableProfile templates = new TableProfile();
        templates.setSelectionEnabled(true);
        templates.setDndEnabled(false);
        templates.setDelegate(0, docMTemplateIdDelegate);

        // workflow models profile
        TableProfile workflows = new TableProfile();
        workflows.setSelectionEnabled(true);
        workflows.setDndEnabled(false);
        workflows.setDelegate(0, workflowIdDelegate);

        profiles.put("documentsProfile", documents);
        profiles.put("searchProfile", search);
        profiles.put("templatesProfile", templates);
        profiles.put("workflowsProfile", workflows);

    }

    public TableProfile getProfile(String key) {
        return profiles.get(key);
    }
}
