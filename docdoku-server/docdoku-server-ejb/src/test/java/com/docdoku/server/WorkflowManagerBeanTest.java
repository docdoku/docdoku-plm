package com.docdoku.server;

import com.docdoku.core.services.IUserManagerLocal;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

public class WorkflowManagerBeanTest {


    @InjectMocks
    WorkflowManagerBean workflowManagerBean = new WorkflowManagerBean();

    @Mock
    private EntityManager em;
    @Mock
    private IUserManagerLocal userManager;

    @Test
    public void testRemoveACLFromWorkflow() throws Exception {

    }

    @Test
    public void testUpdateACLForWorkflowWithNoACL() throws Exception {

    }
    @Test
    public void testUpdateACLForWorkflowANExistentACL() throws Exception {

    }
    @Test
    public void testUpdateACLForWorkflowUserNoWriteAccess() throws Exception {

    }
    @Test
    public void testUpdateACLForWorkflowUserNoWriteAccessInGrpFullAccess() throws Exception {

    }
}