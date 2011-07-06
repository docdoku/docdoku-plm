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
package com.docdoku.core.product;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Version;
import com.docdoku.core.common.Workspace;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This class holds the unchanging aspects of a part.
 * It contains a collection of parts which wraps the subsequent changes
 * operated on the part.
 *
 * @author Florent Garin
 * @version 1.1, 18/05/11
 * @since   V1.1
 */
@IdClass(MasterPartKey.class)
@Entity
public class MasterPart implements Serializable {



    @Column(length=50)
    @Id
    private String number="";

    @Column(length=10)
    @Id
    private String version="";

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;

    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @ManyToMany
    private Set<MasterPart> alternatives;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date creationDate;

    private String name;

    @Lob
    private String description;

    
    @OneToMany(mappedBy = "masterPart", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("iteration ASC")
    private List<Part> partIterations = new ArrayList<Part>();
   
 

    public MasterPart(){

    }

    public MasterPart(Workspace pWorkspace,
            String pNumber,
            String pStringVersion,
            User pAuthor) {
        this(pWorkspace, pNumber);
        version=pStringVersion;
        author = pAuthor;
    }

    public MasterPart(Workspace pWorkspace, String pNumber,
            Version pVersion,
            User pAuthor) {
        this(pWorkspace, pNumber);
        version=pVersion.toString();
        author = pAuthor;
    }

    public MasterPart(Workspace pWorkspace, String pNumber, User pAuthor) {
        this(pWorkspace, pNumber);
        version = new Version().toString();
        author = pAuthor;
    }

    private MasterPart(Workspace pWorkspace, String pNumber) {
        number=pNumber;
        setWorkspace(pWorkspace);
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Set<MasterPart> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(Set<MasterPart> alternatives) {
        this.alternatives = alternatives;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getWorkspaceId(){
        return workspace==null?"":workspace.getId();
    }
    
}
