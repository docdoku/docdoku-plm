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
package com.docdoku.core.product;

import com.docdoku.core.common.User;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Morgan Guimard
 *
 * Helping class to construct a descriptive tree of a resolved configuration
 */

public class Component implements Serializable {

    private User user;
    private PartMaster partMaster;
    private PartIteration retainedIteration;
    private List<PartLink> path;
    private List<Component> components;

    public Component() {
    }

    public Component(User user, PartMaster partMaster, List<PartLink> path, List<Component> components) {
        this.user = user;
        this.partMaster = partMaster;
        this.path = path;
        this.components = components;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public List<PartLink> getPath() {
        return path;
    }

    public void setPath(List<PartLink> path) {
        this.path = path;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public PartMaster getPartMaster() {
        return partMaster;
    }

    public void setPartMaster(PartMaster partMaster) {
        this.partMaster = partMaster;
    }

    public PartIteration getRetainedIteration() {
        return retainedIteration;
    }

    public void setRetainedIteration(PartIteration retainedIteration) {
        this.retainedIteration = retainedIteration;
    }
}
