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
import com.docdoku.api.models.*;
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.api.services.SharedApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;

@RunWith(JUnit4.class)
public class SharedPartApiTest {


    @Test
    public void privatePartShareTests() throws ApiException {

        PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);
        PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
        SharedApi sharedApi = new SharedApi(TestConfig.GUEST_CLIENT);

        // Create a part
        PartCreationDTO partCreationDTO = new PartCreationDTO();
        partCreationDTO.setNumber(UUID.randomUUID().toString().substring(0, 8));
        partCreationDTO.setName("PublicPart");

        PartRevisionDTO part = partsApi.createNewPart(TestConfig.WORKSPACE, partCreationDTO);
        part = partApi.checkIn(part.getWorkspaceId(),part.getNumber(),part.getVersion(),"");

        // Try guest access (should fail)
        try {
            sharedApi.getPublicSharedPartRevision(part.getWorkspaceId(), part.getNumber(), part.getVersion());
        } catch (ApiException e) {
            int statusCode = TestConfig.GUEST_CLIENT.getStatusCode();
            Assert.assertEquals(403, statusCode);
        }

        // Create a private share
        SharedPartDTO sharedPartDTO = new SharedPartDTO();
        SharedPartDTO sharedPart =
                partsApi.createSharedPart(part.getWorkspaceId(), part.getNumber(),
                        part.getVersion(), sharedPartDTO);


        // Try guest access with shared entity uuid
        PartRevisionDTO partWithSharedEntity = sharedApi.getPartWithSharedEntity(sharedPart.getUuid(), null);
        Assert.assertEquals(part.getNumber(), partWithSharedEntity.getNumber());

    }

    @Test
    public void publicPartShareTests() throws ApiException {

        PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);
        PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
        SharedApi sharedApi = new SharedApi(TestConfig.GUEST_CLIENT);

        // Create a part
        PartCreationDTO partCreationDTO = new PartCreationDTO();
        partCreationDTO.setNumber(UUID.randomUUID().toString().substring(0, 8));
        partCreationDTO.setName("PublicPart");

        PartRevisionDTO part = partsApi.createNewPart(TestConfig.WORKSPACE, partCreationDTO);
        part = partApi.checkIn(part.getWorkspaceId(), part.getNumber(), part.getVersion(), "");

        // Try guest access (should fail)
        try{
            sharedApi.getPublicSharedPartRevision(part.getWorkspaceId(), part.getNumber(), part.getVersion());
        } catch (ApiException e){
            int statusCode = TestConfig.GUEST_CLIENT.getStatusCode();
            Assert.assertEquals(403, statusCode);
        }

        // publish
        partApi.publishPartRevision(part.getWorkspaceId(), part.getNumber(), part.getVersion(), "");

        // Try guest access with part key
        PartRevisionDTO publicSharedPartRevision = sharedApi.getPublicSharedPartRevision(part.getWorkspaceId(), part.getNumber(), part.getVersion());
        Assert.assertEquals(part.getNumber(), publicSharedPartRevision.getNumber());

        // un publish
        partApi.unPublishPartRevision(part.getWorkspaceId(), part.getNumber(), part.getVersion(), "");

        // Try guest access (should fail)
        try{
            sharedApi.getPublicSharedPartRevision(part.getWorkspaceId(), part.getNumber(), part.getVersion());
        } catch (ApiException e){
            int statusCode = TestConfig.GUEST_CLIENT.getStatusCode();
            Assert.assertEquals(403, statusCode);
        }

    }



}
