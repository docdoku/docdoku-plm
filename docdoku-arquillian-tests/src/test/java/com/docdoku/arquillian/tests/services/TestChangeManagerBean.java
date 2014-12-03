package com.docdoku.arquillian.tests.services;

import com.docdoku.core.change.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IChangeManagerLocal;

import com.docdoku.server.esindexer.ESIndexer;
import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;


import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

/**
 * @author Asmae CHADID
 */
@LocalBean
@Stateless
public class TestChangeManagerBean {


    @EJB
    private IChangeManagerLocal changeManagerLocal;

    @EJB
    private ESIndexer esIndexer;

    private ProgrammaticLogin loginP = new ProgrammaticLogin();
    private String password = "password";

    public ChangeRequest createRequest(String login, String pWorkspaceId, String name, String description, int milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException {
        loginP.login(login, password.toCharArray());
        ChangeRequest changeRequest = changeManagerLocal.createChangeRequest(pWorkspaceId, name, description, milestone, priority, assignee, category);
        loginP.logout();
        return changeRequest;

    }

    public ChangeIssue createIssue(String login, String pWorkspaceId, String name, String description, String milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException {
        loginP.login(login, password.toCharArray());
        ChangeIssue changeIssue = changeManagerLocal.createChangeIssue(pWorkspaceId, name, description, milestone, priority, assignee, category);
        loginP.logout();
        return changeIssue;

    }

    public ChangeOrder createOrder(String login, String pWorkspaceId, String name, String description, int milestone, ChangeItem.Priority priority, String assignee, ChangeItem.Category category) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException {
        loginP.login(login, password.toCharArray());
        ChangeOrder changeOrder = changeManagerLocal.createChangeOrder(pWorkspaceId, name, description, milestone, priority, assignee, category);
        loginP.logout();
        return changeOrder;
    }

    public Milestone createChangeMilestone(String login, String pWorkspaceId, String title, String description, Date dueDate) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, MilestoneAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        Milestone milestone = changeManagerLocal.createChangeMilestone(pWorkspaceId, title, description, dueDate);
        loginP.logout();
        return milestone;
    }

    public List<ChangeRequest> getAllChangeRequest(String login, String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        return changeManagerLocal.getChangeRequests(pWorkspaceId);
    }

    public List<ChangeIssue> getAllChangeIssue(String login, String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        return changeManagerLocal.getChangeIssues(pWorkspaceId);
    }

    public List<ChangeOrder> getAllChangeOrder(String login, String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        return changeManagerLocal.getChangeOrders(pWorkspaceId);
    }

    public List<Milestone>getAllMilestones(String login, String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        return changeManagerLocal.getChangeMilestones(pWorkspaceId);
    }


}
