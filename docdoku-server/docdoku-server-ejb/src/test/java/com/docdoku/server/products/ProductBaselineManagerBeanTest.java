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

package com.docdoku.server.products;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.server.DataManagerBean;
import com.docdoku.server.UserManagerBean;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.PartIterationDAO;
import com.docdoku.server.util.BaselineRule;
import com.docdoku.server.util.CyclicAssemblyRule;
import com.docdoku.server.util.FastTestCategory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import java.security.Principal;
import java.util.ArrayList;
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
    public BaselineRule baselineRuleLatest;
    @Rule
    public CyclicAssemblyRule cyclicAssemblyRule;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        initMocks(this);
        Mockito.when(ctx.getCallerPrincipal()).thenReturn(principal);
        Mockito.when(principal.getName()).thenReturn("user1");

    }


    /**
     * test the creation of Released baseline
     */
    @Category(FastTestCategory.class)
    @Test
    public void createReleasedBaseline() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, UserNotActiveException, PartIterationNotFoundException, PartRevisionNotReleasedException, EntityConstraintException, PartMasterNotFoundException, CreationException {

        //Given
        baselineRuleReleased = new BaselineRule("myBaseline", ProductBaseline.BaselineType.RELEASED, "description", "workspace01", "user1", "part01", "product01", true);
        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleReleased.getUser());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem()
        );
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem());


        //When
        ProductBaseline baseline = productBaselineService.createBaseline(baselineRuleReleased.getConfigurationItemKey(), baselineRuleReleased.getName(), baselineRuleReleased.getType(), baselineRuleReleased.getDescription(), new ArrayList<>(), baselineRuleReleased.getSubstituteLinks(), baselineRuleReleased.getOptionalUsageLinks());

        //Then
        Assert.assertTrue(baseline != null);
        Assert.assertTrue(baseline.getDescription().equals(baselineRuleReleased.getDescription()));
        Assert.assertTrue(baseline.getType().equals(baselineRuleReleased.getType()));
        Assert.assertTrue(baseline.getConfigurationItem().getWorkspaceId().equals(baselineRuleReleased.getWorkspace().getId()));

    }

    /**
     * test the creation of a released baseline with a product that contains a part that has not been released yet
     *
     * @throws Exception PartRevisionNotReleasedException
     */
    @Category(FastTestCategory.class)
    @Test
    public void createReleasedBaselineUsingPartNotReleased() throws Exception{

        //Given
        baselineRuleNotReleased = new BaselineRule("myBaseline", ProductBaseline.BaselineType.RELEASED, "description", "workspace01", "user1", "part01", "product01", false);

        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleNotReleased.getUser());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleNotReleased.getConfigurationItemKey())).thenReturn(baselineRuleNotReleased.getConfigurationItem());
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleNotReleased.getConfigurationItemKey())).thenReturn(baselineRuleNotReleased.getConfigurationItem());
        thrown.expect(PartRevisionNotReleasedException.class);
        //When
        productBaselineService.createBaseline(baselineRuleNotReleased.getConfigurationItemKey(), baselineRuleNotReleased.getName(), baselineRuleNotReleased.getType(), baselineRuleNotReleased.getDescription(),new ArrayList<>(), baselineRuleNotReleased.getSubstituteLinks(), baselineRuleNotReleased.getOptionalUsageLinks());

    }
    /**
     * test the creation of latest baseline
     */
    @Category(FastTestCategory.class)
    @Test
    public void createLatestBaseline() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, EntityConstraintException, UserNotActiveException, NotAllowedException, PartIterationNotFoundException, PartRevisionNotReleasedException, PartMasterNotFoundException, CreationException {

        //Given
        baselineRuleLatest = new BaselineRule("myBaseline", ProductBaseline.BaselineType.LATEST, "description", "workspace01", "user1", "part01", "product01", true);
        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleLatest.getUser());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleLatest.getConfigurationItemKey())).thenReturn(baselineRuleLatest.getConfigurationItem()
        );
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleLatest.getConfigurationItemKey())).thenReturn(baselineRuleLatest.getConfigurationItem());

        //When
        ProductBaseline baseline = productBaselineService.createBaseline(baselineRuleLatest.getConfigurationItemKey(), baselineRuleLatest.getName(), baselineRuleLatest.getType(), baselineRuleLatest.getDescription(), new ArrayList<>(), baselineRuleLatest.getSubstituteLinks(), baselineRuleLatest.getOptionalUsageLinks());

        //Then
        Assert.assertTrue(baseline != null);
        Assert.assertTrue(baseline.getDescription().equals(baselineRuleLatest.getDescription()));
        Assert.assertTrue(baseline.getType().equals(baselineRuleLatest.getType()));
        Assert.assertTrue(baseline.getConfigurationItem().getWorkspaceId().equals(baselineRuleLatest.getWorkspace().getId()));

    }

    /**
     * @throws UserNotFoundException
     * @throws AccessRightException
     * @throws WorkspaceNotFoundException
     * @throws ConfigurationItemNotFoundException
     * @throws NotAllowedException
     * @throws UserNotActiveException
     * @throws PartIterationNotFoundException
     * @throws com.docdoku.core.exceptions.PartRevisionNotReleasedException
     */
    @Category(FastTestCategory.class)
    @Test
    public void createLatestBaselineWithCheckedPart() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, UserNotActiveException, PartIterationNotFoundException, PartRevisionNotReleasedException, EntityConstraintException, PartMasterNotFoundException, CreationException {

        //Given
        baselineRuleReleased = new BaselineRule("myBaseline", ProductBaseline.BaselineType.LATEST , "description", "workspace01", "user1", "part01", "product01", true, false);
        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleReleased.getUser());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem()
        );
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem());
        Mockito.when(em.find(PartIteration.class, baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1).getKey())).thenReturn(baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1));
        Mockito.when(new PartIterationDAO(new Locale("en"), em).loadPartI(baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1).getKey())).thenReturn(baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1));

        //When
        ProductBaseline baseline = productBaselineService.createBaseline(baselineRuleReleased.getConfigurationItemKey(), baselineRuleReleased.getName(), baselineRuleReleased.getType(), baselineRuleReleased.getDescription(), new ArrayList<>(), baselineRuleReleased.getSubstituteLinks(), baselineRuleReleased.getOptionalUsageLinks());

        //Then
        Assert.assertTrue(baseline != null);
        Assert.assertTrue(baseline.getDescription().equals(baselineRuleReleased.getDescription()));
        Assert.assertTrue(baseline.getType().equals(ProductBaseline.BaselineType.LATEST));
        Assert.assertTrue(baseline.getConfigurationItem().getWorkspaceId().equals(baselineRuleReleased.getWorkspace().getId()));

    }

    @Category(FastTestCategory.class)
    @Test
    public void checkCyclicDetection() throws EntityConstraintException, PartMasterNotFoundException {

        cyclicAssemblyRule = new CyclicAssemblyRule("user1");
        Mockito.when(em.find(PartMaster.class, cyclicAssemblyRule.getP1().getKey())).thenReturn(cyclicAssemblyRule.getP1());
        Mockito.when(em.find(PartMaster.class, cyclicAssemblyRule.getP2().getKey())).thenReturn(cyclicAssemblyRule.getP2());

        thrown.expect(EntityConstraintException.class);

//        productBaselineService.checkCyclicAssembly(
//                    cyclicAssemblyRule.getWorkspaceId(),
//                    cyclicAssemblyRule.getP1(),
//                    cyclicAssemblyRule.getP1().getLastRevision().getLastIteration().getComponents(),
//                    new LatestConfigSpec(cyclicAssemblyRule.getUser()), new Locale(cyclicAssemblyRule.getUser().getLanguage()));

    }



}