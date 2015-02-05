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

import com.docdoku.core.common.User;
import com.docdoku.core.product.ConfigurationItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is an instance of a product, its main attributes are
 * the serial number and the configuration item it is an instance of.
 *
 * The composition of the instance may vary according to modifications
 * applied on it so to track this evolution the part collection is kept
 * on several {@link ProductInstanceIteration}.
 *
 * @author Florent Garin
 * @version 2.0, 24/02/14
 * @since   V2.0
 */
@Table(name="PRODUCTINSTANCEMASTER")
@IdClass(com.docdoku.core.configuration.ProductInstanceMasterKey.class)
@Entity
@NamedQueries({
        @NamedQuery(name="ProductInstanceMaster.findByConfigurationItemId", query="SELECT pim FROM ProductInstanceMaster pim WHERE pim.instanceOf.id = :ciId AND pim.instanceOf.workspace.id = :workspaceId")
})
public class ProductInstanceMaster implements Serializable {


    @Column(name="SERIALNUMBER", length = 100)
    @Id
    private String serialNumber;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "CONFIGURATIONITEM_ID", referencedColumnName = "ID"),
            @JoinColumn(name = "WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private ConfigurationItem instanceOf;

    @OneToMany(mappedBy = "productInstanceMaster", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("iteration ASC")
    private List<ProductInstanceIteration> productInstanceIterations = new ArrayList<>();

    public ProductInstanceMaster() {
    }

    public ProductInstanceMaster(ConfigurationItem configurationItem, String serialNumber) {
        this.instanceOf = configurationItem;
        this.serialNumber = serialNumber;
    }

    public ConfigurationItem getInstanceOf() {
        return instanceOf;
    }
    public void setInstanceOf(ConfigurationItem instanceOf) {
        this.instanceOf = instanceOf;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public List<ProductInstanceIteration> getProductInstanceIterations() {
        return productInstanceIterations;
    }

    public ProductInstanceIteration createNextIteration() {
        ProductInstanceIteration lastProductInstanceIteration = getLastIteration();
        int iteration;
        if(lastProductInstanceIteration==null){
            iteration = 1;
        }else{
            iteration = lastProductInstanceIteration.getIteration()+1;
        }
        ProductInstanceIteration productInstanceIteration = new ProductInstanceIteration(this,iteration);
        this.productInstanceIterations.add(productInstanceIteration);
        return productInstanceIteration;
    }
    public void removeIteration(ProductInstanceIteration prodInstI){
        this.productInstanceIterations.remove(prodInstI);
    }

    public ProductInstanceIteration getLastIteration() {
        int index = productInstanceIterations.size()-1;
        if(index < 0)
            return null;
        else
            return productInstanceIterations.get(index);
    }

    public User getUpdateAuthor(){
        ProductInstanceIteration productInstanceIteration = getLastIteration();
        if(productInstanceIteration==null){
            return null;
        }
        return getLastIteration().getUpdateAuthor();
    }

    public String getUpdateAuthorName(){
        User author = getUpdateAuthor();
        if(author==null){
            return null;
        }
        return author.getName();
    }

    public Date getUpdateDate(){
        ProductInstanceIteration productInstanceIteration = getLastIteration();
        if(productInstanceIteration==null){
            return null;
        }else{
            return getLastIteration().getUpdateDate();
        }
    }
}