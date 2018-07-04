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
import com.docdoku.api.models.PartCreationDTO;
import com.docdoku.api.models.QueryDTO;
import com.docdoku.api.models.QueryRuleDTO;
import com.docdoku.api.models.WorkspaceDTO;
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.api.services.WorkspacesApi;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@RunWith(JUnit4.class)
public class PartQueryApiTest {

    private static WorkspaceDTO workspace;
    private static PartCreationDTO part;
    private static PartApi partApi = new PartApi(TestConfig.REGULAR_USER_CLIENT);
    private static PartsApi partsApi = new PartsApi(TestConfig.REGULAR_USER_CLIENT);

    @BeforeClass
    public static void initParts() throws ApiException {
        workspace = TestUtils.createWorkspace(PartQueryApiTest.class.getName());
        part = new PartCreationDTO();
        part.setNumber(TestUtils.randomString());
        part.setName(TestUtils.randomString());
        partsApi.createNewPart(workspace.getId(), part);
        partApi.checkIn(workspace.getId(), part.getNumber(), "A");
    }

    @AfterClass
    public static void clearData() throws ApiException {
        new WorkspacesApi(TestConfig.REGULAR_USER_CLIENT).deleteWorkspace(workspace.getId());
    }

    @Test
    public void partQueryTests() throws ApiException, IOException {

        QueryDTO queryDTO = new QueryDTO();
        QueryRuleDTO mainRule = new QueryRuleDTO();
        QueryRuleDTO partNumberRule = new QueryRuleDTO();

        partNumberRule.setField("pm.number");
        partNumberRule.setId("pm.number");
        partNumberRule.setOperator("equal");
        partNumberRule.setType("string");
        partNumberRule.getValues().add(part.getNumber());

        mainRule.setCondition("AND");
        mainRule.getRules().add(partNumberRule);

        queryDTO.setSelects(Arrays.asList("pm.number", "pm.name"));
        queryDTO.setOrderByList(Collections.singletonList("pm.number"));
        queryDTO.setGroupedByList(Collections.singletonList("pm.author"));

        queryDTO.setName("QUERY-" + TestUtils.randomString());
        queryDTO.setQueryRule(mainRule);

        File file = partsApi.runCustomQuery(workspace.getId(), queryDTO, false, "JSON");
        String result = FileUtils.readFileToString(file);
        Assert.assertNotNull(result);
        JsonArray rows = new Gson().fromJson(result, JsonArray.class);
        Assert.assertEquals(1, rows.size());
        JsonObject row = rows.get(0).getAsJsonObject();

        Assert.assertEquals(part.getNumber() + "-A", row.get("pr.partKey").getAsString());
        Assert.assertEquals(part.getNumber(), row.get("pm.number").getAsString());
        Assert.assertEquals(part.getName(), row.get("pm.name").getAsString());

    }

    public void simpleOR() {
        QueryRuleDTO ruleA = new QueryRuleDTO();

        ruleA.setField("pm.number");
        ruleA.setOperator("begins_with");
        ruleA.setType("string");
        ruleA.setValues(Collections.singletonList("A"));


        QueryRuleDTO ruleB = new QueryRuleDTO();

        ruleB.setField("pm.number");
        ruleB.setOperator("begins_with");
        ruleB.setType("string");
        ruleB.setValues(Collections.singletonList("B"));

        QueryRuleDTO ruleAorB = new QueryRuleDTO();
        ruleAorB.setCondition("OR");
        ruleAorB.setRules(Arrays.asList(ruleA, ruleB));
    }
}
