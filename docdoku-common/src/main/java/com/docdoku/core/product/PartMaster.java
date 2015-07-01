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
import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * This class holds the unchanging aspects of a part.
 * It contains a collection of part revisions which themselves reference
 * a collection of part iterations which wrap the subsequent changes
 * operated on the part.
 *
 * @author Florent Garin
 * @version 1.1, 18/05/11
 * @since V1.1
 */
@Table(name = "PARTMASTER")
@IdClass(PartMasterKey.class)
@Entity
@NamedQueries({
        @NamedQuery(name = "PartMaster.findByNameOrNumber", query = "SELECT pm FROM PartMaster pm WHERE (pm.name LIKE :partName OR pm.number LIKE :partNumber) AND pm.workspace.id = :workspaceId"),
        @NamedQuery(name = "PartMaster.findByWorkspace", query = "SELECT pm FROM PartMaster pm WHERE pm.workspace.id = :workspaceId ORDER BY pm.creationDate DESC")
})
public class PartMaster implements Serializable {

    @Column(name = "PARTNUMBER", length = 100)
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


    @OrderColumn(name = "ALTERNATE_ORDER")
    @CollectionTable(name = "PARTMASTER_ALTERNATE", joinColumns = {
            @JoinColumn(name = "PARTMASTER_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
            @JoinColumn(name = "PARTMASTER_PARTNUMBER", referencedColumnName = "PARTNUMBER")
    })
    @ElementCollection(fetch = FetchType.LAZY)
    private List<PartAlternateLink> alternates = new LinkedList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date creationDate;

    private String name;

    private String type;


    @OneToMany(mappedBy = "partMaster", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("version ASC")
    private List<PartRevision> partRevisions = new ArrayList<>();

    private boolean standardPart;

    private boolean attributesLocked;

    public PartMaster() {
    }

    public PartMaster(Workspace pWorkspace,
                      String pNumber,
                      User pAuthor) {
        this(pWorkspace, pNumber);
        author = pAuthor;
    }

    public PartMaster(Workspace pWorkspace, String pNumber) {
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
        int index = partRevisions.size() - 1;
        if (index < 0) {
            return null;
        } else {
            return partRevisions.get(index);
        }
    }

    public PartRevision removeLastRevision() {
        int index = partRevisions.size() - 1;
        if (index < 0) {
            return null;
        } else {
            return partRevisions.remove(index);
        }
    }

    public List<PartRevision> getAllReleasedRevisions() {
        List<PartRevision> releasedRevisions = new ArrayList<>();
        for (int index = partRevisions.size() - 1; index >= 0; index--) {
            PartRevision partRevision = partRevisions.get(index);
            if (partRevision.isReleased()) {
                releasedRevisions.add(partRevision);
            }
        }
        return releasedRevisions;
    }

    public PartRevision getLastReleasedRevision() {
        for (int index = partRevisions.size() - 1; index >= 0; index--) {
            PartRevision partRevision = partRevisions.get(index);
            if (partRevision.isReleased()) {
                return partRevision;
            }
        }
        return null;
    }

    public void removeRevision(PartRevision partR) {
        this.partRevisions.remove(partR);
    }

    public PartRevision createNextRevision(User pUser) {
        PartRevision lastRev = getLastRevision();
        Version version;
        if (lastRev == null) {
            version = new Version("A");
        } else {
            version = new Version(lastRev.getVersion());
            version.increase();
        }

        PartRevision rev = new PartRevision(this, version, pUser);
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
        return new PartMasterKey(getWorkspaceId(), number);
    }

    public String getWorkspaceId() {
        return workspace == null ? "" : workspace.getId();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAttributesLocked() {
        return attributesLocked;
    }

    public void setAttributesLocked(boolean attributesLocked) {
        this.attributesLocked = attributesLocked;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof PartMaster)) {
            return false;
        }

        PartMaster partM = (PartMaster) pObj;
        return partM.number.equals(number) &&
                partM.getWorkspaceId().equals(getWorkspaceId());

    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + getWorkspaceId().hashCode();
        hash = 31 * hash + number.hashCode();
        return hash;
    }


    @Override
    public String toString() {
        return number;
    }

}
