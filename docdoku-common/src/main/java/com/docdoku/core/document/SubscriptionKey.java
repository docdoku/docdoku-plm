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
package com.docdoku.core.document;

import java.io.Serializable;

/**
 *
 * @author Florent Garin
 */
public class SubscriptionKey implements Serializable {

    private String subscriberWorkspaceId;
    private String subscriberLogin;
    private String observedDocumentMasterWorkspaceId;
    private String observedDocumentMasterVersion;
    private String observedDocumentMasterId;

    public SubscriptionKey() {
    }

    public SubscriptionKey(String pSubscriberWorkspaceId, String pSubscriberLogin, String pObservedDocumentMasterWorkspaceId, String pObservedDocumentMasterId, String pObservedDocumentMasterVersion) {
        subscriberWorkspaceId = pSubscriberWorkspaceId;
        subscriberLogin = pSubscriberLogin;
        observedDocumentMasterWorkspaceId = pObservedDocumentMasterWorkspaceId;
        observedDocumentMasterId = pObservedDocumentMasterId;
        observedDocumentMasterVersion = pObservedDocumentMasterVersion;
    }

    public String getObservedDocumentMasterId() {
        return observedDocumentMasterId;
    }

    public String getObservedDocumentMasterVersion() {
        return observedDocumentMasterVersion;
    }

    public String getObservedDocumentMasterWorkspaceId() {
        return observedDocumentMasterWorkspaceId;
    }

    public String getSubscriberLogin() {
        return subscriberLogin;
    }

    public String getSubscriberWorkspaceId() {
        return subscriberWorkspaceId;
    }

    public void setObservedDocumentMasterId(String observedDocumentMasterId) {
        this.observedDocumentMasterId = observedDocumentMasterId;
    }

    public void setObservedDocumentMasterVersion(String observedDocumentMasterVersion) {
        this.observedDocumentMasterVersion = observedDocumentMasterVersion;
    }

    public void setObservedDocumentMasterWorkspaceId(String observedDocumentMasterWorkspaceId) {
        this.observedDocumentMasterWorkspaceId = observedDocumentMasterWorkspaceId;
    }

    public void setSubscriberLogin(String subscriberLogin) {
        this.subscriberLogin = subscriberLogin;
    }

    public void setSubscriberWorkspaceId(String subscriberWorkspaceId) {
        this.subscriberWorkspaceId = subscriberWorkspaceId;
    }

    @Override
    public String toString() {
        return subscriberWorkspaceId + "-" + subscriberLogin + "/" + observedDocumentMasterWorkspaceId + "-" + observedDocumentMasterId + "-" + observedDocumentMasterVersion;
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
        return ((key.subscriberWorkspaceId.equals(subscriberWorkspaceId)) && (key.subscriberLogin.equals(subscriberLogin)) && (key.observedDocumentMasterId.equals(observedDocumentMasterId)) && (key.observedDocumentMasterWorkspaceId.equals(observedDocumentMasterWorkspaceId)) && (key.observedDocumentMasterVersion.equals(observedDocumentMasterVersion)));
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + subscriberWorkspaceId.hashCode();
        hash = 31 * hash + subscriberLogin.hashCode();
        hash = 31 * hash + observedDocumentMasterWorkspaceId.hashCode();
        hash = 31 * hash + observedDocumentMasterId.hashCode();
        hash = 31 * hash + observedDocumentMasterVersion.hashCode();
        return hash;
    }
}
