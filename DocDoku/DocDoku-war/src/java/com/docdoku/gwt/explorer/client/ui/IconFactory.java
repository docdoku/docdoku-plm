/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import java.util.Map;

/**
 *
 * @author Florent GARIN
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
            images.documentNewVersionRowIcon().applyTo(this);
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
                images.alarmIterationOnIcon().applyTo(this);
                setTitle(i18n.actionIterationNotificationRemove());
            }
            else{
                images.alarmIterationOffIcon().applyTo(this);
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
                images.alarmStateOnIcon().applyTo(this);
                setTitle(i18n.actionStateNotificationRemove());
            }
            else{
                images.alarmStateOffIcon().applyTo(this);
                setTitle(i18n.actionStateNotificationAdd());
            }
        }
    }
}
