/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
    private String observedDocumentRevisionWorkspaceId;
    private String observedDocumentRevisionVersion;
    private String observedDocumentRevisionId;

    public SubscriptionKey() {
    }

    public SubscriptionKey(String pSubscriberWorkspaceId, String pSubscriberLogin, String pObservedDocumentRevisionWorkspaceId, String pObservedDocumentRevisionId, String pObservedDocumentRevisionVersion) {
        subscriberWorkspaceId = pSubscriberWorkspaceId;
        subscriberLogin = pSubscriberLogin;
        observedDocumentRevisionWorkspaceId = pObservedDocumentRevisionWorkspaceId;
        observedDocumentRevisionId = pObservedDocumentRevisionId;
        observedDocumentRevisionVersion = pObservedDocumentRevisionVersion;
    }

    public String getObservedDocumentRevisionId() {
        return observedDocumentRevisionId;
    }

    public String getObservedDocumentRevisionVersion() {
        return observedDocumentRevisionVersion;
    }

    public String getObservedDocumentRevisionWorkspaceId() {
        return observedDocumentRevisionWorkspaceId;
    }

    public String getSubscriberLogin() {
        return subscriberLogin;
    }

    public String getSubscriberWorkspaceId() {
        return subscriberWorkspaceId;
    }

    public void setObservedDocumentRevisionId(String observedDocumentRevisionId) {
        this.observedDocumentRevisionId = observedDocumentRevisionId;
    }

    public void setObservedDocumentRevisionVersion(String observedDocumentRevisionVersion) {
        this.observedDocumentRevisionVersion = observedDocumentRevisionVersion;
    }

    public void setObservedDocumentRevisionWorkspaceId(String observedDocumentRevisionWorkspaceId) {
        this.observedDocumentRevisionWorkspaceId = observedDocumentRevisionWorkspaceId;
    }

    public void setSubscriberLogin(String subscriberLogin) {
        this.subscriberLogin = subscriberLogin;
    }

    public void setSubscriberWorkspaceId(String subscriberWorkspaceId) {
        this.subscriberWorkspaceId = subscriberWorkspaceId;
    }

    @Override
    public String toString() {
        return subscriberWorkspaceId + "-" + subscriberLogin + "/" + observedDocumentRevisionWorkspaceId + "-" + observedDocumentRevisionId + "-" + observedDocumentRevisionVersion;
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
        return key.subscriberWorkspaceId.equals(subscriberWorkspaceId) && key.subscriberLogin.equals(subscriberLogin) && key.observedDocumentRevisionId.equals(observedDocumentRevisionId) && key.observedDocumentRevisionWorkspaceId.equals(observedDocumentRevisionWorkspaceId) && key.observedDocumentRevisionVersion.equals(observedDocumentRevisionVersion);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + subscriberWorkspaceId.hashCode();
        hash = 31 * hash + subscriberLogin.hashCode();
        hash = 31 * hash + observedDocumentRevisionWorkspaceId.hashCode();
        hash = 31 * hash + observedDocumentRevisionId.hashCode();
        hash = 31 * hash + observedDocumentRevisionVersion.hashCode();
        return hash;
    }
}
