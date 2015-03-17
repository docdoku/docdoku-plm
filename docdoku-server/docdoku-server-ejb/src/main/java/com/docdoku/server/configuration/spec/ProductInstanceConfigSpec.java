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
package com.docdoku.server.configuration.spec;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PartMaster;

import java.util.List;


/**
 * A {@link com.docdoku.core.configuration.ProductConfigSpec} which returns the {@link com.docdoku.core.product.PartIteration} and {@link com.docdoku.core.document.DocumentIteration}
 * which belong to the given baseline.
 *
 * @author Florent Garin
 * @version 1.1, 30/10/11
 * @since   V1.1
 */
public class ProductInstanceConfigSpec extends ProductConfigSpec {

    private ProductInstanceIteration productInstanceIteration;
    private User user;

    public ProductInstanceConfigSpec(){
    }
    public ProductInstanceConfigSpec(ProductInstanceIteration productInstanceIteration, User user) {
        this.productInstanceIteration = productInstanceIteration;
        this.user = user;
    }

    public ProductInstanceIteration getProductInstanceIteration() {
        return productInstanceIteration;
    }
    public void setProductInstanceIteration(ProductInstanceIteration productInstanceIteration) {
        this.productInstanceIteration = productInstanceIteration;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public int getPartCollectionId(){
        return productInstanceIteration.getPartCollection().getId();
    }

    @Override
    public PartIteration filterPartIteration(PartMaster part) {
        PartCollection partCollection = productInstanceIteration==null ? null : productInstanceIteration.getPartCollection();// Prevent NullPointerException
        if(partCollection != null) {
            BaselinedPartKey baselinedRootPartKey = new BaselinedPartKey(partCollection.getId(), part.getWorkspaceId(), part.getNumber());
            BaselinedPart baselinedRootPart = productInstanceIteration.getBaselinedPart(baselinedRootPartKey);
            if (baselinedRootPart != null) {
                return baselinedRootPart.getTargetPart();
            }
        }
        return null;
    }

    @Override
    public PartLink filterPartLink(List<PartLink> path) {
        // TODO : implement filter
        return null;
    }

}