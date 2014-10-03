/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * A {@link ConfigSpec} which returns the {@link PartIteration} and {@link DocumentIteration}
 * which belong to the given baseline.
 *
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */
@Table(name="BASELINECONFIGSPEC")
@Entity
public class BaselineConfigSpec extends ConfigSpec {

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private ProductBaseline productBaseline;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private DocumentBaseline documentBaseline;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User user;

    public BaselineConfigSpec(){
    }
    public BaselineConfigSpec(ProductBaseline productBaseline, User user) {
        this.productBaseline = productBaseline;
        this.user = user;
    }
    public BaselineConfigSpec(DocumentBaseline documentBaseline, User user) {
        this.documentBaseline = documentBaseline;
        this.user = user;
    }

    public DocumentBaseline getDocumentBaseline() {
        return documentBaseline;
    }
    public void setDocumentBaseline(DocumentBaseline documentBaseline) {
        this.documentBaseline = documentBaseline;
    }

    public ProductBaseline getProductBaseline() {
        return productBaseline;
    }
    public void setProductBaseline(ProductBaseline productBaseline) {
        this.productBaseline = productBaseline;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public int getPartCollectionId(){
        return productBaseline.getPartCollection().getId();
    }
    public int getFolderCollectionId(){
        return documentBaseline.getFolderCollection().getId();
    }

    @Override
    public PartIteration filterConfigSpec(PartMaster part) {
        PartCollection partCollection = productBaseline==null ? null : productBaseline.getPartCollection();             // Prevent NullPointerException
        if(partCollection != null) {
            BaselinedPartKey baselinedRootPartKey = new BaselinedPartKey(partCollection.getId(), part.getWorkspaceId(), part.getNumber());
            BaselinedPart baselinedRootPart = productBaseline.getBaselinedPart(baselinedRootPartKey);
            if (baselinedRootPart != null) {
                return baselinedRootPart.getTargetPart();
            }
            // the part isn't in baseline, choose the latest checked in version-iteration
            return new LatestConfigSpec(user).filterConfigSpec(part);
        }

        return null;
    }

    @Override
    public DocumentIteration filterConfigSpec(DocumentRevision documentRevision) {
        FolderCollection folderCollection = documentBaseline==null ? null : documentBaseline.getFolderCollection();     // Prevent NullPointerException
        if(folderCollection != null){
            DocumentIteration docI = folderCollection.getDocumentIteration(documentRevision.getKey());
            if(docI!=null){
                return docI;
            }
            // the document isn't in baseline, choose the latest checked in version-iteration
            return new LatestConfigSpec(user).filterConfigSpec(documentRevision);
        }

        return null;
    }
}