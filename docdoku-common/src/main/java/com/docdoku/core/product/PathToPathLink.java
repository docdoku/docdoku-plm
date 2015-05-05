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

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by morgan on 29/04/15.
 *
 *
 */

@Table(name="PATHTOPATHLINK")
@Entity
@NamedQueries({
        @NamedQuery(name="PathToPathLink.findPathToPathLinkTypesByProductInstanceIteration", query="SELECT DISTINCT(p.type) FROM PathToPathLink p JOIN ProductInstanceIteration pi WHERE p member of pi.pathToPathLinks AND pi = :productInstanceIteration"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkTypesByProduct", query="SELECT DISTINCT(p.type) FROM PathToPathLink p JOIN ConfigurationItem ci WHERE p member of ci.pathToPathLinks AND ci = :configurationItem"),
        @NamedQuery(name="PathToPathLink.findNextPathToPathLinkInProduct", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ConfigurationItem ci WHERE p member of ci.pathToPathLinks AND ci = :configurationItem AND p.sourcePath = :targetPath AND p.type = :type"),
        @NamedQuery(name="PathToPathLink.findNextPathToPathLinkInProductInstanceIteration", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ProductInstanceIteration pi WHERE p member of pi.pathToPathLinks AND pi = :productInstanceIteration AND p.sourcePath = :targetPath AND p.type = :type"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkBySourceAndTargetInProductInstance", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ProductInstanceIteration pi WHERE p member of pi.pathToPathLinks AND pi = :productInstanceIteration AND (p.sourcePath = :source AND p.targetPath = :target OR p.sourcePath = :target AND p.targetPath = :source)"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkBySourceAndTargetInProduct", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ConfigurationItem ci WHERE p member of ci.pathToPathLinks AND ci = :configurationItem AND (p.sourcePath = :source AND p.targetPath = :target OR p.sourcePath = :target AND p.targetPath = :source)"),
        @NamedQuery(name="PathToPathLink.findSamePathToPathLinkInProduct", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ConfigurationItem ci WHERE p member of ci.pathToPathLinks AND ci = :configurationItem AND p.sourcePath = :sourcePath AND p.targetPath = :targetPath AND p.type = :type"),
        @NamedQuery(name="PathToPathLink.findSamePathToPathLinkInProductInstanceIteration", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ProductInstanceIteration pi WHERE p member of pi.pathToPathLinks AND pi = :productInstanceIteration AND p.sourcePath = :sourcePath AND p.targetPath = :targetPath AND p.type = :type"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinksForGivenProductInstanceIterationAndType", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ProductInstanceIteration pi WHERE p member of pi.pathToPathLinks AND pi = :productInstanceIteration AND p.type = :type"),
        @NamedQuery(name="PathToPathLink.findRootPathToPathLinkForGivenProductInstanceIterationAndType", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ProductInstanceIteration pi WHERE p member of pi.pathToPathLinks AND p.type = :type AND pi = :productInstanceIteration AND p.sourcePath not in (SELECT _p.targetPath FROM PathToPathLink _p WHERE _p member of pi.pathToPathLinks AND _p.type = :type)"),
        @NamedQuery(name="PathToPathLink.findRootPathToPathLinkForGivenProductAndType", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ConfigurationItem ci WHERE p member of ci.pathToPathLinks AND p.type = :type AND ci = :configurationItem AND p.sourcePath not in (SELECT _p.targetPath FROM PathToPathLink _p WHERE _p member of ci.pathToPathLinks AND _p.type = :type)"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkByPathListInProduct", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ConfigurationItem ci WHERE p member of ci.pathToPathLinks AND ci = :configurationItem AND p.sourcePath in :paths AND p.targetPath in :paths"),
        @NamedQuery(name="PathToPathLink.findSourcesPathToPathLinkInProduct", query="SELECT DISTINCT p FROM PathToPathLink p JOIN ConfigurationItem ci WHERE p member of ci.pathToPathLinks AND ci = :configurationItem AND p.sourcePath = :source AND p.type = :type")
})
public class PathToPathLink implements Serializable, Cloneable{

    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Id
    @Column(name="ID")
    private int id;

    private String type;
    private String sourcePath;
    private String targetPath;

    @Lob
    private String description;

    public PathToPathLink() {
    }

    public PathToPathLink(String type, String sourcePath, String targetPath, String description) {
        this.type = type;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String pathFrom) {
        this.sourcePath = pathFrom;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String pathTo) {
        this.targetPath = pathTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public PathToPathLink clone(){
        PathToPathLink clone;
        try {
            clone = (PathToPathLink) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return clone;
    }

}
