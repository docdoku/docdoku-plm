package com.docdoku.arquillian.tests.services;

import com.docdoku.core.configuration.BaselineCreation;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.esindexer.ESIndexer;
import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;


import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Asmae CHADID
 */
@LocalBean
@Stateless
public class TestProductManagerBean {
    @EJB
    private IProductManagerLocal productManagerLocal;

    @EJB
    private IProductBaselineManagerLocal productBaselineManagerLocal;

    @EJB
    private ESIndexer esIndexer;

    private ProgrammaticLogin loginP = new ProgrammaticLogin();
    private String password = "password";

    public ConfigurationItem createConfigurationItem(String login,String pWorkspaceId, String pId, String pDescription, String pDesignItemNumber) throws NotAllowedException, WorkspaceNotFoundException, CreationException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException, ConfigurationItemAlreadyExistsException, PartMasterNotFoundException {
        loginP.login(login, password.toCharArray());
        ConfigurationItem configurationItem = productManagerLocal.createConfigurationItem(pWorkspaceId, pId, pDescription,pDesignItemNumber);
        loginP.logout();
        return  configurationItem;
    }

    public List<ConfigurationItem> getListConfigurationItem(String login,String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        loginP.login(login, password.toCharArray());
        List<ConfigurationItem> configurationItems = productManagerLocal.getConfigurationItems(pWorkspaceId);
        loginP.logout();
        return  configurationItems;

    }

    public BaselineCreation baselineProduct(String login,ConfigurationItemKey configurationItemKey, String name, ProductBaseline.BaselineType type, String description) throws UserNotActiveException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, UserNotFoundException, PartIterationNotFoundException, NotAllowedException, AccessRightException, ConfigurationItemNotReleasedException {

        loginP.login(login, password.toCharArray());
        BaselineCreation baselineCreation = productBaselineManagerLocal.createBaseline(configurationItemKey,name,type,description);
        loginP.logout();
        return  baselineCreation;
    }

    public ProductBaseline getBaseline(String login,int baselineId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, BaselineNotFoundException {
        loginP.login(login, password.toCharArray());
        ProductBaseline baselineCreation = productBaselineManagerLocal.getBaseline(baselineId);
        loginP.logout();
        return  baselineCreation;
    }






}
