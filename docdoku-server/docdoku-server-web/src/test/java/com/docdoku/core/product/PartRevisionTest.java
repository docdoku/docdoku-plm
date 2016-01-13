/*
 *
 *  * DocDoku, Professional Open Source
 *  * Copyright 2006 - 2015 DocDoku SARL
 *  *
 *  * This file is part of DocDokuPLM.
 *  *
 *  * DocDokuPLM is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * DocDokuPLM is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.docdoku.core.product;

import com.docdoku.core.common.User;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kelto on 13/01/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class PartRevisionTest extends TestCase {

    private PartRevision partRevision;
    private User user;
    private List<PartIteration> partIterations;

    @Before
    public void setup() {
        partRevision = new PartRevision();
        user = Mockito.spy(new User());
        partIterations = new ArrayList<>();
        partRevision.setAuthor(user);
        partRevision.setPartIterations(partIterations);
    }
    @Test
    public void testGetLastAccessibleIteration() throws Exception {
        PartIteration partIteration = Mockito.mock(PartIteration.class);
        partIterations.add(partIteration);

        //No checkedOut user, if any iteration present, should send back the last one.
        Assert.assertTrue(partRevision.getLastAccessibleIteration(new User()) != null);

        partRevision.setCheckOutUser(user);

        //The only iteration has been checked-out, should return null since it's not the same user
        Assert.assertTrue(partRevision.getLastAccessibleIteration(new User()) == null);

        //The user who checked-out the part can access it
        Assert.assertTrue(partRevision.getLastAccessibleIteration(user) != null);

        partIterations.add(Mockito.mock(PartIteration.class));
        //Any other user should have access to the previous iteration
        Assert.assertTrue(partRevision.getLastAccessibleIteration(new User()) == partIteration);
    }
}