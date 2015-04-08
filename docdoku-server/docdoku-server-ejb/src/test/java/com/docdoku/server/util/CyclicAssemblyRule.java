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

package com.docdoku.server.util;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.product.*;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Morgan Guimard
 */
public class CyclicAssemblyRule implements TestRule {

    private Workspace workspace = new Workspace("workspace");
    private PartMaster p1 = new PartMaster(workspace,"P1");
    private PartMaster p2 = new PartMaster(workspace,"P2");
    private ConfigurationItemKey configurationItemKey;
    private ConfigurationItem configurationItem;
    private User user;

    public CyclicAssemblyRule(String login){

        user = new User(workspace, login , login ,login+"@docdoku.com", "en");

        // Create P1
        List<PartRevision> revisionsP1 = new ArrayList<>();
        List<PartIteration> iterationListsP1 = new ArrayList<>();
        PartRevision revisionP1 = new PartRevision(p1, "A", user);
        iterationListsP1.add(new PartIteration(revisionP1, user));
        revisionP1.setPartIterations(iterationListsP1);
        revisionsP1.add(revisionP1);
        p1.setPartRevisions(revisionsP1);

        // Create P2
        List<PartRevision> revisionsP2 = new ArrayList<>();
        List<PartIteration> iterationListsP2 = new ArrayList<>();
        PartRevision revisionP2 = new PartRevision(p2, "A", user);
        iterationListsP2.add(new PartIteration(revisionP2, user));
        revisionP2.setPartIterations(iterationListsP2);
        revisionsP2.add(revisionP2);
        p2.setPartRevisions(revisionsP2);

        // Add P2 to P1 usage links
        PartUsageLink p1p2 = new PartUsageLink(p2,1, null, false);
        p1.getLastRevision().getLastIteration().getComponents().add(p1p2);

        // Add P1 to P2 usage links
        PartUsageLink p2p1 = new PartUsageLink(p1,1, null, false);
        p2.getLastRevision().getLastIteration().getComponents().add(p2p1);

        configurationItemKey = new ConfigurationItemKey(getWorkspaceId(),"productId");
        configurationItem = new ConfigurationItem(user,workspace, "productId", "description");
        configurationItem.setDesignItem(p1);

    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new CyclicAssemblyStatement(statement);
    }

    public String getWorkspaceId() {
        return workspace.getId();
    }

    public PartMaster getP1() {
        return p1;
    }

    public PartMaster getP2() {
        return p2;
    }

    public class CyclicAssemblyStatement extends Statement{
        private final Statement statement;
        public CyclicAssemblyStatement(Statement s){
            this.statement = s;
        }
        @Override
        public void evaluate() throws Throwable {
            statement.evaluate();
        }
    }

    public ConfigurationItemKey getConfigurationItemKey() {
        return configurationItemKey;
    }

    public String getName() {
        return "baseline";
    }

    public ProductBaseline.BaselineType getType() {
        return ProductBaseline.BaselineType.LATEST;
    }

    public String getDescription() {
        return "description";
    }

    public User getUser() {
        return user;
    }
}
