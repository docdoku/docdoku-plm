/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.server.rest.dto.PartUsageLinkDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@ApiModel(value="PathChoiceDTO", description="This class is the representation of {@link com.docdoku.core.configuration.PathChoice} entity")
public class PathChoiceDTO implements Serializable {

    @ApiModelProperty(value = "Complete path in context")
    private List<ResolvedPartLinkDTO> resolvedPath = new ArrayList<>();

    @ApiModelProperty(value = "Path concerned by the choice")
    private PartUsageLinkDTO partUsageLink;

    public PathChoiceDTO() {
    }

    public List<ResolvedPartLinkDTO> getResolvedPath() {
        return resolvedPath;
    }

    public void setResolvedPath(List<ResolvedPartLinkDTO> resolvedPath) {
        this.resolvedPath = resolvedPath;
    }

    public PartUsageLinkDTO getPartUsageLink() {
        return partUsageLink;
    }

    public void setPartUsageLink(PartUsageLinkDTO partUsageLink) {
        this.partUsageLink = partUsageLink;
    }
}
