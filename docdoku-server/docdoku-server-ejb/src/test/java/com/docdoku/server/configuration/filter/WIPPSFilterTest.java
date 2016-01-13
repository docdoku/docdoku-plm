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

package com.docdoku.server.configuration.filter;

import com.docdoku.core.common.User;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
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
public class WIPPSFilterTest {

    private WIPPSFilter filter;
    private PartMaster partMaster;
    private List<PartRevision> partRevisions;
    private User user;

    @Before
    public void setup() {
        user = Mockito.spy(new User());
        Mockito.doReturn("test").when(user).getLogin();
        filter = new WIPPSFilter(user);
        partMaster = Mockito.spy(new PartMaster());
        partRevisions = new ArrayList<>();
        Mockito.doReturn(partRevisions).when(partMaster).getPartRevisions();
    }


    @Test
    public void testFilterNoIterationAccessible() throws Exception {
        PartRevision partRevision = Mockito.spy(new PartRevision());
        Mockito.doReturn(null).when(partRevision).getLastAccessibleIteration(Mockito.any());
        partRevisions.add(partRevision);


        //Should return empty list of partIteration if only one partRevision with no accessible iteration
        Assert.assertTrue(filter.filter(partMaster).isEmpty());

        partRevision = Mockito.spy(new PartRevision());
        Mockito.doReturn(null).when(partRevision).getLastAccessibleIteration(Mockito.any());
        partRevisions.add(partRevision);
        partRevision = Mockito.spy(new PartRevision());
        Mockito.doReturn(null).when(partRevision).getLastAccessibleIteration(Mockito.any());
        partRevisions.add(partRevision);
        partRevision = Mockito.spy(new PartRevision());
        Mockito.doReturn(null).when(partRevision).getLastAccessibleIteration(Mockito.any());
        partRevisions.add(partRevision);

        //Should still return empty list of partiteration with multiple partRevision
        Assert.assertTrue(filter.filter(partMaster).isEmpty());
    }

    @Test
    public void testFilterLastRevisionNoIteration() throws Exception {
        PartRevision partRevision = Mockito.spy(new PartRevision());
        Mockito.doReturn(Mockito.mock(PartIteration.class)).when(partRevision).getLastAccessibleIteration(Mockito.any());
        partRevisions.add(partRevision);

        Assert.assertTrue(filter.filter(partMaster).size() == 1);

        partRevision = Mockito.spy(new PartRevision());
        Mockito.doReturn(null).when(partRevision).getLastAccessibleIteration(Mockito.any());
        partRevisions.add(partRevision);

        Assert.assertTrue(filter.filter(partMaster).size() == 1);

        partRevision = Mockito.spy(new PartRevision());
        Mockito.doReturn(null).when(partRevision).getLastAccessibleIteration(Mockito.any());
        partRevisions.add(partRevision);

        Assert.assertTrue(filter.filter(partMaster).size() == 1);
    }
}