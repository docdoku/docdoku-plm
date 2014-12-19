package com.docdoku.server.rest;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.BaselineCreation;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductBaselineManagerLocal;
import com.docdoku.server.DataManagerBean;
import com.docdoku.server.UserManagerBean;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.PartIterationDAO;
import com.docdoku.server.products.ProductBaselineManagerBean;
import com.docdoku.server.util.BaselineRule;
import com.docdoku.server.util.FastTestCategory;
import com.google.common.collect.Lists;
import com.googlecode.catchexception.CatchException;
import com.googlecode.catchexception.apis.CatchExceptionBdd.*;

import com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.runners.MockitoJUnitRunner;
import sun.org.mozilla.javascript.ast.ErrorCollector;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class BaselinesResourceTest {


    @InjectMocks
    ProductBaselineManagerBean productBaselineService = new ProductBaselineManagerBean();

    @Mock
    SessionContext ctx;
    @Mock
    Principal principal;
    @Mock
    UserManagerBean userManager;
    @Mock
    EntityManager em;
    @Mock
    DataManagerBean dataManager;





    @Rule
    public BaselineRule baselineRuleNotReleased;
    @Rule
    public BaselineRule baselineRuleReleased;


    @Before
    public void setup() throws Exception {
        initMocks(this);
        Mockito.when(ctx.getCallerPrincipal()).thenReturn(principal);
        Mockito.when(principal.getName()).thenReturn("user1");

    }

    /**
     * test the creation of baseline with a product that contains a part that has not been released yet
     *
     * @throws Exception ConfigurationItemNotReleasedException
     */
    @Category(FastTestCategory.class)
    @Test
    public void createBaselineUsingPartNotReleasedYet() {

        //Given

        try {
            baselineRuleNotReleased = new BaselineRule("myBaseline",ProductBaseline.BaselineType.RELEASED,"description","workspace01","user1","part01","product01",false);

            doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
            Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleNotReleased.getUser1());
            Mockito.when(em.find(ConfigurationItem.class, baselineRuleNotReleased.getConfigurationItemKey())).thenReturn(baselineRuleNotReleased.getConfigurationItem());
            Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleNotReleased.getConfigurationItemKey())).thenReturn(baselineRuleNotReleased.getConfigurationItem());

            //When
            CatchException.catchException(productBaselineService).createBaseline(baselineRuleNotReleased.getConfigurationItemKey(),baselineRuleNotReleased.getName(),baselineRuleNotReleased.getType(), baselineRuleNotReleased.getDescription());


        } catch (Exception e) {
            //Then
            Assert.assertTrue(e instanceof ConfigurationItemNotReleasedException);
        }
    }

    /**
     * test the creation of Released baseline
     */
    @Category(FastTestCategory.class)
    @Test
    public void createReleasedBaseline() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, UserNotActiveException, PartIterationNotFoundException, ConfigurationItemNotReleasedException {

        //Given
        baselineRuleReleased = new BaselineRule("myBaseline",ProductBaseline.BaselineType.RELEASED,"description","workspace01","user1","part01","product01",true);
        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleReleased.getUser1());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem()
                );
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem());


        //When
        BaselineCreation baselineCreation = productBaselineService.createBaseline(baselineRuleReleased.getConfigurationItemKey(), baselineRuleReleased.getName(), baselineRuleReleased.getType(), baselineRuleReleased.getDescription());

        //Then
        Assert.assertTrue(baselineCreation != null);
        Assert.assertTrue(baselineCreation.getProductBaseline().getDescription().equals(baselineRuleReleased.getDescription()));
        Assert.assertTrue(baselineCreation.getProductBaseline().getType().equals(baselineRuleReleased.getType()));
        Assert.assertTrue(baselineCreation.getProductBaseline().getConfigurationItem().getWorkspaceId().equals(baselineRuleReleased.getWorkspace().getId()));

    }


    /**
     * Create  baseline with the latest version of the products
     * @throws UserNotFoundException
     * @throws AccessRightException
     * @throws WorkspaceNotFoundException
     * @throws ConfigurationItemNotFoundException
     * @throws NotAllowedException
     * @throws UserNotActiveException
     * @throws PartIterationNotFoundException
     * @throws ConfigurationItemNotReleasedException
     */
    @Category(FastTestCategory.class)
    @Test
    public void createBaselineWithoutSpecifiyingType() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, UserNotActiveException, PartIterationNotFoundException, ConfigurationItemNotReleasedException {

        //Given
        baselineRuleReleased = new BaselineRule("myBaseline",null,"description","workspace01","user1","part01","product01",true);
        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleReleased.getUser1());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem()
        );
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem());
        Mockito.when(em.find(PartIteration.class, baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1).getKey())).thenReturn(baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1));
        Mockito.when(new PartIterationDAO(new Locale("en"), em).loadPartI(baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1).getKey())).thenReturn(baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1));

        //When
        BaselineCreation baselineCreation = productBaselineService.createBaseline(baselineRuleReleased.getConfigurationItemKey(), baselineRuleReleased.getName(), baselineRuleReleased.getType(), baselineRuleReleased.getDescription());

        //Then
        Assert.assertTrue(baselineCreation != null);
        Assert.assertTrue(baselineCreation.getProductBaseline().getDescription().equals(baselineRuleReleased.getDescription()));
        Assert.assertTrue(baselineCreation.getProductBaseline().getType().equals(ProductBaseline.BaselineType.LATEST));
        Assert.assertTrue(baselineCreation.getProductBaseline().getConfigurationItem().getWorkspaceId().equals(baselineRuleReleased.getWorkspace().getId()));
    }


}