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

import com.docdoku.core.configuration.PathChoice;
import com.docdoku.core.configuration.ResolvedPartLink;
import com.docdoku.server.rest.dto.PartUsageLinkDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import java.util.ArrayList;
import java.util.List;

public class PathChoiceDTO {

    private List<ResolvedPartLinkDTO> resolvedPath = new ArrayList<>();
    private PartUsageLinkDTO partUsageLink;

    public PathChoiceDTO() {
    }

    public PathChoiceDTO(PathChoice choice) {
        for(ResolvedPartLink resolvedPartLink:choice.getResolvedPath()){
            resolvedPath.add(new ResolvedPartLinkDTO(resolvedPartLink));
        }

        Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
        partUsageLink = mapper.map(choice.getPartUsageLink(),PartUsageLinkDTO.class);
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
