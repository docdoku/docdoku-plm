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

package com.docdoku.api;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.PartCreationDTO;
import com.docdoku.api.models.PartIterationDTO;
import com.docdoku.api.models.PartRevisionDTO;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.PartsApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PartApiTest {

    private PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
    private PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);

    @Test
    public void partApiUsageTests() throws ApiException {

        // Create a part
        PartCreationDTO part = new PartCreationDTO();
        part.setNumber(TestUtils.randomString());
        part.setName("GeneratedPart");

        PartRevisionDTO createdPart = partsApi.createNewPart(TestConfig.WORKSPACE, part);
        Assert.assertEquals(createdPart.getNumber(), part.getNumber());

        // Check in
        PartRevisionDTO checkedInPart = partApi.checkIn(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion());
        Assert.assertEquals(checkedInPart.getNumber(),part.getNumber());
        Assert.assertEquals(LastIterationHelper.getLastIteration(checkedInPart).getIteration(), Integer.valueOf("1"));

        // Check out
        PartRevisionDTO checkedOutPart = partApi.checkOut(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion());
        Assert.assertEquals(checkedOutPart.getNumber(),part.getNumber());
        Assert.assertEquals(LastIterationHelper.getLastIteration(checkedOutPart).getIteration(),Integer.valueOf("2"));

        // Undo check out
        PartRevisionDTO undoCheckOutPart= partApi.undoCheckOut(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion());
        Assert.assertEquals(undoCheckOutPart,checkedInPart);

        // Check out
        checkedOutPart = partApi.checkOut(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion());

        // Edit
        PartIterationDTO lastIteration = LastIterationHelper.getLastIteration(checkedOutPart);
        Assert.assertNull(lastIteration.getModificationDate());
        lastIteration.setIterationNote("Something modified");

        PartRevisionDTO updatedPartRevision = partApi.updatePartIteration(TestConfig.WORKSPACE, checkedOutPart.getNumber(), checkedOutPart.getVersion(), 2, lastIteration);

        PartIterationDTO updatedIteration = LastIterationHelper.getLastIteration(updatedPartRevision);
        Assert.assertNotNull(updatedIteration.getModificationDate());
        lastIteration.setModificationDate(updatedIteration.getModificationDate());
        Assert.assertEquals(lastIteration,updatedIteration);

        // Check in
        checkedInPart = partApi.checkIn(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion());
        Assert.assertNull(checkedInPart.getCheckOutUser());

        // Release
        PartRevisionDTO releasedPart = partApi.releasePartRevision(TestConfig.WORKSPACE, checkedInPart.getNumber(), checkedInPart.getVersion());
        Assert.assertEquals(releasedPart.getStatus(), PartRevisionDTO.StatusEnum.RELEASED);

        // Mark as obsolete
        PartRevisionDTO obsoletePart = partApi.markPartRevisionAsObsolete(TestConfig.WORKSPACE, releasedPart.getNumber(), releasedPart.getVersion());
        Assert.assertEquals(obsoletePart.getStatus(), PartRevisionDTO.StatusEnum.OBSOLETE);

    }

}
