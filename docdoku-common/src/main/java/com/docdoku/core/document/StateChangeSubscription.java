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

import com.docdoku.core.common.User;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Subscription on the event that is triggered when the state of the workflow
 * attached to the document has changed.  
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="STATECHANGESUBSCRIPTION")
@Entity
@NamedQueries({
    @NamedQuery(name="StateChangeSubscription.findSubscriptionByUserAndDocRevision", query="SELECT s FROM StateChangeSubscription s WHERE s.subscriber = :user AND s.observedDocumentRevision = :docR"),
})
public class StateChangeSubscription extends Subscription{
    

    public StateChangeSubscription() {
    }
    
    public StateChangeSubscription (User pSubscriber, DocumentRevision pObservedElement){
        super(pSubscriber,pObservedElement);
    }
    
}
