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
import com.docdoku.core.product.PartIteration;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This class represents a state, identified by its iteration, of an instance of
 * a product <a href="ProductInstanceMaster.html">ProductInstanceMaster</a>.
 * 
 * @author Florent Garin
 * @version 2.0, 24/02/14
 * @since   V2.0
 */
@Table(name="PRODUCTINSTANCEITERATION")
@IdClass(com.docdoku.core.configuration.ProductInstanceIterationKey.class)
@Entity
public class ProductInstanceIteration implements Serializable {

    @Id
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="PRDINSTANCEMASTER_SERIALNUMBER", referencedColumnName="SERIALNUMBER"),
            @JoinColumn(name="CONFIGURATIONITEM_ID", referencedColumnName="CONFIGURATIONITEM_ID"),
            @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")

    })
    private ProductInstanceMaster productInstanceMaster;

    @Id
    private int iteration;

    private String iterationNote;

    @OneToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
    private PartCollection partCollection;

    public ProductInstanceIteration() {
    }

    public ProductInstanceIteration(ProductInstanceMaster pProductInstanceMaster, int pIteration) {
        this.productInstanceMaster = pProductInstanceMaster;
        this.iteration = pIteration;
    }

    @XmlTransient
    public ProductInstanceMaster getProductInstanceMaster() {return productInstanceMaster;}
    public void setProductInstanceMaster(ProductInstanceMaster productInstanceMaster) {this.productInstanceMaster = productInstanceMaster;}

    public String getSerialNumber(){return this.productInstanceMaster.getSerialNumber();}
    public String getConfigurationItemId(){return this.productInstanceMaster.getInstanceOf().getId();}

    public int getIteration() {return iteration;}
    public void setIteration(int iteration) {this.iteration = iteration;}

    public String getIterationNote() {return iterationNote;}
    public void setIterationNote(String iterationNote) {this.iterationNote = iterationNote;}

    public PartCollection getPartCollection() {return partCollection;}
    public void setPartCollection(PartCollection partCollection) {this.partCollection = partCollection;}

    public Map<BaselinedPartKey, BaselinedPart> getBaselinedParts() {return partCollection.getBaselinedParts();}
    public void addBaselinedPart(PartIteration targetPart){
        partCollection.addBaselinedPart(targetPart);
    }
    public boolean hasBasedLinedPart(String targetPartWorkspaceId, String targetPartNumber){
        return partCollection.hasBasedLinedPart(new BaselinedPartKey(partCollection.getId(),targetPartWorkspaceId,targetPartNumber));
    }
    public BaselinedPart getBaselinedPart(BaselinedPartKey baselinedPartKey){return partCollection.getBaselinedPart(baselinedPartKey);}

    public User getUpdateAuthor(){
        return this.getPartCollection().getAuthor();
    }
    public String getUpdateAuthorName(){
        User author = getUpdateAuthor();
        if(author==null){
            return null;
        }
        return author.getName();
    }
    public Date getUpdateDate(){
        return this.getPartCollection().getCreationDate();
    }
    public List<BaselinedPart> getBaselinedPartsList(){return new ArrayList<>(this.getBaselinedParts().values());}
}
