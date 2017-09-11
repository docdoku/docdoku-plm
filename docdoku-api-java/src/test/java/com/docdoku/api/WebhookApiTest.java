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
import com.docdoku.api.client.ApiResponse;
import com.docdoku.api.models.*;
import com.docdoku.api.services.WebhookApi;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class WebhookApiTest {

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
    public void webhookBasicCrudTests() throws ApiException {
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

        webhook.setName("New name");
        WebhookDTO updatedWebhook = webhookApi.updateWebhook(workspace.getId(), webhook.getId(), webhook);
        Assert.assertNotNull(updatedWebhook);
        Assert.assertEquals(webhook, updatedWebhook);

        webhook.setActive(false);
        WebhookDTO webhookDisabled = webhookApi.updateWebhook(workspace.getId(), webhook.getId(), webhook);
        Assert.assertNotNull(webhookDisabled);
        Assert.assertFalse(webhookDisabled.getActive());

        ApiResponse<Void> voidApiResponse = webhookApi.deleteWebhookWithHttpInfo(workspace.getId(), webhook.getId());
        Assert.assertEquals(204, voidApiResponse.getStatusCode());

    }

    @Test
    public void configurationsTest() throws ApiException {

        // Simple web hook

        WebhookDTO webhookDTO = new WebhookDTO();
        webhookDTO.setName("A simple post to an URI");
        webhookDTO.setActive(true);
        WebhookDTO webhook = webhookApi.createWebhook(workspace.getId(), webhookDTO);

        SimpleWebhookDTO simpleWebhookDTO = new SimpleWebhookDTO();
        simpleWebhookDTO.setMethod("POST");
        simpleWebhookDTO.setUri("http://localhost:8080");
        simpleWebhookDTO.setAuthorization("some authorization header");

        SimpleWebhookDTO simpleApp = webhookApi.configureSimpleWebhook(workspace.getId(), webhook.getId(), simpleWebhookDTO);
        Assert.assertNotNull(simpleApp);

        // SNS web hook

        webhookDTO.setName("A sns app");
        webhookDTO.setActive(true);
        webhook = webhookApi.updateWebhook(workspace.getId(), webhook.getId(), webhookDTO);

        SNSWebhookDTO snsWebhookDTO = new SNSWebhookDTO();
        snsWebhookDTO.setAwsAccount("My account");
        snsWebhookDTO.setAwsSecret("My secret");
        snsWebhookDTO.setRegion("My region");
        snsWebhookDTO.setTopicArn("arn::topic:arn...");

        SNSWebhookDTO snsApp = webhookApi.configureSNSWebhook(workspace.getId(), webhook.getId(), snsWebhookDTO);
        Assert.assertNotNull(snsApp);
        Assert.assertEquals("My account", snsApp.getAwsAccount());

    }

    @Test
    public void directSimpleWebhookCreationTest() throws ApiException {

        // Simple web hook
        WebhookDTO webhookDTO = new WebhookDTO();
        webhookDTO.setName("A simple post to an URI");
        webhookDTO.setActive(true);
        webhookDTO.setAppName("SIMPLEWEBHOOK");
        List<WebhookAppParameterDTO> parameters = new ArrayList<>();

        WebhookAppParameterDTO method = new WebhookAppParameterDTO();
        method.setName("method");
        method.setValue("POST");
        parameters.add(method);

        WebhookAppParameterDTO uri = new WebhookAppParameterDTO();
        uri.setName("uri");
        uri.setValue("http://localhost:9001");
        parameters.add(uri);

        WebhookAppParameterDTO authorization = new WebhookAppParameterDTO();
        authorization.setName("authorization");
        authorization.setValue("some authorization field");
        parameters.add(authorization);

        webhookDTO.setParameters(parameters);

        WebhookDTO webhook = webhookApi.createWebhook(workspace.getId(), webhookDTO);

        Assert.assertNotNull(webhook);
        Assert.assertEquals(parameters, webhook.getParameters());

    }


    @Test
    public void directSNSWebhookCreationTest() throws ApiException {

        // Simple web hook
        WebhookDTO webhookDTO = new WebhookDTO();
        webhookDTO.setName("A simple post to an URI");
        webhookDTO.setActive(true);
        webhookDTO.setAppName("SNSWEBHOOK");
        List<WebhookAppParameterDTO> parameters = new ArrayList<>();

        WebhookAppParameterDTO topicArn = new WebhookAppParameterDTO();
        topicArn.setName("topicArn");
        topicArn.setValue("some topic arn");
        parameters.add(topicArn);

        WebhookAppParameterDTO region = new WebhookAppParameterDTO();
        region.setName("region");
        region.setValue("us-west-2");
        parameters.add(region);

        WebhookAppParameterDTO awsAccount = new WebhookAppParameterDTO();
        awsAccount.setName("awsAccount");
        awsAccount.setValue("some awsAccount");
        parameters.add(awsAccount);

        WebhookAppParameterDTO awsSecret = new WebhookAppParameterDTO();
        awsSecret.setName("awsSecret");
        awsSecret.setValue("some awsSecret");
        parameters.add(awsSecret);

        webhookDTO.setParameters(parameters);

        WebhookDTO webhook = webhookApi.createWebhook(workspace.getId(), webhookDTO);

        Assert.assertNotNull(webhook);
        Assert.assertEquals(parameters, webhook.getParameters());

    }


}
