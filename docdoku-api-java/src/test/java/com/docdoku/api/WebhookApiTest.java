/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
import com.docdoku.api.models.WebhookDTO;
import com.docdoku.api.models.WorkspaceDTO;
import com.docdoku.api.services.WebhookApi;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class WebhookApiTest {

    //    private PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
//    private PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);
    private WebhookApi webhookApi = new WebhookApi(TestConfig.REGULAR_USER_CLIENT);
    private static WorkspaceDTO workspace;

    @BeforeClass
    public static void initWorkspace() throws ApiException {
        workspace = TestUtils.createWorkspace(WebhookApiTest.class.getName());
    }

    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        TestUtils.deleteWorkspace(workspace);
    }

    @Test
    public void createWebhookTest() throws ApiException {
        String name = "My first webhook";
        WebhookDTO webhookDTO = new WebhookDTO();
        webhookDTO.setName(name);
        webhookDTO.setActive(true);
        WebhookDTO webhook = webhookApi.createWebhook(workspace.getId(), webhookDTO);

        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhookDTO.getActive(), webhook.getActive());
        Assert.assertEquals(webhookDTO.getName(), webhook.getName());

        List<WebhookDTO> webhooks = webhookApi.getWebhooks(workspace.getId());
        Assert.assertNotNull(webhooks);
        Assert.assertFalse(webhooks.isEmpty());
        Assert.assertTrue(webhooks.contains(webhook));

        WebhookDTO webhookFetch = webhookApi.getWebhook(workspace.getId(), webhook.getId());
        Assert.assertNotNull(webhookFetch);
        Assert.assertEquals(webhook, webhookFetch);
    }


}
