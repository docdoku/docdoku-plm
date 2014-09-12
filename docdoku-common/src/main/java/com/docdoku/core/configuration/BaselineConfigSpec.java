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
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.baseline.BaselinedDocument;
import com.docdoku.core.document.baseline.BaselinedDocumentKey;
import com.docdoku.core.document.baseline.DocumentBaseline;
import com.docdoku.core.document.baseline.DocumentsCollection;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

    @Override
    public int getId(){
        if(productBaseline!=null){
            return productBaseline.getPartCollection().getId();
        }else if(documentBaseline!=null){
            return documentBaseline.getDocumentsCollection().getId();
        }else{
            return -1;
        }
    }

    @Override
    public PartIteration filterConfigSpec(PartMaster part) {
        PartCollection partCollection = productBaseline.getPartCollection();
        if(partCollection != null){
            BaselinedPartKey baselinedRootPartKey = new BaselinedPartKey(partCollection.getId(),part.getWorkspaceId(),part.getNumber());
            BaselinedPart baselinedRootPart = productBaseline.getBaselinedPart(baselinedRootPartKey);
            if(baselinedRootPart != null){
                return baselinedRootPart.getTargetPart();
            }else{
                // the part isn't in baseline, choose the latest version-iteration uncheckouted
                return new LatestConfigSpec(user).filterConfigSpec(part);
            }
        }else{
            return null;
        }
    }

    @Override
    public DocumentIteration filterConfigSpec(DocumentMaster documentMaster) {
        DocumentsCollection documentsCollection = documentBaseline.getDocumentsCollection();
        if(documentsCollection != null){
            BaselinedDocumentKey baselinedDocumentKey = new BaselinedDocumentKey(documentBaseline.getId(), documentMaster.getWorkspaceId(),documentMaster.getId());
            BaselinedDocument baselinedDocument = documentsCollection.getBaselinedDocument(baselinedDocumentKey);
            if(baselinedDocument != null){
                return baselinedDocument.getTargetDocument();
            }else{
                return new LatestConfigSpec(user).filterConfigSpec(documentMaster);
            }
        }else{
            return null;
        }
    }

}