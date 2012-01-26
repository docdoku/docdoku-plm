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
import com.docdoku.gwt.explorer.client.ui.IconFactory.SubscriptionIcon;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Florent GARIN
 */
public class StateSubscriptionCommand implements Action {

    private ExplorerPage m_mainPage;

    public StateSubscriptionCommand(ExplorerPage mainPage) {
        m_mainPage = mainPage;
    }

    @Override
    public void execute(Object... userObject) {
            final SubscriptionIcon icon=(SubscriptionIcon) userObject[0];
            final boolean subscribe= (Boolean)userObject[1];
            String workspaceId= (String) userObject[2];
            String id = (String) userObject[3];
            String version = (String) userObject[4];
            AsyncCallback<Void> callback = new AsyncCallback<Void>() {

            @Override
                public void onSuccess(Void result) {
                    icon.setSubscribe(subscribe);
                }

            @Override
                public void onFailure(Throwable caught) {
                    HTMLUtil.showError(caught.getMessage());
                }


            };
            if(subscribe)
                ServiceLocator.getInstance().getExplorerService().subscribeToStateChangeEvent(workspaceId, id, version, callback);
            else
                ServiceLocator.getInstance().getExplorerService().unsubscribeToStateChangeEvent(workspaceId, id, version, callback);

    }
}
