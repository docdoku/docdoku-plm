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

import com.docdoku.core.common.User;
import com.docdoku.core.common.Version;
import com.docdoku.core.workflow.Workflow;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class stands between <code>PartMaster</code>
 * and <code>PartIteration</code>.
 * Its main purpose is to hold effectivities.
 *
 * @author Florent Garin
 * @version 1.1, 31/10/11
 * @since   V1.1
 */
@IdClass(PartRevisionKey.class)
@Entity
public class PartRevision implements Serializable {


    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="PARTMASTER_NUMBER", referencedColumnName="NUMBER"),
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private PartMaster partMaster;


    @Column(length=10)
    @Id
    private String version="";

    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;
       
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date creationDate;


    @Lob
    private String description;
    
    @OneToMany(orphanRemoval=true, cascade= CascadeType.ALL, fetch= FetchType.EAGER)
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="EFFECTIVITY_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="PARTREVISION_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PARTREVISION_PARTMASTER_NUMBER", referencedColumnName="PARTMASTER_NUMBER"),
        @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="VERSION")
    })
    private Set<Effectivity> effectivities = new HashSet<Effectivity>();
    
    
    @OneToMany(mappedBy = "partRevision", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("iteration ASC")
    private List<PartIteration> partIterations = new ArrayList<PartIteration>();
   
 
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="CHECKOUTUSER_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="CHECKOUTUSER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User checkOutUser;
    
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date checkOutDate;
    
    @OneToOne(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private Workflow workflow;

    public PartRevision(){

    }

    public PartRevision(PartMaster pPartMaster,
            String pStringVersion,
            User pAuthor) {
        this(pPartMaster);
        version=pStringVersion;
        author = pAuthor;
    }

    public PartRevision(PartMaster pPartMaster,
            Version pVersion,
            User pAuthor) {
        this(pPartMaster);
        version=pVersion.toString();
        author = pAuthor;
    }

    public PartRevision(PartMaster pPartMaster, User pAuthor) {
        this(pPartMaster);
        version = new Version().toString();
        author = pAuthor;
    }

    private PartRevision(PartMaster pPartMaster) {
        setPartMaster(pPartMaster);
    }

    public void setPartMaster(PartMaster partMaster) {
        this.partMaster = partMaster;
    }

    @XmlTransient
    public PartMaster getPartMaster() {
        return partMaster;
    }
    
    public PartRevisionKey getKey() {
        return new PartRevisionKey(getPartMasterKey(),version);
    }
    
    public boolean isCheckedOut() {
        return (checkOutUser != null);
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

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public User getCheckOutUser() {
        return checkOutUser;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public void setCheckOutUser(User checkOutUser) {
        this.checkOutUser = checkOutUser;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Workflow getWorkflow() {
        return workflow;
    }
    
    public void setWorkflow(Workflow pWorkflow) {
        workflow = pWorkflow;
    }
    
    public String getLifeCycleState() {
        if (workflow != null)
            return workflow.getLifeCycleState();
        else
            return null;
    }
    
    public boolean hasWorkflow() {
        return (workflow != null);
    }
    
    
    public PartIteration createNextIteration(User pUser){
        PartIteration lastPart=getLastIteration();
        int iteration = lastPart==null?1:lastPart.getIteration() + 1;
        PartIteration part = new PartIteration(this,iteration,pUser);
        partIterations.add(part);
        return part;
    }

    
    public void setPartIterations(List<PartIteration> partIterations) {
        this.partIterations = partIterations;
    }
    

    public List<PartIteration> getPartIterations() {
        return partIterations;
    }
    
    public PartIteration getLastIteration() {
        int index = partIterations.size()-1;
        if(index < 0)
            return null;
        else
            return partIterations.get(index);
    }
    
    public PartIteration removeLastIteration() {
        int index = partIterations.size()-1;
        if(index < 0)
            return null;
        else
            return partIterations.remove(index);
    }
    
    public PartIteration getIteration(int pIteration) {
        return partIterations.get(pIteration-1);
    }
    
    public int getNumberOfIterations() {
        return partIterations.size();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }  

    public PartMasterKey getPartMasterKey() {
        return partMaster==null?new PartMasterKey("",""):partMaster.getKey();
    }
    
    public String getWorkspaceId() {
        return partMaster==null?"":partMaster.getWorkspaceId();
    }
    
    public String getPartNumber() {
        return partMaster==null?"":partMaster.getNumber();
    }

    public Set<Effectivity> getEffectivities() {
        return effectivities;
    }

    public void setEffectivities(Set<Effectivity> effectivities) {
        this.effectivities = effectivities;
    }
    
    
}
