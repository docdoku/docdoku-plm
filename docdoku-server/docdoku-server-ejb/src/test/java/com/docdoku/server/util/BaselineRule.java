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

/*
 *
 * @author Asmae CHADID on 19/12/14.
 */
     public class BaselineRule implements TestRule {

    private String name ;
    private ProductBaseline.BaselineType type;
    private String description;
    private Workspace workspace;
    private User user;
    private PartMaster partMaster;
    private ConfigurationItemKey configurationItemKey;
    private ConfigurationItem configurationItem;
    private List<String> substituteLinks = new ArrayList<>();
    private List<String> optionalUsageLinks = new ArrayList<>();;

    public BaselineRule(String baselineName,ProductBaseline.BaselineType type,String description,String workspaceId,String login,String partId,String productId,boolean released){
        name = baselineName;
        this.type = type;
        this.description = description;
        this.workspace = new Workspace(workspaceId);
        user = new User(workspace, login,login,login+"@docdoku.com", "en");
        partMaster = new PartMaster(workspace, partId, user);
        configurationItemKey = new ConfigurationItemKey("workspace1",productId);
        configurationItem = new ConfigurationItem(user,workspace, productId, "description");
        partMaster.setPartRevisions(new ArrayList<PartRevision>());
        if (released){
            List<PartRevision> revisions = new ArrayList<PartRevision>();
            List<PartIteration> iterationLists = new ArrayList<PartIteration>();
            PartRevision revision = new PartRevision(partMaster, "A", user);
            iterationLists.add(new PartIteration(revision, user));
            revision.setPartIterations(iterationLists);
            revision.setStatus(PartRevision.RevisionStatus.RELEASED);
            revisions.add(revision);
            partMaster.setPartRevisions(revisions);
        }

        configurationItem.setDesignItem(partMaster);
    }

    public BaselineRule(String baselineName,ProductBaseline.BaselineType type,String description,String workspaceId,String login,String partId,String productId,boolean released,boolean checkedOut){
        this(baselineName,type,description,workspaceId,login,partId,productId,released);
        if (checkedOut){
            this.partMaster.getLastReleasedRevision().getIteration(1).getPartRevision().setCheckOutUser(this.user);
        }
    }


    @Override
    public Statement apply(Statement statement, Description description) {
        return new BaselineStatement(statement,this.configurationItem);
    }

    public PartLink getRootPartUsageLink() {
       return new PartLink() {
           @Override
           public int getId() {
               return 1;
           }

           @Override
           public Character getCode() {
               return '-';
           }

           @Override
           public String getFullId() {
               return "-1";
           }

           @Override
           public double getAmount() {
               return 1;
           }

           @Override
           public String getUnit() {
               return null;
           }

           @Override
           public String getComment() {
               return null;
           }

           @Override
           public boolean isOptional() {
               return false;
           }

           @Override
           public PartMaster getComponent() {
               return configurationItem.getDesignItem();
           }

           @Override
           public List<PartSubstituteLink> getSubstitutes() {
               return null;
           }

           @Override
           public String getReferenceDescription() {
               return null;
           }

           @Override
           public List<CADInstance> getCadInstances() {
               List<CADInstance> cads = new ArrayList<>();
               CADInstance cad = new CADInstance(0d, 0d, 0d, 0d, 0d, 0d);
               cad.setId(0);
               cads.add(cad);
               return cads;
           }
       };
    }

    public class BaselineStatement extends Statement{
        private final Statement statement;
        public BaselineStatement(Statement s,ConfigurationItem configurationItem){
            this.statement = s;
        }
        @Override
        public void evaluate() throws Throwable {
            statement.evaluate();
        }
    }

    public ConfigurationItem getConfigurationItem(){
        return this.configurationItem;
    }

    public String getName() {
        return name;
    }

    public ProductBaseline.BaselineType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public User getUser() {
        return user;
    }

    public PartMaster getPartMaster() {
        return partMaster;
    }

    public ConfigurationItemKey getConfigurationItemKey() {
        return configurationItemKey;
    }

    public List<String> getOptionalUsageLinks() {
        return optionalUsageLinks;
    }

    public List<String> getSubstituteLinks() {
        return substituteLinks;
    }
}
