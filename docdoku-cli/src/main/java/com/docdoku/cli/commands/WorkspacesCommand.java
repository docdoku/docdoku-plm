/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.cli.commands;

import com.docdoku.cli.ScriptingTools;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.services.IUserManagerWS;
import org.codehaus.jettison.json.JSONArray;
import org.kohsuke.args4j.Option;

public class WorkspacesCommand extends AbstractCommandLine{

    @Option(name="-j", aliases = "--jsonparser", usage="return the list of the workspaces in JSON format")
    private boolean jsonParser;

    private IUserManagerWS userS;

    public void execImpl() throws Exception {

        userS = ScriptingTools.createUserManagerService(getServerURL(), user, password);
        Workspace[] workspaces = userS.getWorkspacesWhereCallerIsActive();

        if(jsonParser){
            JSONArray wks = new JSONArray();
            for(int i = 0 ; i < workspaces.length; i++){
                wks.put(i,workspaces[i].getId());
            }
            System.out.println(wks.toString());
        }else{
            for(int i = 0 ; i < workspaces.length; i++){
                System.out.println(workspaces[i].getId());
            }
        }

    }

    @Override
    public String getDescription() {
        return "List all workspaces the user belongs.";
    }
}
