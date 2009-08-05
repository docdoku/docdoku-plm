/*
 * ExplorerConstants.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.gwt.explorer.client.data;

import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.docdoku.gwt.explorer.common.UserDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ExplorerConstants {

    private UserDTO[] workspaceUsers ;

    private UserDTO user ;

    static private ExplorerConstants instance ;

    static public void init(String workspaceId){
        instance = new ExplorerConstants(workspaceId);
    }

    private ExplorerConstants(final String workspaceId){
        AsyncCallback<UserDTO> callback = new AsyncCallback<UserDTO>() {

            public void onFailure(Throwable caught) {
                HTMLUtil.showError(caught.getMessage());
            }

            public void onSuccess(UserDTO result) {
                user = result ;
                AsyncCallback<UserDTO[]> usersCallback = new AsyncCallback<UserDTO[]>() {

                    public void onFailure(Throwable caught) {
                        HTMLUtil.showError(caught.getMessage());
                    }

                    public void onSuccess(UserDTO[] result) {
                        workspaceUsers = result;
                    }
                };
                ServiceLocator.getInstance().getExplorerService().getUsers(workspaceId, usersCallback);
            }
        };
        ServiceLocator.getInstance().getExplorerService().whoAmI(workspaceId, callback);
    }

    public static ExplorerConstants getInstance() {
        return instance;
    }

    public UserDTO getUser() {
        return user;
    }

    public UserDTO[] getWorkspaceUsers() {
        return workspaceUsers;
    }

    

}
