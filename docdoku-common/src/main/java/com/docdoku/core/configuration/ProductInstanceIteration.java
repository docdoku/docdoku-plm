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
package com.docdoku.core.configuration;


import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.FileHolder;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PathToPathLink;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.*;

/**
 * This class represents a state, identified by its iteration, of an instance of
 * a product {@link ProductInstanceMaster}.
 *
 * @author Florent Garin
 * @version 2.0, 24/02/14
 * @since V2.0
 */
@Table(name = "PRODUCTINSTANCEITERATION")
@IdClass(com.docdoku.core.configuration.ProductInstanceIterationKey.class)
@Entity
@NamedQueries({
        @NamedQuery(name="ProductInstanceIteration.findByProductBaseline", query = "SELECT p FROM ProductInstanceIteration p WHERE p.basedOn = :productBaseline")
})
public class ProductInstanceIteration implements Serializable, FileHolder {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "PRDINSTANCEMASTER_SERIALNUMBER", referencedColumnName = "SERIALNUMBER"),
            @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "CONFIGURATIONITEM_ID"),
            @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private ProductInstanceMaster productInstanceMaster;

    @Id
    private int iteration;

    private String iterationNote;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PartCollection partCollection;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private DocumentCollection documentCollection = new DocumentCollection();

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(name = "PRDINSTITERATION_BINRES",
            inverseJoinColumns = {
                    @JoinColumn(name = "ATTACHEDFILE_FULLNAME", referencedColumnName = "FULLNAME")
            },
            joinColumns = {
                    @JoinColumn(name = "PRDINSTANCEMASTER_SERIALNUMBER", referencedColumnName = "PRDINSTANCEMASTER_SERIALNUMBER"),
                    @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "CONFIGURATIONITEM_ID"),
                    @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
                    @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
            })
    private Set<BinaryResource> attachedFiles = new HashSet<>();

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "PRDINSTITERATION_DOCUMENTLINK",
            inverseJoinColumns = {
                    @JoinColumn(name = "DOCUMENTLINK_ID", referencedColumnName = "ID")
            },
            joinColumns = {
                    @JoinColumn(name = "PRDINSTANCEMASTER_SERIALNUMBER", referencedColumnName = "PRDINSTANCEMASTER_SERIALNUMBER"),
                    @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "CONFIGURATIONITEM_ID"),
                    @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
                    @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
            })
    private Set<DocumentLink> linkedDocuments = new HashSet<>();

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "ATTRIBUTE_ORDER")
    @JoinTable(name = "PRDINSTITERATION_ATTRIBUTE",
            inverseJoinColumns = {
                    @JoinColumn(name = "INSTANCEATTRIBUTE_ID", referencedColumnName = "ID")
            },
            joinColumns = {
                    @JoinColumn(name = "PRDINSTANCEMASTER_SERIALNUMBER", referencedColumnName = "PRDINSTANCEMASTER_SERIALNUMBER"),
                    @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "CONFIGURATIONITEM_ID"),
                    @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
                    @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
            })
    private List<InstanceAttribute> instanceAttributes = new ArrayList<>();

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCTBASELINE_ID", referencedColumnName = "ID")
    private ProductBaseline basedOn;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "PRDINSTITERATION_PATHDATAMSTR",
            inverseJoinColumns = {
                    @JoinColumn(name = "PATHDATAMASTER_ID", referencedColumnName = "ID")
            },
            joinColumns = {
                    @JoinColumn(name="PRDINSTANCEITERATION_ITERATION", referencedColumnName="ITERATION"),
                    @JoinColumn(name="PRDINSTANCEMASTER_SERIALNUMBER", referencedColumnName="PRDINSTANCEMASTER_SERIALNUMBER"),
                    @JoinColumn(name="CONFIGURATIONITEM_ID", referencedColumnName="CONFIGURATIONITEM_ID"),
                    @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
            })
    private List<PathDataMaster> pathDataMasterList = new ArrayList<>();

    /**
     * Set of substitute links (actually their path from the root node)
     * that have been included into the baseline.
     * Only selected substitute links are stored as part usage links are considered as the default
     * choices for baselines.
     * <p>
     * Paths are strings made of ordered lists of usage link ids joined by "-".
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "PRDINSTANCEITERATION_SUBLINK",
            joinColumns = {
                    @JoinColumn(name = "PRDINSTANCEMASTER_SERIALNUMBER", referencedColumnName = "PRDINSTANCEMASTER_SERIALNUMBER"),
                    @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "CONFIGURATIONITEM_ID"),
                    @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
                    @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
            }
    )
    private Set<String> substituteLinks = new HashSet<>();

    /**
     * Set of optional usage links (actually their path from the root node)
     * that have been included into the baseline.
     * <p>
     * Paths are strings made of ordered lists of usage link ids joined by "-".
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "PRDINSTANCEITERATION_OPTLINK",
            joinColumns = {
                    @JoinColumn(name = "PRDINSTANCEMASTER_SERIALNUMBER", referencedColumnName = "PRDINSTANCEMASTER_SERIALNUMBER"),
                    @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "CONFIGURATIONITEM_ID"),
                    @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
                    @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
            }
    )

    private Set<String> optionalUsageLinks = new HashSet<>();


    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "PRDINSTITERATION_P2PLINK",
            inverseJoinColumns = {
                    @JoinColumn(name = "PATHTOPATHLINK_ID", referencedColumnName = "ID")
            },
            joinColumns = {
                    @JoinColumn(name = "PRDINSTANCEMASTER_SERIALNUMBER", referencedColumnName = "PRDINSTANCEMASTER_SERIALNUMBER"),
                    @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "CONFIGURATIONITEM_ID"),
                    @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
                    @JoinColumn(name = "ITERATION", referencedColumnName = "ITERATION")
            })
    private List<PathToPathLink> pathToPathLinks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate;

    public ProductInstanceIteration() {
    }

    public ProductInstanceIteration(ProductInstanceMaster pProductInstanceMaster, int pIteration) {
        this.productInstanceMaster = pProductInstanceMaster;
        this.iteration = pIteration;
    }

    @XmlTransient
    public ProductInstanceMaster getProductInstanceMaster() {
        return productInstanceMaster;
    }

    public void setProductInstanceMaster(ProductInstanceMaster productInstanceMaster) {
        this.productInstanceMaster = productInstanceMaster;
    }

    public String getSerialNumber() {
        return this.productInstanceMaster.getSerialNumber();
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public String getIterationNote() {
        return iterationNote;
    }

    public void setIterationNote(String iterationNote) {
        this.iterationNote = iterationNote;
    }

    public PartCollection getPartCollection() {
        return partCollection;
    }

    public void setPartCollection(PartCollection partCollection) {
        this.partCollection = partCollection;
    }

    public DocumentCollection getDocumentCollection() {
        return documentCollection;
    }

    public void setDocumentCollection(DocumentCollection documentCollection) {
        this.documentCollection = documentCollection;
    }

    public void addFile(BinaryResource pBinaryResource) {
        attachedFiles.add(pBinaryResource);
    }

    public Set<DocumentLink> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(Set<DocumentLink> pLinkedDocuments) {
        linkedDocuments = pLinkedDocuments;
    }

    public List<InstanceAttribute> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttribute> pInstanceAttributes) {
        instanceAttributes = pInstanceAttributes;
    }

    public void setAttachedFiles(Set<BinaryResource> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    @Override
    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }

    public Map<BaselinedPartKey, BaselinedPart> getBaselinedParts() {
        return partCollection.getBaselinedParts();
    }

    public void addBaselinedPart(PartIteration targetPart) {
        partCollection.addBaselinedPart(targetPart);
    }

    public boolean hasBasedLinedPart(String targetPartWorkspaceId, String targetPartNumber) {
        return partCollection.hasBaselinedPart(new BaselinedPartKey(partCollection.getId(), targetPartWorkspaceId, targetPartNumber));
    }

    public BaselinedPart getBaselinedPart(BaselinedPartKey baselinedPartKey) {
        return partCollection.getBaselinedPart(baselinedPartKey);
    }

    public Map<BaselinedDocumentKey, BaselinedDocument> getBaselinedDocuments() {
        return documentCollection.getBaselinedDocuments();
    }

    public BaselinedDocument getBaselinedDocument(BaselinedDocumentKey baselinedDocumentKey) {
        return documentCollection.getBaselinedDocument(baselinedDocumentKey);
    }

    public ProductBaseline getBasedOn() {
        return basedOn;
    }

    public void setBasedOn(ProductBaseline basedOn) {
        this.basedOn = basedOn;
    }

    public List<PathDataMaster> getPathDataMasterList() {
        return pathDataMasterList;
    }

    public void setPathDataMasterList(List<PathDataMaster> pathDataMasterList) {
        this.pathDataMasterList = pathDataMasterList;
    }

    public User getUpdateAuthor(){
        return this.getPartCollection().getAuthor();
    }

    public String getUpdateAuthorName() {
        User updateAuthor = getUpdateAuthor();
        if (updateAuthor == null) {
            return null;
        }
        return updateAuthor.getName();
    }

    public Date getModificationDate() {
        return (modificationDate!=null) ? (Date) modificationDate.clone() : null;
    }
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = (modificationDate!=null) ? (Date) modificationDate.clone() : null;
    }

    public List<BaselinedPart> getBaselinedPartsList() {
        return new ArrayList<>(this.getBaselinedParts().values());
    }

    public Set<String> getSubstituteLinks() {
        return substituteLinks;
    }

    public void setSubstituteLinks(Set<String> substituteLinks) {
        this.substituteLinks = substituteLinks;
    }

    public Set<String> getOptionalUsageLinks() {
        return optionalUsageLinks;
    }

    public void setOptionalUsageLinks(Set<String> optionalUsageLinks) {
        this.optionalUsageLinks = optionalUsageLinks;
    }

    public String getConfigurationItemId() {
        return this.productInstanceMaster.getInstanceOf().getId();
    }


    public boolean hasSubstituteLink(String link) {
        return substituteLinks.contains(link);
    }

    public boolean isLinkOptional(String link) {
        return optionalUsageLinks.contains(link);
    }

    public boolean removeFile(BinaryResource pBinaryResource) {
        return attachedFiles.remove(pBinaryResource);
    }

    public List<PathToPathLink> getPathToPathLinks() {
        return pathToPathLinks;
    }

    public void setPathToPathLinks(List<PathToPathLink> pathToPathLinks) {
        this.pathToPathLinks = pathToPathLinks;
    }

    public void addPathToPathLink(PathToPathLink pathToPathLink) {
        pathToPathLinks.add(pathToPathLink);
    }

    public void removePathToPathLink(PathToPathLink pathToPathLink) {
        pathToPathLinks.remove(pathToPathLink);
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

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        ProductInstanceIteration that = (ProductInstanceIteration) o;

        if (iteration != that.iteration) {
            return false;
        }
        if (productInstanceMaster != null ? !productInstanceMaster.equals(that.productInstanceMaster) : that.productInstanceMaster != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = productInstanceMaster != null ? productInstanceMaster.hashCode() : 0;
        result = 31 * result + iteration;
        return result;
    }
}
