package com.docdoku.arquillian.tests.services;

import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.esindexer.ESIndexer;
import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;


import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.Map;

/**
 * @author Asmae CHADID
 */
@LocalBean
@Stateless
public class TestPartManagerBean {

    @EJB
    private IProductManagerLocal productManagerLocal;

    @EJB
    private ESIndexer esIndexer;

    private ProgrammaticLogin loginP = new ProgrammaticLogin();
    private String password = "password";

    public PartMasterTemplate createPartMasterTemplate(String login, String pWorkspaceId, String pId, String pPartType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated, boolean attributesLocked) throws NotAllowedException, WorkspaceNotFoundException, CreationException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException {
        loginP.login(login, password.toCharArray());
        PartMasterTemplate partMasterTemplate = productManagerLocal.createPartMasterTemplate(pWorkspaceId, pId, pPartType, pMask, pAttributeTemplates, idGenerated, attributesLocked);
        loginP.logout();
        return  partMasterTemplate;
    }

    public PartMaster createPartMaster(String login, String pWorkspaceId, String pNumber, String pName, boolean pStandardPart, String pWorkflowModelId, String pPartRevisionDescription, String templateId, Map<String, String> roleMappings, ACLUserEntry[] userEntries, ACLUserGroupEntry[] userGroupEntries) throws NotAllowedException, WorkspaceNotFoundException, CreationException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException, FileAlreadyExistsException, PartMasterTemplateNotFoundException, PartMasterAlreadyExistsException, RoleNotFoundException, WorkflowModelNotFoundException {
        loginP.login(login, password.toCharArray());
        PartMaster partMaster = productManagerLocal.createPartMaster(pWorkspaceId,pNumber,pName,pStandardPart,pWorkflowModelId,pPartRevisionDescription,templateId,roleMappings,userEntries,userGroupEntries);
        loginP.logout();
        return  partMaster;
    }


    public PartMaster findPartMasterById(String login, String pWorkspaceId, String pNumber) throws Exception{
        loginP.login(login, password.toCharArray());
        PartMaster partMaster =  productManagerLocal.findPartMasters(pWorkspaceId,pNumber,1).get(0);
        loginP.logout();
        return partMaster;
    }
    public int findAllPartMaster(String login, String pWorkspaceId) throws Exception{
        loginP.login(login, password.toCharArray());
        int totalParts =  productManagerLocal.getPartMasters(pWorkspaceId, 0, 100).size();
        loginP.logout();
        return totalParts;
    }
    public PartRevision checkoutPart(String login,PartRevisionKey revisionKey) throws PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        PartRevision partRevision = productManagerLocal.checkOutPart(revisionKey);
        loginP.logout();
        return partRevision;
    }

    public PartRevision updatePartIteration(String login,PartIterationKey pKey, java.lang.String pIterationNote, PartIteration.Source source, java.util.List<PartUsageLink> pUsageLinks, java.util.List<InstanceAttribute> pAttributes, DocumentIterationKey[] pLinkKeys) throws NotAllowedException, PartRevisionNotFoundException, WorkspaceNotFoundException, UserNotFoundException, AccessRightException, PartMasterNotFoundException {
        loginP.login(login, password.toCharArray());
        PartRevision partRevision = productManagerLocal.updatePartIteration(pKey,pIterationNote,source,pUsageLinks,pAttributes,pLinkKeys);
        loginP.logout();
        return partRevision;
    }

    public void checkOutPart(String login,PartMaster partMaster) throws PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException {
        loginP.login(login, password.toCharArray());
        productManagerLocal.checkOutPart(partMaster.getLastRevision().getKey());
        loginP.logout();
    }

    public void checkInPart(String login,PartMaster partMaster) throws PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, AccessRightException, FileAlreadyExistsException, ESServerException {
        loginP.login(login, password.toCharArray());
        productManagerLocal.checkInPart(partMaster.getLastRevision().getKey());
        loginP.logout();
    }
}