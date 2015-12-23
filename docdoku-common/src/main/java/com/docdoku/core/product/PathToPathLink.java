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
 * @author Morgan Guimard
 * @version 2.0, 29/04/15
 * @since   V2.0
 */
@Table(name="PATHTOPATHLINK")
@Entity
@NamedQueries({
        @NamedQuery(name="PathToPathLink.findPathToPathLinkTypesByProductInstanceIteration", query="SELECT DISTINCT(p.type) FROM ProductInstanceIteration pi JOIN pi.pathToPathLinks p WHERE pi = :productInstanceIteration"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkByProductInstanceIteration", query="SELECT DISTINCT p FROM ProductInstanceIteration pi JOIN pi.pathToPathLinks p WHERE pi = :productInstanceIteration"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkTypesByProductBaseline", query="SELECT DISTINCT(p.type) FROM ProductBaseline pb JOIN pb.pathToPathLinks p WHERE pb = :productBaseline"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkTypesByProduct", query="SELECT DISTINCT(p.type) FROM ConfigurationItem ci JOIN ci.pathToPathLinks p WHERE ci = :configurationItem"),
        @NamedQuery(name="PathToPathLink.findNextPathToPathLinkInProduct", query="SELECT DISTINCT p FROM ConfigurationItem ci JOIN ci.pathToPathLinks p WHERE ci = :configurationItem AND p.sourcePath = :targetPath AND p.type = :type"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkBySourceAndTargetInProductInstance", query="SELECT DISTINCT p FROM ProductInstanceIteration pi JOIN pi.pathToPathLinks p WHERE pi = :productInstanceIteration AND (p.sourcePath = :source AND p.targetPath = :target OR p.sourcePath = :target AND p.targetPath = :source)"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkBySourceAndTargetInBaseline", query="SELECT DISTINCT p FROM ProductBaseline pb JOIN pb.pathToPathLinks p WHERE pb = :baseline AND (p.sourcePath = :source AND p.targetPath = :target OR p.sourcePath = :target AND p.targetPath = :source)"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinkBySourceAndTargetInProduct", query="SELECT DISTINCT p FROM ConfigurationItem ci JOIN ci.pathToPathLinks p WHERE ci = :configurationItem AND (p.sourcePath = :source AND p.targetPath = :target OR p.sourcePath = :target AND p.targetPath = :source)"),
        @NamedQuery(name="PathToPathLink.findSamePathToPathLinkInProduct", query="SELECT DISTINCT p FROM ConfigurationItem ci JOIN ci.pathToPathLinks p WHERE ci = :configurationItem AND p.sourcePath = :sourcePath AND p.targetPath = :targetPath AND p.type = :type"),
        @NamedQuery(name="PathToPathLink.findPathToPathLinksForGivenProductInstanceIterationAndType", query="SELECT DISTINCT p FROM ProductInstanceIteration pi JOIN pi.pathToPathLinks p WHERE pi = :productInstanceIteration AND p.type = :type"),

        @NamedQuery(name="PathToPathLink.findRootPathToPathLinkForGivenProductInstanceIterationAndType", query="SELECT DISTINCT p FROM ProductInstanceIteration pi JOIN pi.pathToPathLinks p WHERE p.type = :type AND pi = :productInstanceIteration AND p.sourcePath not in (SELECT _p.targetPath FROM PathToPathLink _p WHERE _p member of pi.pathToPathLinks AND _p.type = :type)"),

        @NamedQuery(name="PathToPathLink.findRootPathToPathLinkForGivenProductBaselineAndType", query="SELECT DISTINCT p FROM ProductBaseline pb JOIN pb.pathToPathLinks p WHERE p.type = :type AND pb = :productBaseline AND p.sourcePath not in (SELECT _p.targetPath FROM PathToPathLink _p WHERE _p member of pb.pathToPathLinks AND _p.type = :type)"),

        @NamedQuery(name="PathToPathLink.findRootPathToPathLinkForGivenProductAndType", query="SELECT DISTINCT p FROM ConfigurationItem ci JOIN ci.pathToPathLinks p WHERE p.type = :type AND ci = :configurationItem AND p.sourcePath not in (SELECT _p.targetPath FROM PathToPathLink _p WHERE _p member of ci.pathToPathLinks AND _p.type = :type)"),

        @NamedQuery(name="PathToPathLink.findPathToPathLinkByPathListInProduct", query="SELECT DISTINCT p FROM ConfigurationItem ci JOIN ci.pathToPathLinks p WHERE ci = :configurationItem AND p.sourcePath in :paths AND p.targetPath in :paths"),
        @NamedQuery(name="PathToPathLink.findSourcesPathToPathLinkInProduct", query="SELECT DISTINCT p FROM ConfigurationItem ci JOIN ci.pathToPathLinks p WHERE ci = :configurationItem AND p.sourcePath = :source AND p.type = :type"),
        @NamedQuery(name="PathToPathLink.findSourcesPathToPathLinkInProductBaseline", query="SELECT DISTINCT p FROM ProductBaseline pb JOIN pb.pathToPathLinks p WHERE pb = :productBaseline AND p.sourcePath = :source AND p.type = :type"),
        @NamedQuery(name="PathToPathLink.findLinksWherePartialPathIsPresent", query="SELECT DISTINCT p FROM PathToPathLink p WHERE p.targetPath LIKE :endOfChain OR p.targetPath LIKE :inChain OR p.sourcePath LIKE :endOfChain OR p.sourcePath LIKE :inChain"),
        @NamedQuery(name="PathToPathLink.isSourceInProductInstanceContext", query="SELECT p FROM PathToPathLink p JOIN ProductInstanceIteration pi WHERE pi = :productInstanceIteration AND p member of pi.pathToPathLinks AND p.sourcePath = :path"),
        @NamedQuery(name="PathToPathLink.isTargetInProductInstanceContext", query="SELECT p FROM PathToPathLink p JOIN ProductInstanceIteration pi WHERE pi = :productInstanceIteration AND p member of pi.pathToPathLinks AND p.targetPath = :path"),
        @NamedQuery(name="PathToPathLink.isSourceInConfigurationItemContext", query="SELECT p FROM PathToPathLink p JOIN ConfigurationItem ci WHERE ci = :configurationItem AND p member of ci.pathToPathLinks AND p.sourcePath = :path"),
        @NamedQuery(name="PathToPathLink.isTargetInConfigurationItemContext", query="SELECT p FROM PathToPathLink p JOIN ConfigurationItem ci WHERE ci = :configurationItem AND p member of ci.pathToPathLinks AND p.targetPath = :path")
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
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        PathToPathLink that = (PathToPathLink) o;

        if (id != that.id){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id;
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
