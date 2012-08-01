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
import com.docdoku.core.common.Workspace;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This class holds the unchanging aspects of a part.
 * It contains a collection of part revisions which themselves reference
 * a collection of part iterations which wrap the subsequent changes
 * operated on the part.
 *
 * @author Florent Garin
 * @version 1.1, 18/05/11
 * @since   V1.1
 */
@IdClass(PartMasterKey.class)
@Entity
public class PartMaster implements Serializable {

    @Column(length = 50)
    @Id
    private String number = "";
    
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;
    
    
    @OrderColumn(name="ALTERNATE_ORDER")
    @CollectionTable(name="PARTMASTER_ALTERNATE",joinColumns={
        @JoinColumn(name="PARTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PARTMASTER_NUMBER", referencedColumnName="NUMBER")
    })    
    @ElementCollection(fetch = FetchType.LAZY)
    private List<PartAlternateLink> alternates = new LinkedList<PartAlternateLink>();
    
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date creationDate;
    
    private String name;
    
    @Lob
    private String description;
    
    
    @OneToMany(mappedBy = "partMaster", cascade = CascadeType.ALL, fetch = FetchType.EAGER)    
    @OrderBy("version ASC")
    private List<PartRevision> partRevisions = new ArrayList<PartRevision>();

    private boolean standardPart;
    
    public PartMaster() {
    }

    public PartMaster(Workspace pWorkspace,
            String pNumber,
            User pAuthor) {
        this(pWorkspace, pNumber);
        author = pAuthor;
    }

    private PartMaster(Workspace pWorkspace, String pNumber) {
        number = pNumber;
        setWorkspace(pWorkspace);
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

  
    public List<PartAlternateLink> getAlternates() {
        return alternates;
    }

    public void setAlternates(List<PartAlternateLink> alternates) {
        this.alternates = alternates;
    }


    public List<PartRevision> getPartRevisions() {
        return partRevisions;
    }

    public void setPartRevisions(List<PartRevision> partRevisions) {
        this.partRevisions = partRevisions;
    }

    public boolean isStandardPart() {
        return standardPart;
    }

    public void setStandardPart(boolean standardPart) {
        this.standardPart = standardPart;
    }
    
        
    public PartRevision getLastRevision() {
        int index = partRevisions.size()-1;
        if(index < 0)
            return null;
        else
            return partRevisions.get(index);
    }
    
    public PartRevision removeLastRevision() {
        int index = partRevisions.size()-1;
        if(index < 0)
            return null;
        else
            return partRevisions.remove(index);
    }
    
    public PartRevision createNextRevision(User pUser){
        PartRevision lastRev=getLastRevision();
        Version version;
        if(lastRev==null)
            version = new Version("A");
        else{
            version = new Version(lastRev.getVersion());
            version.increase();
        }
        
        PartRevision rev = new PartRevision(this,version,pUser);
        partRevisions.add(rev);
        return rev;
    }
    
    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public PartMasterKey getKey() {
        return new PartMasterKey(getWorkspaceId(),number);
    }
        
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkspaceId() {
        return workspace == null ? "" : workspace.getId();
    }
    
    @Override
    public String toString() {
        return number;
    }
}
