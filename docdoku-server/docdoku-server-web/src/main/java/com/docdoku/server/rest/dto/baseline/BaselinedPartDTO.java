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

import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaselinedPartDTO {

    private String number;
    private String name;
    private String version;
    private int iteration;
    private List<BaselinedPartOptionDTO> availableIterations;

    public BaselinedPartDTO() {
    }

    public BaselinedPartDTO(PartIteration partIteration){
        this.number = partIteration.getPartNumber();
        this.version = partIteration.getVersion();
        this.name = partIteration.getPartRevision().getPartMaster().getName();
        this.iteration = partIteration.getIteration();

        this.availableIterations = new ArrayList<>();
        for(PartRevision partRevision : partIteration.getPartRevision().getPartMaster().getPartRevisions()){
            BaselinedPartOptionDTO option = new BaselinedPartOptionDTO(partRevision.getVersion(),
                                                                       partRevision.getLastIteration().getIteration(),
                                                                       partRevision.isReleased());
            this.availableIterations.add(option);
        }
    }

    public BaselinedPartDTO(List<PartIteration> availableParts){

        PartIteration max = Collections.max(availableParts);

        this.number = max.getPartNumber();
        this.version = max.getVersion();
        this.name = max.getPartRevision().getPartMaster().getName();
        this.iteration = max.getIteration();

        this.availableIterations = new ArrayList<>();
        for(PartIteration partIteration : availableParts){
            this.availableIterations.add(new BaselinedPartOptionDTO(partIteration.getVersion(),partIteration.getIteration(),partIteration.getPartRevision().isReleased()));
        }

    }

    public BaselinedPartDTO(String number, String version, int iteration) {
        this.number = number;
        this.version = version;
        this.iteration = iteration;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public int getIteration() {
        return iteration;
    }
    public void setIteration(int iteration) {
        this.iteration = iteration;
    }


    public List<BaselinedPartOptionDTO> getAvailableIterations() {
        return availableIterations;
    }
    public void setAvailableIterations(List<BaselinedPartOptionDTO> availableIterations) {
        this.availableIterations = availableIterations;
    }

}