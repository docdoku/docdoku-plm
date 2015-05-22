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
package com.docdoku.core.meta;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link InstanceAttributeTemplate} implementation that can instantiate list of values based
 * attributes.
 * 
 * @author Florent Garin
 * @version 2.0, 02/03/15
 * @since   V2.0
 */
@Table(name="ILOVATTRIBUTETEMPLATE")
@Entity
public class ListOfValuesAttributeTemplate extends InstanceAttributeTemplate {


    @JoinColumns({
            @JoinColumn(name = "LOV_NAME", referencedColumnName = "NAME"),
            @JoinColumn(name = "LOV_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private ListOfValues lov;

    public ListOfValuesAttributeTemplate() {
    }

    public ListOfValuesAttributeTemplate(String pName, ListOfValues pLov) {
        super(pName);
        lov=pLov;
    }

    public DefaultAttributeTemplate.AttributeType getAttributeType() {
        return AttributeType.LOV;
    }

    public ListOfValues getLov() {
        return lov;
    }

    public void setLov(ListOfValues lov) {
        this.lov = lov;
    }

    public String getLovName(){
        return this.lov != null ? this.lov.getName():null;
    }

    public InstanceAttribute createInstanceAttribute() {
        InstanceListOfValuesAttribute attr = new InstanceListOfValuesAttribute();
        List<NameValuePair> items = new ArrayList<>(lov.getValues());
        attr.setItems(items);
        attr.setName(name);
        attr.setMandatory(mandatory);
        attr.setLocked(locked);

        return attr;
    }



    @Override
    public String toString() {
        return name + "-" + lov;
    }
}
