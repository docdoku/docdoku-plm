package com.docdoku.server.products;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.BaselineCreation;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;

import com.docdoku.server.DataManagerBean;
import com.docdoku.server.UserManagerBean;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.PartIterationDAO;
import com.docdoku.server.products.ProductBaselineManagerBean;
import com.docdoku.server.util.BaselineRule;
import com.docdoku.server.util.FastTestCategory;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;


import javax.ejb.SessionContext;
import javax.persistence.EntityManager;

import java.security.Principal;
import java.util.Locale;



import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class ProductBaselineManagerBeanTest {


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
    @Rule
    public BaselineRule baselineRuleACL;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


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
    public void createBaselineUsingPartNotReleasedYet() throws Exception{

        //Given

        baselineRuleNotReleased = new BaselineRule("myBaseline", ProductBaseline.BaselineType.RELEASED, "description", "workspace01", "user1", "part01", "product01", false, null);

        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleNotReleased.getUser1());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleNotReleased.getConfigurationItemKey())).thenReturn(baselineRuleNotReleased.getConfigurationItem());
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleNotReleased.getConfigurationItemKey())).thenReturn(baselineRuleNotReleased.getConfigurationItem());
        thrown.expect(ConfigurationItemNotReleasedException.class);
        //When
        productBaselineService.createBaseline(baselineRuleNotReleased.getConfigurationItemKey(), baselineRuleNotReleased.getName(), baselineRuleNotReleased.getType(), baselineRuleNotReleased.getDescription());


    }

    /**
     * test the creation of Released baseline
     */
    @Category(FastTestCategory.class)
    @Test
    public void createReleasedBaseline() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, UserNotActiveException, PartIterationNotFoundException, ConfigurationItemNotReleasedException {

        //Given
        baselineRuleReleased = new BaselineRule("myBaseline", ProductBaseline.BaselineType.RELEASED, "description", "workspace01", "user1", "part01", "product01", true, null);
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
     *
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
        baselineRuleReleased = new BaselineRule("myBaseline", null, "description", "workspace01", "user1", "part01", "product01", true, null);
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

    /**
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
    public void createLatestBaselineWithCheckedPart() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, UserNotActiveException, PartIterationNotFoundException, ConfigurationItemNotReleasedException {

        //Given
        baselineRuleReleased = new BaselineRule("myBaseline", null, "description", "workspace01", "user1", "part01", "product01", true, false);
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

    @Category(FastTestCategory.class)
    @Test
    public void throwExceptionWhenNoPermissionForUserOnPart() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, UserNotActiveException, PartIterationNotFoundException, ConfigurationItemNotReleasedException {

        //Given
        baselineRuleACL = new BaselineRule("myBaseline", null, "description", "workspace01", "user1", "part01", "product01", true, ACL.Permission.FORBIDDEN);
        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleACL.getUser1());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleACL.getConfigurationItemKey())).thenReturn(baselineRuleACL.getConfigurationItem()
        );
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleACL.getConfigurationItemKey())).thenReturn(baselineRuleACL.getConfigurationItem());
        Mockito.when(em.find(PartIteration.class, baselineRuleACL.getPartMaster().getLastReleasedRevision().getIteration(1).getKey())).thenReturn(baselineRuleACL.getPartMaster().getLastReleasedRevision().getIteration(1));
        Mockito.when(new PartIterationDAO(new Locale("en"), em).loadPartI(baselineRuleACL.getPartMaster().getLastReleasedRevision().getIteration(1).getKey())).thenReturn(baselineRuleACL.getPartMaster().getLastReleasedRevision().getIteration(1));

        thrown.expect(NotAllowedException.class);

        //When
        BaselineCreation baselineCreation = productBaselineService.createBaseline(baselineRuleACL.getConfigurationItemKey(), baselineRuleACL.getName(), baselineRuleACL.getType(), baselineRuleACL.getDescription());
    }

}