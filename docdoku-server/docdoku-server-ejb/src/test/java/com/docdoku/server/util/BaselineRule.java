package com.docdoku.server.util;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asmae on 19/12/14.
 */
public class BaselineRule implements TestRule {

    private String name ;
    private ProductBaseline.BaselineType type;
    private String description;
    private Workspace workspace;
    private User user1;
    private PartMaster partMaster;
    private ConfigurationItemKey configurationItemKey;
    private ConfigurationItem configurationItem;

    public BaselineRule(String baselineName,ProductBaseline.BaselineType type,String description,String workspaceId,String login,String partId,String productId,boolean released,ACL.Permission permission){
        name = baselineName;
        this.type = type;
        this.description = description;
        this.workspace = new Workspace(workspaceId);
        user1 = new User(workspace, login,login,login+"@docdoku.com", "en");
        partMaster = new PartMaster(workspace, partId, user1);
        configurationItemKey = new ConfigurationItemKey("workspace1",productId);
        configurationItem = new ConfigurationItem(workspace, productId, "description");
        partMaster.setPartRevisions(new ArrayList<PartRevision>());
        if (released){
            List<PartRevision> revisions = new ArrayList<PartRevision>();
            List<PartIteration> iterationLists = new ArrayList<PartIteration>();
            PartRevision revision = new PartRevision(partMaster, "A", user1);
            iterationLists.add(new PartIteration(revision, user1));
            revision.setPartIterations(iterationLists);
            revision.setStatus(PartRevision.RevisionStatus.RELEASED);
            if (permission != null)
            {
                ACL acl = new ACL();
                acl.addEntry(user1,permission);
                revision.setACL(acl);
            }

            revisions.add(revision);
            partMaster.setPartRevisions(revisions);
        }

        configurationItem.setDesignItem(partMaster);
    }

    public BaselineRule(String baselineName,ProductBaseline.BaselineType type,String description,String workspaceId,String login,String partId,String productId,boolean released,boolean checkouted){
        this(baselineName,type,description,workspaceId,login,partId,productId,released,null);
        if (checkouted){
            this.partMaster.getLastReleasedRevision().getIteration(1).getPartRevision().setCheckOutUser(this.user1);
        }


    }


    @Override
    public Statement apply(Statement statement, Description description) {
        return new BaselineStatement(statement,this.configurationItem);
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

    public User getUser1() {
        return user1;
    }

    public PartMaster getPartMaster() {
        return partMaster;
    }

    public ConfigurationItemKey getConfigurationItemKey() {
        return configurationItemKey;
    }
}
