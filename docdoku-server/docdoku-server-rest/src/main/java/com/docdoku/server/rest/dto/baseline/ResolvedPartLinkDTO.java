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

package com.docdoku.server.rest.dto.baseline;

import com.docdoku.core.configuration.ResolvedPartLink;
import com.docdoku.core.product.PartIteration;
import com.docdoku.server.rest.dto.LightPartLinkDTO;
import com.docdoku.server.rest.dto.PartIterationDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@ApiModel(value="ResolvedPartLinkDTO", description="This class is the representation of a {@link com.docdoku.core.product.PartLink} and its resolved {@link com.docdoku.core.product.PartIteration} in context")
public class ResolvedPartLinkDTO implements Serializable {

    @ApiModelProperty(value = "Resolved part iteration")
    private PartIterationDTO partIteration;

    @ApiModelProperty(value = "Usage Link")
    private LightPartLinkDTO partLink;

    public ResolvedPartLinkDTO() {
    }

    public ResolvedPartLinkDTO(ResolvedPartLink resolvedPartLink) {
        PartIteration resolvedIteration = resolvedPartLink.getPartIteration();
        this.partIteration = new PartIterationDTO(resolvedIteration.getWorkspaceId(), resolvedIteration.getName(), resolvedIteration.getNumber(), resolvedIteration.getVersion(), resolvedIteration.getIteration());
        this.partLink = new LightPartLinkDTO(resolvedPartLink.getPartLink());
    }

    public PartIterationDTO getPartIteration() {
        return partIteration;
    }

    public void setPartIteration(PartIterationDTO partIteration) {
        this.partIteration = partIteration;
    }

    public LightPartLinkDTO getPartLink() {
        return partLink;
    }

    public void setPartLink(LightPartLinkDTO partLink) {
        this.partLink = partLink;
    }
}
