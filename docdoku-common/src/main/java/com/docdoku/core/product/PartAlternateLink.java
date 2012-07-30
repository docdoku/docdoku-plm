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
package com.docdoku.core.product;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

/**
 *
 * An Alternate object is a part that is interchangeable with another part
 * with respect to function and physical properties.
 * 
 * Beware that this link is neither transitive nor even bidirectional.
 * That means if we want to express that two parts are alternates of each other
 * we must create two links.
 * 
 * @author Florent Garin
 * @version 1.1, 15/10/11
 * @since   V1.1
 */
@Embeddable
public class PartAlternateLink implements Serializable {

    
    private String referenceDescription;
    private String comment;
    
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="ALTERNATE_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="ALTERNATE_NUMBER", referencedColumnName="NUMBER")})
    private PartMaster alternate;


    public PartAlternateLink() {
    }

    public PartMaster getAlternate() {
        return alternate;
    }

    public void setAlternate(PartMaster alternate) {
        this.alternate = alternate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReferenceDescription() {
        return referenceDescription;
    }

    public void setReferenceDescription(String referenceDescription) {
        this.referenceDescription = referenceDescription;
    }
    
}
