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

package com.docdoku.server.validation;

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceTextAttribute;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by kelto on 16/07/15.
 */
@RunWith(Parameterized.class)
public class AttributesConsistencyUtilsTest extends TestCase {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] parameters = new Object[4][4];
        List<InstanceAttribute> current = new ArrayList<>();
        List<InstanceAttribute> newAttrs = new ArrayList<>();
        InstanceAttribute attribute = new InstanceTextAttribute("name","value1",false);
        InstanceAttribute attribute1 = new InstanceTextAttribute("name","value2",true);
        attribute1.setLocked(true);
        InstanceAttribute attribute2 = new InstanceTextAttribute("test",null,false);
        current.add(attribute);
        current.add(attribute1);
        current.add(attribute2);
        newAttrs.add(attribute);
        newAttrs.add(attribute1);

        parameters[0][0] = current;
        parameters[0][1] = newAttrs;
        parameters[0][2] = false;
        parameters[0][3] = true;

        current = new ArrayList<>();
        newAttrs = new ArrayList<>();
        current.add(new InstanceTextAttribute("name", "value", true));
        newAttrs.add(new InstanceTextAttribute("name", "value", true));

        parameters[1][0] = current;
        parameters[1][1] = newAttrs;
        parameters[1][2] = false;
        //should fail, the attribute is not locked.
        parameters[1][3] = false;

        attribute = new InstanceTextAttribute("name","value",true);
        attribute.setLocked(true);
        attribute1 = new InstanceTextAttribute("name",null,true);
        attribute1.setLocked(true);
        current = new ArrayList<>();
        newAttrs = new ArrayList<>();
        current.add(attribute);
        newAttrs.add(attribute1);
        parameters[2][0] = current;
        parameters[2][1] = newAttrs;
        parameters[2][2] = false;
        //should fail, the attribute is mandatory but null value.
        parameters[2][3] = false;

        attribute = new InstanceTextAttribute("name","value",true);
        attribute.setLocked(true);
        attribute1 = new InstanceTextAttribute("name","",true);
        attribute1.setLocked(true);
        current = new ArrayList<>();
        newAttrs = new ArrayList<>();
        current.add(attribute);
        newAttrs.add(attribute1);
        parameters[3][0] = current;
        parameters[3][1] = newAttrs;
        parameters[3][2] = false;
        //should fail, the attribute is mandatory but empty value.
        parameters[3][3] = false;
        return Arrays.asList(parameters);
    }
    @Parameterized.Parameter
    public List<InstanceAttribute> currentAttrs;

    @Parameterized.Parameter(value = 1)
    public List<InstanceAttribute> pAttributes;

    @Parameterized.Parameter(value = 2)
    public boolean isAttributesLocked;

    @Parameterized.Parameter(value = 3)
    public boolean expect;

    @Test
    public void testHasValidChange() throws Exception {
        Assert.assertEquals("Wrong result from hasValidChange", expect, AttributesConsistencyUtils.hasValidChange(pAttributes, isAttributesLocked, currentAttrs));
    }

}