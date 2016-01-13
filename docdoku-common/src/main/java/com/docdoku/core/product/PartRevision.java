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
package com.docdoku.core.product;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Version;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.security.ACL;
import com.docdoku.core.workflow.Workflow;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.*;

/**
 * This class stands between {@link PartMaster} and {@link PartIteration}.
 * Its main purpose is to hold effectivities. It represents a formal revision of a part.
 *
 * @author Florent Garin
 * @version 1.1, 31/10/11
 * @since   V1.1
 */
@Table(name="PARTREVISION")
@IdClass(PartRevisionKey.class)
@Entity
@NamedQueries({
        @NamedQuery(name="PartRevision.findByWorkspace", query="SELECT pr FROM PartRevision pr WHERE pr.partMaster.workspace.id = :workspaceId ORDER BY pr.partMaster.number ASC"),
        @NamedQuery(name="PartRevision.findByWorkspace.filterUserACLEntry", query="SELECT pr FROM PartRevision pr WHERE pr.partMaster.workspace.id = :workspaceId and (pr.acl is null or exists(SELECT au from ACLUserEntry au WHERE au.principal = :user AND au.permission not like com.docdoku.core.security.ACL.Permission.FORBIDDEN AND au.acl = pr.acl)) ORDER BY pr.partMaster.number ASC"),
        @NamedQuery(name="PartRevision.countByWorkspace.filterUserACLEntry", query="SELECT count(pr) FROM PartRevision pr WHERE pr.partMaster.workspace.id = :workspaceId and (pr.acl is null or exists(SELECT au from ACLUserEntry au WHERE au.principal = :user AND au.permission not like com.docdoku.core.security.ACL.Permission.FORBIDDEN AND au.acl = pr.acl))"),
        @NamedQuery(name="PartRevision.countByWorkspace", query="SELECT count(pr) FROM PartRevision pr WHERE pr.partMasterWorkspaceId = :workspaceId"),
        @NamedQuery(name="PartRevision.findByReferenceOrName", query="SELECT pr FROM PartRevision pr WHERE (pr.partMaster.number LIKE :partNumber OR pr.partMaster.name LIKE :partName) AND pr.partMaster.workspace.id = :workspaceId")
})
public class PartRevision implements Serializable, Comparable<PartRevision> {


    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTNUMBER"),
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
    @JoinTable(name="PARTREVISION_EFFECTIVITY",
    inverseJoinColumns={
        @JoinColumn(name="EFFECTIVITY_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="PARTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
        @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="VERSION")
    })
    private Set<Effectivity> effectivities = new HashSet<>();
    
    
    @OneToMany(mappedBy = "partRevision", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("iteration ASC")
    private List<PartIteration> partIterations = new ArrayList<>();
   
 
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

    @OneToMany(orphanRemoval=true, cascade= CascadeType.ALL, fetch= FetchType.EAGER)
    @JoinTable(name="PART_ABORTED_WORKFLOW",
        inverseJoinColumns={
                @JoinColumn(name="WORKFLOW_ID", referencedColumnName="ID")
        },
        joinColumns={
                @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
                @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="VERSION"),
                @JoinColumn(name="PARTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private List<Workflow> abortedWorkflows=new ArrayList<>();

    @Column(name = "PARTMASTER_PARTNUMBER", nullable = false, insertable = false, updatable = false)
    private String partMasterNumber="";

    @Column(name = "WORKSPACE_ID", nullable = false, insertable = false, updatable = false)
    private String partMasterWorkspaceId="";

    @OneToOne(orphanRemoval = true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private ACL acl;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="PARTREVISION_TAG",
            inverseJoinColumns={
                    @JoinColumn(name="TAG_LABEL", referencedColumnName="LABEL"),
                    @JoinColumn(name="TAG_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            },
            joinColumns={
                    @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
                    @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="VERSION"),
                    @JoinColumn(name="PARTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            })
    private Set<Tag> tags=new HashSet<>();

    private boolean publicShared;

    private RevisionStatus status=RevisionStatus.WIP;



    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name="statusModificationDate",
                    column=@Column(name="RELEASE_DATE"))
    })
    @AssociationOverrides({
            @AssociationOverride(
                    name="statusChangeAuthor",
                    joinColumns={
                            @JoinColumn(name="RELEASE_USER_LOGIN", referencedColumnName = "LOGIN"),
                            @JoinColumn(name="RELEASE_USER_WORKSPACE", referencedColumnName = "WORKSPACE_ID")
                    })
    })
    private StatusChange releaseStatusChange;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name="statusModificationDate",
                    column=@Column(name="OBSOLETE_DATE"))
    })
    @AssociationOverrides({
            @AssociationOverride(
                    name="statusChangeAuthor",
                    joinColumns={
                            @JoinColumn(name="OBSOLETE_USER_LOGIN", referencedColumnName = "LOGIN"),
                            @JoinColumn(name="OBSOLETE_USER_WORKSPACE", referencedColumnName = "WORKSPACE_ID")
                    })
    })
    private StatusChange obsoleteStatusChange;

    public enum RevisionStatus {
        WIP, RELEASED, OBSOLETE
    }

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
        setPartMasterNumber(partMaster.getNumber());
        setPartMasterWorkspaceId(partMaster.getWorkspaceId());
    }

    @XmlTransient
    public PartMaster getPartMaster() {
        return partMaster;
    }
    
    public PartRevisionKey getKey() {
        return new PartRevisionKey(getPartMasterKey(),version);
    }

    public boolean isCheckedOut() {
        return checkOutUser != null;
    }
    
    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return (creationDate!=null) ? (Date) creationDate.clone() : null;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public Date getCheckOutDate() {
        return (checkOutDate!=null) ? (Date) checkOutDate.clone() : null;
    }
    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = (checkOutDate!=null) ? (Date) checkOutDate.clone() : null;
    }

    public User getCheckOutUser() {
        return checkOutUser;
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

    public String getType() {
        return partMaster.getType();
    }

    public Workflow getWorkflow() {
        return workflow;
    }
    public void setWorkflow(Workflow pWorkflow) {
        workflow = pWorkflow;
    }
    
    public String getLifeCycleState() {
        if (workflow != null) {
            return workflow.getLifeCycleState();
        }else {
            return null;
        }
    }
    
    public boolean hasWorkflow() {
        return workflow != null;
    }
    
    public ACL getACL() {
        return acl;
    }
    public void setACL(ACL acl) {
        this.acl = acl;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> pTags) {
       if (pTags != null){
           tags.retainAll(pTags);
           pTags.removeAll(tags);
           tags.addAll(pTags);
       }

    }

    public boolean addTag(Tag pTag) {
        return tags.add(pTag);
    }
    public boolean removeTag(Tag pTag){
        return tags.remove(pTag);
    }
    public List<Workflow> getAbortedWorkflows() {
        return abortedWorkflows;
    }
    public void addAbortedWorkflows(Workflow abortedWorkflow) {
        this.abortedWorkflows.add(abortedWorkflow);
    }
    
    public PartIteration createNextIteration(User pUser){
        PartIteration part = new PartIteration(this, pUser);
        partIterations.add(part);
        return part;
    }

    public List<PartIteration> getPartIterations() {
        return partIterations;
    }
    public void setPartIterations(List<PartIteration> partIterations) {
        this.partIterations = partIterations;
    }

    public PartIteration getLastIteration() {
        int index = partIterations.size()-1;
        if(index < 0) {
            return null;
        } else {
            return partIterations.get(index);
        }
    }

    public PartIteration getLastCheckedInIteration() {
        int index;
        if(isCheckedOut()){
            index = partIterations.size()-2;
        }else{
            index = partIterations.size()-1;
        }
        if(index < 0) {
           return null;
        }else {
            return partIterations.get(index);
        }
    }

    public int getLastIterationNumber() {
        if(this.getLastIteration()!=null){
            return this.getLastIteration().getIteration();
        }else{
            return 0;
        }
    }
    
    public PartIteration removeLastIteration() {
        int index = partIterations.size()-1;
        if(index < 0) {
            return null;
        }else {
            return partIterations.remove(index);
        }
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

    public String getPartName() {
        return partMaster==null?"":partMaster.getName();
    }


    public String getPartMasterNumber() {
        return partMasterNumber;
    }

    public void setPartMasterNumber(String pPartMasterNumber) {
        partMasterNumber=pPartMasterNumber;
    }

    public String getPartMasterWorkspaceId() {
        return partMasterWorkspaceId;
    }

    public void setPartMasterWorkspaceId(String pPartMasterWorkspaceId) {
        partMasterWorkspaceId = pPartMasterWorkspaceId;
    }

    public Set<Effectivity> getEffectivities() {
        return effectivities;
    }
    public void setEffectivities(Set<Effectivity> effectivities) {
        this.effectivities = effectivities;
    }

    public boolean isPublicShared() {
        return publicShared;
    }
    public void setPublicShared(boolean publicShared) {
        this.publicShared = publicShared;
    }

    public RevisionStatus getStatus() {
        return status;
    }
    public void setStatus(RevisionStatus status) {
        this.status = status;
    }

    public boolean isReleased(){
        return status==RevisionStatus.RELEASED;
    }
    public boolean isObsolete(){
        return status==RevisionStatus.OBSOLETE;
    }
    public boolean release(User user){
        if(this.status==RevisionStatus.WIP){
            this.status=RevisionStatus.RELEASED;
            StatusChange statusChange = new StatusChange();
            statusChange.setStatusChangeAuthor(user);
            statusChange.setStatusModificationDate(new Date());
            this.setReleaseStatusChange(statusChange);
            return true;
        }else{
            return false;
        }

    }
    public boolean markAsObsolete(User user){
        if(this.status==RevisionStatus.RELEASED){
            this.status=RevisionStatus.OBSOLETE;
            StatusChange statusChange = new StatusChange();
            statusChange.setStatusChangeAuthor(user);
            statusChange.setStatusModificationDate(new Date());
            this.setObsoleteStatusChange(statusChange);
            return true;
        }else{
            return false;
        }

    }

    public boolean isAttributesLocked(){
        if (this.partMaster != null){
            return this.partMaster.isAttributesLocked();
        }
        return false;
    }

    public StatusChange getObsoleteStatusChange() {
        return obsoleteStatusChange;
    }

    public void setObsoleteStatusChange(StatusChange statusChange) {
        this.obsoleteStatusChange = statusChange;
    }

    public StatusChange getReleaseStatusChange() {
        return releaseStatusChange;
    }

    public void setReleaseStatusChange(StatusChange statusChange) {
        this.releaseStatusChange = statusChange;
    }

    public User getObsoleteAuthor() {
        return obsoleteStatusChange == null ? null : obsoleteStatusChange.getStatusChangeAuthor();
    }

    public Date getObsoleteDate() {
        return obsoleteStatusChange == null ? null : obsoleteStatusChange.getStatusModificationDate();
    }
    public User getReleaseAuthor() {
        return releaseStatusChange == null ? null : releaseStatusChange.getStatusChangeAuthor();
    }

    public Date getReleaseDate() {
        return releaseStatusChange == null ? null : releaseStatusChange.getStatusModificationDate();
    }

    @Override
    public String toString() {
        return getPartNumber() + "-" + version;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof PartRevision)) {
            return false;
        }
        PartRevision partR = (PartRevision) pObj;
        return partR.getPartNumber().equals(getPartNumber()) && partR.getWorkspaceId().equals(getWorkspaceId()) && partR.version.equals(version);
        
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + getWorkspaceId().hashCode();
        hash = 31 * hash + getPartNumber().hashCode();
        hash = 31 * hash + version.hashCode();
        return hash;
    }

    @Override
    public int compareTo(PartRevision pPartR) {
        int wksComp = getWorkspaceId().compareTo(pPartR.getWorkspaceId());
        if (wksComp != 0) {
            return wksComp;
        }
        int numberComp = getPartNumber().compareTo(pPartR.getPartNumber());
        if (numberComp != 0) {
            return numberComp;
        } else {
            return version.compareTo(pPartR.version);
        }
    }

    public PartIteration getLastAccessibleIteration(User user) {
        if(isCheckoutByAnotherUser(user)) {
            return partIterations.size() <= 1 ? null : partIterations.get(partIterations.size() -2);
        } else {
            return getLastIteration();
        }
    }

    private boolean isCheckoutByAnotherUser(User user) {
        return isCheckedOut() && !getCheckOutUser().equals(user);
    }
}
