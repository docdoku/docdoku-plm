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
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.ChangeItemsApi;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class ChangeItemApiTest {

    private static WorkspaceDTO workspace;

    @BeforeClass
    public static void initWorkspace() throws ApiException {
        workspace = TestUtils.createWorkspace(ChangeItemApiTest.class.getName());
    }

    @AfterClass
    public static void deleteWorkspace() throws ApiException {
        TestUtils.deleteWorkspace(workspace);
    }

    @Test
    public void createChangeItems() throws ApiException {

        PartRevisionDTO part = TestUtils.createPart(workspace.getId(), "Generated part for change issue tests");
        PartIterationDTO partIterationDTO = LastIterationHelper.getLastIteration(part);
        PartIterationListDTO partIterationListDTO = new PartIterationListDTO();
        partIterationListDTO.setParts(Collections.singletonList(partIterationDTO));

        DocumentRevisionDTO document = TestUtils.createDocument(workspace.getId(), "Generated document for change issue tests");
        DocumentIterationDTO documentIterationDTO = LastIterationHelper.getLastIteration(document);
        DocumentIterationListDTO documentIterationListDTO = new DocumentIterationListDTO();
        documentIterationListDTO.setDocuments(Collections.singletonList(documentIterationDTO));

        ChangeItemsApi changeItemsApi = new ChangeItemsApi(TestConfig.REGULAR_USER_CLIENT);

        ChangeIssueDTO issueData = new ChangeIssueDTO();
        issueData.setName("Issue 1");
        issueData.setAssignee(TestConfig.LOGIN);
        issueData.setDescription("Issue 1 description");
        issueData.setCategory(ChangeIssueDTO.CategoryEnum.ADAPTIVE);
        issueData.setAuthor(TestConfig.LOGIN);
        issueData.setPriority(ChangeIssueDTO.PriorityEnum.EMERGENCY);

        ChangeIssueDTO issue = changeItemsApi.createIssue(workspace.getId(), issueData);
        Assert.assertNotNull(issue);
        Assert.assertEquals(issueData.getName(), issue.getName());
        Assert.assertEquals(issueData.getDescription(), issue.getDescription());


        partIterationListDTO.setParts(Collections.singletonList(partIterationDTO));
        issue = changeItemsApi.saveChangeIssueAffectedParts(workspace.getId(), issue.getId(), partIterationListDTO);
        Assert.assertNotNull(issue);
        Assert.assertEquals(1, issue.getAffectedParts().size());

        issue = changeItemsApi.saveChangeIssueAffectedDocuments(workspace.getId(), issue.getId(), documentIterationListDTO);
        Assert.assertNotNull(issue);
        Assert.assertEquals(1, issue.getAffectedDocuments().size());

        TagListDTO tagList = new TagListDTO();
        TagDTO tag = new TagDTO();
        tag.setLabel("MY TAG");
        tagList.setTags(Collections.singletonList(tag));
        issue = changeItemsApi.saveChangeItemTags(workspace.getId(), issue.getId(), tagList);

        Assert.assertNotNull(issue);
        Assert.assertEquals(1, issue.getTags().size());
        Assert.assertEquals("MY TAG", issue.getTags().get(0));

        // Request

        ChangeRequestDTO requestData = new ChangeRequestDTO();
        requestData.setName("Request 1");
        requestData.setAssignee(TestConfig.LOGIN);
        requestData.setDescription("Request 1 description");
        requestData.setCategory(ChangeRequestDTO.CategoryEnum.ADAPTIVE);
        requestData.setAuthor(TestConfig.LOGIN);
        requestData.setPriority(ChangeRequestDTO.PriorityEnum.EMERGENCY);

        ChangeRequestDTO request = changeItemsApi.createRequest(workspace.getId(), requestData);
        Assert.assertNotNull(request);
        Assert.assertEquals(requestData.getName(), request.getName());
        Assert.assertEquals(requestData.getDescription(), request.getDescription());


        request = changeItemsApi.saveChangeRequestAffectedParts(workspace.getId(), request.getId(), partIterationListDTO);
        Assert.assertNotNull(request);
        Assert.assertEquals(1, request.getAffectedParts().size());

        request = changeItemsApi.saveChangeRequestAffectedDocuments(workspace.getId(), request.getId(), documentIterationListDTO);
        Assert.assertNotNull(request);
        Assert.assertEquals(1, request.getAffectedDocuments().size());

        request = changeItemsApi.saveChangeRequestTags(workspace.getId(), request.getId(), tagList);

        Assert.assertNotNull(request);
        Assert.assertEquals(1, request.getTags().size());
        Assert.assertEquals("MY TAG", request.getTags().get(0));

        ChangeIssueListDTO issueList = new ChangeIssueListDTO();
        issueList.setIssues(Collections.singletonList(issue));
        request = changeItemsApi.saveAffectedIssues(workspace.getId(), request.getId(), issueList);

        Assert.assertNotNull(request);
        Assert.assertEquals(1, request.getAddressedChangeIssues().size());
        Assert.assertEquals(issue.getId(), request.getAddressedChangeIssues().get(0).getId());


        // Order

        ChangeOrderDTO orderData = new ChangeOrderDTO();
        orderData.setName("Order 1");
        orderData.setAssignee(TestConfig.LOGIN);
        orderData.setDescription("Order 1 description");
        orderData.setCategory(ChangeOrderDTO.CategoryEnum.ADAPTIVE);
        orderData.setAuthor(TestConfig.LOGIN);
        orderData.setPriority(ChangeOrderDTO.PriorityEnum.EMERGENCY);

        ChangeOrderDTO order = changeItemsApi.createOrder(workspace.getId(), orderData);
        Assert.assertNotNull(order);
        Assert.assertEquals(orderData.getName(), order.getName());
        Assert.assertEquals(orderData.getDescription(), order.getDescription());


        order = changeItemsApi.saveChangeOrderAffectedParts(workspace.getId(), order.getId(), partIterationListDTO);
        Assert.assertNotNull(order);
        Assert.assertEquals(1, order.getAffectedParts().size());

        order = changeItemsApi.saveChangeOrderAffectedDocuments(workspace.getId(), order.getId(), documentIterationListDTO);
        Assert.assertNotNull(order);
        Assert.assertEquals(1, order.getAffectedDocuments().size());

        order = changeItemsApi.saveChangeOrderTags(workspace.getId(), order.getId(), tagList);

        Assert.assertNotNull(order);
        Assert.assertEquals(1, order.getTags().size());
        Assert.assertEquals("MY TAG", order.getTags().get(0));

        ChangeRequestListDTO requestList = new ChangeRequestListDTO();
        requestList.setRequests(Collections.singletonList(request));

        order = changeItemsApi.saveAffectedRequests(workspace.getId(), order.getId(), requestList);

        Assert.assertNotNull(order);
        Assert.assertEquals(1, order.getAddressedChangeRequests().size());
        Assert.assertEquals(request.getId(), order.getAddressedChangeRequests().get(0).getId());

        List<ChangeIssueDTO> issues = changeItemsApi.getIssues(workspace.getId());
        Assert.assertEquals(1, issues.size());

        List<ChangeRequestDTO> requests = changeItemsApi.getRequests(workspace.getId());
        Assert.assertEquals(1, requests.size());

        List<ChangeOrderDTO> orders = changeItemsApi.getOrders(workspace.getId());
        Assert.assertEquals(1, orders.size());

        changeItemsApi.removeOrder(workspace.getId(), order.getId());
        changeItemsApi.removeRequest(workspace.getId(), request.getId());
        changeItemsApi.removeIssue(workspace.getId(), issue.getId());

        issues = changeItemsApi.getIssues(workspace.getId());
        Assert.assertEquals(0, issues.size());

        requests = changeItemsApi.getRequests(workspace.getId());
        Assert.assertEquals(0, requests.size());

        orders = changeItemsApi.getOrders(workspace.getId());
        Assert.assertEquals(0, orders.size());

    }


}
