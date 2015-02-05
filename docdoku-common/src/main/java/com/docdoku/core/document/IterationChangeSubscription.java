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
 * A concrete implementation of a subscription.
 * With the use of this class, the user manifests his interest of being informed
 * when a new iteration has been made on a specific document.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="ITERATIONCHANGESUBSCRIPTION")
@Entity
@NamedQueries({
    @NamedQuery(name="IterationChangeSubscription.findSubscriptionByUserAndDocRevision", query="SELECT s FROM IterationChangeSubscription s WHERE s.subscriber = :user AND s.observedDocumentRevision = :docR"),
})
public class IterationChangeSubscription extends Subscription{
    

    public IterationChangeSubscription() {
    }
    
    public IterationChangeSubscription (User pSubscriber, DocumentRevision pObservedElement){
        super(pSubscriber,pObservedElement);
    }
}
