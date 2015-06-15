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

package com.docdoku.server.rest.dto.product;

import com.docdoku.core.product.PathToPathLink;
import com.docdoku.server.rest.dto.ACLDTO;
import com.docdoku.server.rest.dto.PathToPathLinkDTO;
import com.docdoku.server.rest.dto.baseline.BaselineDTO;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class ProductInstanceMasterDTO {

    private String serialNumber;
    private String configurationItemId;
    private String updateAuthor;
    private String updateAuthorName;
    private Date updateDate;
    private List<ProductInstanceIterationDTO> productInstanceIterations;
    private ACLDTO acl;
    private List<PathToPathLinkDTO> typedLinks;
    public ProductInstanceMasterDTO() {
    }

    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getConfigurationItemId() {
        return configurationItemId;
    }
    public void setConfigurationItemId(String configurationItemId) {
        this.configurationItemId = configurationItemId;
    }

    public String getUpdateAuthor() {
        return updateAuthor;
    }
    public void setUpdateAuthor(String updateAuthor) {
        this.updateAuthor = updateAuthor;
    }

    public String getUpdateAuthorName() {
        return updateAuthorName;
    }
    public void setUpdateAuthorName(String updateAuthorName) {
        this.updateAuthorName = updateAuthorName;
    }

    public Date getUpdateDate() {
        return updateDate;
    }
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public List<ProductInstanceIterationDTO> getProductInstanceIterations() {
        return productInstanceIterations;
    }
    public void setProductInstanceIterations(List<ProductInstanceIterationDTO> productInstanceIterations) {
        this.productInstanceIterations = productInstanceIterations;
    }
    public ACLDTO getAcl() {
        return acl;
    }

    public void setAcl(ACLDTO acl) {
        this.acl = acl;
    }

    public List<PathToPathLinkDTO> getTypedLinks() {
        return typedLinks;
    }

    public void setTypedLinks(List<PathToPathLinkDTO> typedLinks) {
        this.typedLinks = typedLinks;
    }


}