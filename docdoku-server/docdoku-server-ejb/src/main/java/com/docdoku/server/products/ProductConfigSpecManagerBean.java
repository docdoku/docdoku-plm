package com.docdoku.server.products;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductConfigSpecManagerLocal;
import com.docdoku.core.services.IProductConfigSpecManagerWS;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.PartUsageLinkDAO;
import com.docdoku.server.dao.ProductBaselineDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IProductConfigSpecManagerLocal.class)
@Stateless(name = "ProductConfigSpecManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IProductConfigSpecManagerWS")
public class ProductConfigSpecManagerBean implements IProductConfigSpecManagerWS, IProductConfigSpecManagerLocal {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IProductManagerLocal productManager;

    private static final Logger LOGGER = Logger.getLogger(ProductConfigSpecManagerBean.class.getName());

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ConfigSpec getLatestConfigSpec(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException{
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return new LatestConfigSpec(user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ConfigSpec getLatestReleasedConfigSpec(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException{
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return new LatestReleasedConfigSpec(user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ConfigSpec getConfigSpecForBaseline(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(productBaseline.getConfigurationItem().getWorkspaceId());
        return new BaselineConfigSpec(productBaseline, user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartUsageLink filterProductStructure(ConfigurationItemKey pKey, ConfigSpec configSpec, Integer partUsageLink, Integer pDepth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        int depth = (pDepth == null) ? -1 : pDepth;
        PartUsageLink rootUsageLink;

        if (partUsageLink == null || partUsageLink == -1) {                                                             // If no partUsageLink specified, we get the rootPartUsageLink
            rootUsageLink = getRootPartUsageLink(pKey);
        } else {
            rootUsageLink = new PartUsageLinkDAO(new Locale(user.getLanguage()), em).loadPartUsageLink(partUsageLink);
        }

        PartMaster component = rootUsageLink.getComponent();

        if(component.getWorkspaceId().equals(pKey.getWorkspace())){
            if(configSpec != null){
                filterPartConfigSpec(configSpec, component, depth, user);
            }else{
                filterPartConfigSpec(new LatestConfigSpec(user), component, depth, user);
            }

            return rootUsageLink;
        }else{
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartUsageLink filterProductStructure(PartUsageLink rootUsageLink, ConfigSpec configSpec, Integer pDepth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(rootUsageLink.getComponent().getWorkspaceId());
        int depth = (pDepth == null) ? -1 : pDepth;
        PartMaster component = rootUsageLink.getComponent();

        if(configSpec != null){
            filterPartConfigSpec(configSpec, component, depth, user);
        }else{
            filterPartConfigSpec(new LatestConfigSpec(user), component, depth, user);
        }

        return rootUsageLink;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public PartUsageLink getRootPartUsageLink(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
        ConfigurationItem ci = new ConfigurationItemDAO(new Locale(user.getLanguage()), em).loadConfigurationItem(pKey);
        PartUsageLink rootUsageLink;
        rootUsageLink = new PartUsageLink();
        rootUsageLink.setId(-1);
        rootUsageLink.setAmount(1d);
        List<CADInstance> cads = new ArrayList<>();
        CADInstance cad = new CADInstance(0d, 0d, 0d, 0d, 0d, 0d);
        cad.setId(-1);
        cads.add(cad);
        rootUsageLink.setCadInstances(cads);
        rootUsageLink.setComponent(ci.getDesignItem());
        return rootUsageLink;
    }

    private void filterPartsUsageLinksConfigSpec(ConfigSpec configSpec, List<PartUsageLink> partUsageLinks, int pDepth, User user){
        for (PartUsageLink usageLink : partUsageLinks) {
            filterPartConfigSpec(configSpec, usageLink.getComponent(), pDepth, user);

            for (PartSubstituteLink subLink : usageLink.getSubstitutes()) {
                filterPartConfigSpec(configSpec, subLink.getSubstitute(), 0, user);
            }
        }
    }

    private void filterPartConfigSpec(ConfigSpec configSpec, PartMaster partMaster, int pDepth, User user){
        PartIteration partI = configSpec.filterConfigSpec(partMaster);
        int depth = pDepth;
        boolean canRead = false;

        if(partI!=null){
            try {
                // Check PartIteration access
                canRead = productManager.canUserAccess(user, partI.getKey());

                // Filter the childs and childs substitute
                if (canRead && depth != 0) {
                    depth--;
                    filterPartsUsageLinksConfigSpec(configSpec,partI.getComponents(),depth,user);
                }
            } catch (PartRevisionNotFoundException | PartIterationNotFoundException e) {
                LOGGER.log(Level.WARNING,null,e);
            }
        }

        // Check alternative part
        for (PartAlternateLink alternateLink : partMaster.getAlternates()) {
            filterPartConfigSpec(configSpec, alternateLink.getAlternate(), 0, user);
        }

        // Detach PartMaster before serve it
        if(partI!=null){
            detachPartIteration(partMaster,partI,canRead);
        }
    }

    private void detachPartIteration(PartMaster partMaster, PartIteration partI, boolean canRead){
        em.detach(partMaster);
        PartRevision partRevision = partI.getPartRevision();
        if (partMaster.getPartRevisions().size() > 1) {
            partMaster.getPartRevisions().retainAll(Collections.singleton(partRevision));
        }
        if (partRevision != null && partRevision.getNumberOfIterations() > 1) {
            partRevision.getPartIterations().retainAll(Collections.singleton(partI));
        }
        if(!canRead){
            partI.getComponents().clear();
        }
    }
}