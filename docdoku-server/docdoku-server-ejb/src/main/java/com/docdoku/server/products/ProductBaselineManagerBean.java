package com.docdoku.server.products;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.BaselineCreation;
import com.docdoku.core.configuration.BaselinedPart;
import com.docdoku.core.configuration.BaselinedPartKey;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.PartIterationDAO;
import com.docdoku.server.dao.ProductBaselineDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.MessageFormat;
import java.util.*;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IProductBaselineManagerLocal.class)
@Stateless(name = "ProductBaselineManagerBean")
public class ProductBaselineManagerBean implements IProductBaselineManagerLocal {
    @PersistenceContext
    private EntityManager em;
    @EJB
    private IUserManagerLocal userManager;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public BaselineCreation createBaseline(ConfigurationItemKey configurationItemKey, String name, ProductBaseline.BaselineType pType, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, ConfigurationItemNotReleasedException, PartIterationNotFoundException, UserNotActiveException, NotAllowedException{
        User user = userManager.checkWorkspaceWriteAccess(configurationItemKey.getWorkspace());
        Locale locale = new Locale(user.getLanguage());
        ConfigurationItem configurationItem = new ConfigurationItemDAO(locale,em).loadConfigurationItem(configurationItemKey);

        ProductBaseline.BaselineType type = pType;
        if(type == null){
            type = ProductBaseline.BaselineType.LATEST;
        }
        ProductBaseline productBaseline = new ProductBaseline(configurationItem, name, type, description);
        Date now = new Date();
        productBaseline.getPartCollection().setCreationDate(now);
        productBaseline.getPartCollection().setAuthor(user);

        new ProductBaselineDAO(em).createBaseline(productBaseline);

        BaselineCreation baselineCreation = new BaselineCreation(productBaseline);

        PartRevision lastRevision;
        PartIteration baselinedIteration = null;

        switch(type){
            case RELEASED:
                lastRevision = configurationItem.getDesignItem().getLastReleasedRevision();
                if(lastRevision==null){
                    throw new ConfigurationItemNotReleasedException(locale, configurationItemKey.getId());
                }
                baselinedIteration = lastRevision.getLastIteration();
                break;
            // case LATEST:
            default:
                List<PartRevision> partRevisions = configurationItem.getDesignItem().getPartRevisions();
                boolean isPartFinded = false;

                lastRevision =configurationItem.getDesignItem().getLastRevision();
                if(lastRevision.isCheckedOut()){
                    baselineCreation.addConflit(lastRevision);
                }

                for(int j= partRevisions.size()-1; j>=0 && !isPartFinded;j--){
                    lastRevision = partRevisions.get(j);

                    for(int i= lastRevision.getLastIteration().getIteration(); i>0 && !isPartFinded; i--){
                        try{
                            checkPartIterationForBaseline(user,new PartIterationKey(lastRevision.getKey(),i));
                            baselinedIteration = lastRevision.getIteration(i);
                            isPartFinded=true;
                        }catch (AccessRightException e){
                            if(!baselineCreation.getConflit().contains(lastRevision)){
                                baselineCreation.addConflit(lastRevision);
                            }
                        }
                    }
                }
                if(baselinedIteration==null){
                    throw new NotAllowedException(locale, "NotAllowedException1");
                }
                break;
        }

        baselineCreation.addConflit(fillBaselineParts(productBaseline, baselinedIteration, type, user));

        if(!baselineCreation.getConflit().isEmpty()){
            String message = ResourceBundle.getBundle("com.docdoku.core.i18n.LocalStrings", locale).getString("BaselineWarningException1");
            baselineCreation.setMessage(MessageFormat.format(message, productBaseline.getName()));
        }


        return baselineCreation;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductBaseline> getAllBaselines(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        return new ProductBaselineDAO(em).findBaselines(workspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ProductBaseline> getBaselines(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        userManager.checkWorkspaceReadAccess(configurationItemKey.getWorkspace());
        return new ProductBaselineDAO(em).findBaselines(configurationItemKey.getId(), configurationItemKey.getWorkspace());
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteBaseline(int baselineId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, BaselineNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        userManager.checkWorkspaceWriteAccess(productBaseline.getConfigurationItem().getWorkspaceId());
        productBaselineDAO.deleteBaseline(productBaseline);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductBaseline getBaseline(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, BaselineNotFoundException {
        ProductBaseline productBaseline = new ProductBaselineDAO(em).loadBaseline(baselineId);
        userManager.checkWorkspaceReadAccess(productBaseline.getConfigurationItem().getWorkspaceId());
        return productBaseline;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductBaseline getBaselineById(int baselineId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.findBaselineById(baselineId);
        Workspace workspace = productBaseline.getConfigurationItem().getWorkspace();
        userManager.checkWorkspaceReadAccess(workspace.getId());
        return productBaseline;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ProductBaseline duplicateBaseline(int baselineId, String name, ProductBaseline.BaselineType pType, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, BaselineNotFoundException{
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        ConfigurationItem configurationItem = productBaseline.getConfigurationItem();
        User user = userManager.checkWorkspaceWriteAccess(configurationItem.getWorkspaceId());

        ProductBaseline.BaselineType type = pType;
        if(pType == null){
            type = ProductBaseline.BaselineType.LATEST;
        }
        ProductBaseline duplicatedProductBaseline = new ProductBaseline(configurationItem,name, type, description);
        productBaselineDAO.createBaseline(duplicatedProductBaseline);
        Date now = new Date();
        productBaseline.getPartCollection().setCreationDate(now);
        productBaseline.getPartCollection().setAuthor(user);

        // copy partIterations
        Set<Map.Entry<BaselinedPartKey, BaselinedPart>> entries = productBaseline.getBaselinedParts().entrySet();
        for(Map.Entry<BaselinedPartKey,BaselinedPart> entry : entries){
            duplicatedProductBaseline.addBaselinedPart(entry.getValue().getTargetPart());
        }

        return duplicatedProductBaseline;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void updateBaseline(ConfigurationItemKey configurationItemKey, int baselineId, String name, ProductBaseline.BaselineType type, String description, List<PartIterationKey> partIterationKeys) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, BaselineNotFoundException, ConfigurationItemNotReleasedException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(productBaseline.getConfigurationItem().getWorkspaceId());

        productBaseline.setDescription(description);
        productBaseline.setName(name);
        productBaseline.setType(type);
        productBaselineDAO.flushBaselinedParts(productBaseline);

        Locale locale = new Locale(user.getLanguage());
        PartIterationDAO partIterationDAO = new PartIterationDAO(locale,em);
        for(PartIterationKey partIterationKey : partIterationKeys){
            PartIteration partIteration = partIterationDAO.loadPartI(partIterationKey);
            if(type== ProductBaseline.BaselineType.LATEST || (type== ProductBaseline.BaselineType.RELEASED && partIteration.getPartRevision().isReleased())){
                productBaseline.addBaselinedPart(partIteration);
            }else{
                throw new ConfigurationItemNotReleasedException(locale,partIteration.getPartRevisionKey().toString());
            }
        }
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<BaselinedPart> getBaselinedPartWithReference(int baselineId, String q, int maxResults) throws BaselineNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        ProductBaselineDAO productBaselineDAO = new ProductBaselineDAO(em);
        ProductBaseline productBaseline = productBaselineDAO.loadBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(productBaseline.getConfigurationItem().getWorkspaceId());
        return new ProductBaselineDAO(new Locale(user.getLanguage()), em).findBaselinedPartWithReferenceLike(productBaseline.getPartCollection().getId(), q, maxResults);
    }

    private List<PartRevision> fillBaselineParts(ProductBaseline productBaseline, PartIteration lastIteration, ProductBaseline.BaselineType type,User user) throws ConfigurationItemNotReleasedException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartIterationNotFoundException, NotAllowedException{
        Locale locale = new Locale(user.getLanguage());
        // Ignore already existing parts
        List<PartRevision> ignoredRevisions = new ArrayList<>();
        if(productBaseline.hasBasedLinedPart(lastIteration.getWorkspaceId(), lastIteration.getPartNumber())) return ignoredRevisions;
        // Add current
        productBaseline.addBaselinedPart(lastIteration);
        // Add components
        for(PartUsageLink partUsageLink : lastIteration.getComponents()){
            PartRevision lastRevision;
            PartIteration baselinedIteration = null;
            switch(type){
                case RELEASED:
                    lastRevision = partUsageLink.getComponent().getLastReleasedRevision();
                    if(lastRevision==null){
                        throw new ConfigurationItemNotReleasedException(locale, partUsageLink.getComponent().getNumber());
                    }
                    baselinedIteration = lastRevision.getLastIteration();
                    break;
                case LATEST:
                default:
                    List<PartRevision> partRevisions = partUsageLink.getComponent().getPartRevisions();
                    boolean isPartFinded = false;

                    lastRevision =partUsageLink.getComponent().getLastRevision();
                    if(lastRevision.isCheckedOut()){
                        ignoredRevisions.add(lastRevision);
                    }

                    for(int j= partRevisions.size()-1; j>=0 && !isPartFinded;j--){
                        lastRevision = partRevisions.get(j);
                        for(int i= lastRevision.getLastIteration().getIteration(); i>0 && !isPartFinded; i--){
                            try{
                                checkPartIterationForBaseline(user,new PartIterationKey(lastRevision.getKey(), i));
                                baselinedIteration = lastRevision.getIteration(i);
                                isPartFinded=true;
                            }catch (AccessRightException e){
                                if(!ignoredRevisions.contains(lastRevision)){
                                    ignoredRevisions.add(lastRevision);
                                }
                            }
                        }
                    }
                    if(baselinedIteration==null){
                        throw new NotAllowedException(Locale.getDefault(), "NotAllowedException1");
                    }
                    break;
            }
            List<PartRevision> ignoredUsageLinkRevisions = fillBaselineParts(productBaseline, baselinedIteration, type, user);
            ignoredRevisions.addAll(ignoredUsageLinkRevisions);
        }
        return ignoredRevisions;
    }

    private User checkPartIterationForBaseline(User user,PartIterationKey partIterationKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, AccessRightException, NotAllowedException {

        Locale locale = new Locale(user.getLanguage());
        PartIteration partIteration = new PartIterationDAO(locale, em).loadPartI(partIterationKey);
        PartRevision partRevision = partIteration.getPartRevision();
        if ((partRevision.getACL() == null || partRevision.getACL().hasReadAccess(user)) &&
                (!partRevision.isCheckedOut() || !partRevision.getLastIteration().equals(partIteration))) {              // Check if the ACL grant write access
            return user;
        }
        throw new AccessRightException(locale, user);                                                                    // Else throw a AccessRightException

    }
}