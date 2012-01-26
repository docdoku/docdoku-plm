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

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
public class IconFactory extends Image {


    private Map<String,Action> m_cmds;
    private final ExplorerImageBundle images = ServiceLocator.getInstance().getExplorerImageBundle();
    private final ExplorerI18NConstants i18n=ServiceLocator.getInstance().getExplorerI18NConstants();
    

    public IconFactory(Map<String,Action> cmds){
        m_cmds=cmds;
    }
    public NewVersionIcon createNewVersionIcon(String workspaceId, String id, String version){
        final NewVersionIcon icon = new NewVersionIcon(workspaceId,id,version);
        icon.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_cmds.get("ShowCreateVersionPanelCommand").execute(icon.workspaceId,icon.id,icon.version);
            }
        });
        return icon;
    }
    public SubscriptionIcon createIterationSubscriptionIcon(boolean subscribe, String workspaceId, String id, String version){
        final SubscriptionIcon icon = new IterationSubscriptionIcon(workspaceId,id,version);
        icon.setSubscribe(subscribe);
        icon.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                m_cmds.get("IterationSubscriptionCommand").execute(icon,!icon.subscribe,icon.workspaceId,icon.id,icon.version);
            }
        });
        return icon;
    }

    public SubscriptionIcon createStateSubscriptionIcon(boolean subscribe, String workspaceId, String id, String version){
        final SubscriptionIcon icon = new StateSubscriptionIcon(workspaceId,id,version);
        icon.setSubscribe(subscribe);
        icon.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                m_cmds.get("StateSubscriptionCommand").execute(icon,!icon.subscribe,icon.workspaceId,icon.id,icon.version);
            }
        });
        return icon;
    }
    public class NewVersionIcon extends Image{
        private String workspaceId;
        private String id;
        private String version;

        private NewVersionIcon(String workspaceId, String id, String version){
            this.workspaceId=workspaceId;
            this.id=id;
            this.version=version;
            AbstractImagePrototype.create(images.documentNewVersionRowIcon()).applyTo(this);
            setTitle(i18n.actionCreateVersion());
        }
    }
    public abstract class SubscriptionIcon extends Image{
        private boolean subscribe;

        private String workspaceId;
        private String id;
        private String version;

        private SubscriptionIcon(String workspaceId, String id, String version){
            this.workspaceId=workspaceId;
            this.id=id;
            this.version=version;
        }
        public void setSubscribe(boolean subscribe){
            this.subscribe = subscribe;
        }
    }
    private class IterationSubscriptionIcon extends SubscriptionIcon{
        private IterationSubscriptionIcon(String workspaceId, String id, String version){
            super(workspaceId,id,version);
        }
        @Override
        public void setSubscribe(boolean subscribe) {
            super.setSubscribe(subscribe);
            if(subscribe){
                AbstractImagePrototype.create(images.alarmIterationOnIcon()).applyTo(this);
                setTitle(i18n.actionIterationNotificationRemove());
            }
            else{
                AbstractImagePrototype.create(images.alarmIterationOffIcon()).applyTo(this);
                setTitle(i18n.actionIterationNotificationAdd());
            }
        }
    }
    private class StateSubscriptionIcon extends SubscriptionIcon{
        private StateSubscriptionIcon(String workspaceId, String id, String version){
            super(workspaceId,id,version);
        }
        @Override
        public void setSubscribe(boolean subscribe) {
            super.setSubscribe(subscribe);
            if(subscribe){
                AbstractImagePrototype.create(images.alarmStateOnIcon()).applyTo(this);
                setTitle(i18n.actionStateNotificationRemove());
            }
            else{
                AbstractImagePrototype.create(images.alarmStateOffIcon()).applyTo(this);
                setTitle(i18n.actionStateNotificationAdd());
            }
        }
    }
}
