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
package com.docdoku.core.document;

import java.io.Serializable;

/**
 *
 * @author Florent Garin
 */
public class SubscriptionKey implements Serializable {

    private String subscriberWorkspaceId;
    private String subscriberLogin;
    private String observedMasterDocumentWorkspaceId;
    private String observedMasterDocumentVersion;
    private String observedMasterDocumentId;

    public SubscriptionKey() {
    }

    public SubscriptionKey(String pSubscriberWorkspaceId, String pSubscriberLogin, String pObservedMasterDocumentWorkspaceId, String pObservedMasterDocumentId, String pObservedMasterDocumentVersion) {
        subscriberWorkspaceId = pSubscriberWorkspaceId;
        subscriberLogin = pSubscriberLogin;
        observedMasterDocumentWorkspaceId = pObservedMasterDocumentWorkspaceId;
        observedMasterDocumentId = pObservedMasterDocumentId;
        observedMasterDocumentVersion = pObservedMasterDocumentVersion;
    }

    public String getObservedMasterDocumentId() {
        return observedMasterDocumentId;
    }

    public String getObservedMasterDocumentVersion() {
        return observedMasterDocumentVersion;
    }

    public String getObservedMasterDocumentWorkspaceId() {
        return observedMasterDocumentWorkspaceId;
    }

    public String getSubscriberLogin() {
        return subscriberLogin;
    }

    public String getSubscriberWorkspaceId() {
        return subscriberWorkspaceId;
    }

    public void setObservedMasterDocumentId(String observedMasterDocumentId) {
        this.observedMasterDocumentId = observedMasterDocumentId;
    }

    public void setObservedMasterDocumentVersion(String observedMasterDocumentVersion) {
        this.observedMasterDocumentVersion = observedMasterDocumentVersion;
    }

    public void setObservedMasterDocumentWorkspaceId(String observedMasterDocumentWorkspaceId) {
        this.observedMasterDocumentWorkspaceId = observedMasterDocumentWorkspaceId;
    }

    public void setSubscriberLogin(String subscriberLogin) {
        this.subscriberLogin = subscriberLogin;
    }

    public void setSubscriberWorkspaceId(String subscriberWorkspaceId) {
        this.subscriberWorkspaceId = subscriberWorkspaceId;
    }

    @Override
    public String toString() {
        return subscriberWorkspaceId + "-" + subscriberLogin + "/" + observedMasterDocumentWorkspaceId + "-" + observedMasterDocumentId + "-" + observedMasterDocumentVersion;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof SubscriptionKey)) {
            return false;
        }
        SubscriptionKey key = (SubscriptionKey) pObj;
        return ((key.subscriberWorkspaceId.equals(subscriberWorkspaceId)) && (key.subscriberLogin.equals(subscriberLogin)) && (key.observedMasterDocumentId.equals(observedMasterDocumentId)) && (key.observedMasterDocumentWorkspaceId.equals(observedMasterDocumentWorkspaceId)) && (key.observedMasterDocumentVersion.equals(observedMasterDocumentVersion)));
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + subscriberWorkspaceId.hashCode();
        hash = 31 * hash + subscriberLogin.hashCode();
        hash = 31 * hash + observedMasterDocumentWorkspaceId.hashCode();
        hash = 31 * hash + observedMasterDocumentId.hashCode();
        hash = 31 * hash + observedMasterDocumentVersion.hashCode();
        return hash;
    }
}
