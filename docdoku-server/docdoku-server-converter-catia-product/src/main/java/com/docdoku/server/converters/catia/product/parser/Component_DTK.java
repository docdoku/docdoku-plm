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

package com.docdoku.server.converters.catia.product.parser;

import java.util.ArrayList;
import java.util.List;

public class Component_DTK {

    private Integer id;
    private Integer fatherId;
    private String type;
    private boolean assembly;
    private String name;
    private String instanceName;
    private Positioning positioning = null;
    private List<MetaData> metaDataList = null;

    private static final String PROTOTYPE = "PrototypeComponentType";
    private static final String INSTANCE = "InstanceComponentType";
    private static final String CATALOG = "CatalogComponentType";

    private List<Component_DTK> subComponentDtkList = null;

    public Component_DTK(Integer id, Integer fatherId, String type, boolean assembly) {
        this.id = id;
        this.fatherId = fatherId;
        this.type = type;
        this.assembly = assembly;
    }

    public void addMetaData(MetaData metaData) {
        if (metaDataList == null) {
            metaDataList = new ArrayList<MetaData>();
        }
        metaDataList.add(metaData);
    }

    public void setPositioning(Positioning pPositioning) {
        positioning = pPositioning;
    }

    public void addSubComponent(Component_DTK componentDtk) {

        if (subComponentDtkList == null)
            subComponentDtkList = new ArrayList<Component_DTK>();

        subComponentDtkList.add(componentDtk);

    }

    public void setName(String pName) {
        name = pName;
    }

    public void setInstanceName(String pInstanceName) {
        instanceName = pInstanceName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFatherId() {
        return fatherId;
    }

    public void setFatherId(Integer fatherId) {
        this.fatherId = fatherId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAssembly() {
        return assembly;
    }

    public void setAssembly(boolean assembly) {
        this.assembly = assembly;
    }

    public String getName() {
        return name;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public Positioning getPositioning() {
        return positioning;
    }

    public List<MetaData> getMetaDataList() {
        return metaDataList;
    }

    public void setMetaDataList(List<MetaData> metaDataList) {
        this.metaDataList = metaDataList;
    }

    public List<Component_DTK> getSubComponentDtkList() {
        return subComponentDtkList;
    }

    public void setSubComponentDtkList(List<Component_DTK> subComponentDtkList) {
        this.subComponentDtkList = subComponentDtkList;
    }

    public boolean isLinkable() {
        return INSTANCE.equals(type);
    }

}
