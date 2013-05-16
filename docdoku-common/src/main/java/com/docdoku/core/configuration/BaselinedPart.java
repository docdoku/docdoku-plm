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

package com.docdoku.core.configuration;


import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.sharing.SharedEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;

/**
 * Class link that gathers a baseline and a given part iteration.
 *
 * @author Florent Garin
 * @version 2.0, 15/05/13
 * @since   V2.0
 */

@Table(name="BASELINEDPART")
@Entity
public class BaselinedPart implements Serializable{

    @EmbeddedId
    private BaselinedPartKey baselinedPartKey;

    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="BASELINE_ID", referencedColumnName="ID")
    private Baseline baseline;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name="TARGET_ITERATION", referencedColumnName="ITERATION"),
            @JoinColumn(name="TARGET_PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
            @JoinColumn(name="TARGET_PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
            @JoinColumn(name="TARGET_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private PartIteration targetPart;


    @Column(name = "TARGET_ITERATION", nullable = false, insertable = false, updatable = false)
    private int targetPartIteration;

    @Column(name = "TARGET_PARTREVISION_VERSION", length=10, nullable = false, insertable = false, updatable = false)
    private String targetPartVersion="";



    @Column(name="COMMENTDATA")
    private String comment;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    public BaselinedPart(){
    }

    @XmlTransient
    public Baseline getBaseline() {
        return baseline;
    }

    public void setBaseline(Baseline baseline) {
        this.baseline = baseline;
    }

    public PartIteration getTargetPart() {
        return targetPart;
    }

    public void setTargetPart(PartIteration targetPart) {
        this.targetPart = targetPart;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public String getTargetPartVersion() {
        return targetPartVersion;
    }

    public void setTargetPartVersion(String targetPartVersion) {
        this.targetPartVersion = targetPartVersion;
    }


    public int getTargetPartIteration() {
        return targetPartIteration;
    }

    public void setTargetPartIteration(int targetPartIteration) {
        this.targetPartIteration = targetPartIteration;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaselinedPart)) return false;

        BaselinedPart that = (BaselinedPart) o;

        if (!baselinedPartKey.equals(that.baselinedPartKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return baselinedPartKey.hashCode();
    }
}
