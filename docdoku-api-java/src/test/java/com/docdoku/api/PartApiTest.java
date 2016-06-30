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

import java.util.UUID;

@RunWith(JUnit4.class)
public class PartApiTest {

    private PartApi partApi = new PartApi(TestConfig.BASIC_CLIENT);
    private PartsApi partsApi = new PartsApi(TestConfig.BASIC_CLIENT);

    @Test
    public void partApiUsageTests() throws ApiException {

        // Create a part
        PartCreationDTO part = new PartCreationDTO();
        part.setNumber(UUID.randomUUID().toString().substring(0, 6));
        part.setName("GeneratedPart");

        PartRevisionDTO createdPart = partsApi.createNewPart(TestConfig.WORKSPACE, part);
        Assert.assertEquals(createdPart.getNumber(), part.getNumber());

        // Check in
        PartRevisionDTO checkedInPart = partApi.checkIn(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion(), "");
        Assert.assertEquals(checkedInPart.getNumber(),part.getNumber());
        Assert.assertEquals(LastIterationHelper.getLastIteration(checkedInPart).getIteration(), Integer.valueOf("1"));

        // Check out
        PartRevisionDTO checkedOutPart = partApi.checkOut(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion(), "");
        Assert.assertEquals(checkedOutPart.getNumber(),part.getNumber());
        Assert.assertEquals(LastIterationHelper.getLastIteration(checkedOutPart).getIteration(),Integer.valueOf("2"));

        // Undo check out
        PartRevisionDTO undoCheckOutPart= partApi.undoCheckOut(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion(), "");
        Assert.assertEquals(undoCheckOutPart,checkedInPart);

        // Check out
        checkedOutPart = partApi.checkOut(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion(), "");

        // Edit
        PartIterationDTO lastIteration = LastIterationHelper.getLastIteration(checkedOutPart);
        Assert.assertNull(lastIteration.getModificationDate());
        lastIteration.setIterationNote("Something modified");

        PartRevisionDTO updatedPartRevision = partApi.updatePartIteration(TestConfig.WORKSPACE, checkedOutPart.getNumber(), checkedOutPart.getVersion(), 2, lastIteration);

        PartIterationDTO updatedIteration = LastIterationHelper.getLastIteration(updatedPartRevision);
        Assert.assertNotNull(updatedIteration.getModificationDate());
        lastIteration.setModificationDate(updatedIteration.getModificationDate());
        Assert.assertEquals(lastIteration,updatedIteration);

        // Checkin
        checkedInPart = partApi.checkIn(TestConfig.WORKSPACE, createdPart.getNumber(), createdPart.getVersion(), "");
        Assert.assertEquals(checkedInPart.getPartIterations().size(),2);

    }



}
