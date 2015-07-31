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
import com.docdoku.core.product.PathToPathLink;
import com.docdoku.server.DataManagerBean;
import com.docdoku.server.ProductManagerBean;
import com.docdoku.server.UserManagerBean;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.PartIterationDAO;
import com.docdoku.server.util.BaselineRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
    @Mock
    ProductManagerBean productService;

    @Rule
    public BaselineRule baselineRuleNotReleased;
    @Rule
    public BaselineRule baselineRuleReleased;
    @Rule
    public BaselineRule baselineRuleLatest;
    @Mock
    private TypedQuery<PathToPathLink> mockedQuery;

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
    @Test
    public void createReleasedBaseline() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, UserNotActiveException, PartIterationNotFoundException, PartRevisionNotReleasedException, EntityConstraintException, PartMasterNotFoundException, CreationException, BaselineNotFoundException, PathToPathLinkAlreadyExistsException {

        //Given
        baselineRuleReleased = new BaselineRule("myBaseline", ProductBaseline.BaselineType.RELEASED, "description", "workspace01", "user1", "part01", "product01", true);
        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleReleased.getUser());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem());
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem());

        Mockito.when(productService.getRootPartUsageLink(Matchers.any())).thenReturn(baselineRuleReleased.getRootPartUsageLink());
        Mockito.when(mockedQuery.setParameter(Matchers.anyString(), Matchers.any())).thenReturn(mockedQuery);
        Mockito.when(em.createNamedQuery("PathToPathLink.findPathToPathLinkByPathListInProduct", PathToPathLink.class)).thenReturn(mockedQuery);

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
    @Test
    public void createReleasedBaselineUsingPartNotReleased() throws Exception{

        //Given
        baselineRuleNotReleased = new BaselineRule("myBaseline", ProductBaseline.BaselineType.RELEASED, "description", "workspace01", "user1", "part01", "product01", false);

        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleNotReleased.getUser());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleNotReleased.getConfigurationItemKey())).thenReturn(baselineRuleNotReleased.getConfigurationItem());
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleNotReleased.getConfigurationItemKey())).thenReturn(baselineRuleNotReleased.getConfigurationItem());
        thrown.expect(NotAllowedException.class);
        //When
        productBaselineService.createBaseline(baselineRuleNotReleased.getConfigurationItemKey(), baselineRuleNotReleased.getName(), baselineRuleNotReleased.getType(), baselineRuleNotReleased.getDescription(),new ArrayList<>(), baselineRuleNotReleased.getSubstituteLinks(), baselineRuleNotReleased.getOptionalUsageLinks());

    }
    /**
     * test the creation of latest baseline
     */
    @Test
    public void createLatestBaseline() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, EntityConstraintException, UserNotActiveException, NotAllowedException, PartIterationNotFoundException, PartRevisionNotReleasedException, PartMasterNotFoundException, CreationException, BaselineNotFoundException, PathToPathLinkAlreadyExistsException {

        //Given
        baselineRuleLatest = new BaselineRule("myBaseline", ProductBaseline.BaselineType.LATEST, "description", "workspace01", "user1", "part01", "product01", true);
        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleLatest.getUser());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleLatest.getConfigurationItemKey())).thenReturn(baselineRuleLatest.getConfigurationItem());
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleLatest.getConfigurationItemKey())).thenReturn(baselineRuleLatest.getConfigurationItem());

        Mockito.when(productService.getRootPartUsageLink(Matchers.any())).thenReturn(baselineRuleLatest.getRootPartUsageLink());
        Mockito.when(mockedQuery.setParameter(Matchers.anyString(), Matchers.any())).thenReturn(mockedQuery);
        Mockito.when(em.createNamedQuery("PathToPathLink.findPathToPathLinkByPathListInProduct", PathToPathLink.class)).thenReturn(mockedQuery);

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
    @Test
    public void createLatestBaselineWithCheckedPart() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, ConfigurationItemNotFoundException, NotAllowedException, UserNotActiveException, PartIterationNotFoundException, PartRevisionNotReleasedException, EntityConstraintException, PartMasterNotFoundException, CreationException, BaselineNotFoundException, PathToPathLinkAlreadyExistsException {

        //Given
        baselineRuleReleased = new BaselineRule("myBaseline", ProductBaseline.BaselineType.LATEST , "description", "workspace01", "user1", "part01", "product01", true, false);
        doReturn(new User("en")).when(userManager).checkWorkspaceWriteAccess(Matchers.anyString());
        Mockito.when(userManager.checkWorkspaceWriteAccess(Matchers.anyString())).thenReturn(baselineRuleReleased.getUser());
        Mockito.when(em.find(ConfigurationItem.class, baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem()
        );
        Mockito.when(new ConfigurationItemDAO(new Locale("en"), em).loadConfigurationItem(baselineRuleReleased.getConfigurationItemKey())).thenReturn(baselineRuleReleased.getConfigurationItem());
        Mockito.when(em.find(PartIteration.class, baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1).getKey())).thenReturn(baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1));
        Mockito.when(new PartIterationDAO(new Locale("en"), em).loadPartI(baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1).getKey())).thenReturn(baselineRuleReleased.getPartMaster().getLastReleasedRevision().getIteration(1));
        Mockito.when(productService.getRootPartUsageLink(Matchers.any())).thenReturn(baselineRuleReleased.getRootPartUsageLink());
        Mockito.when(mockedQuery.setParameter(Matchers.anyString(), Matchers.any())).thenReturn(mockedQuery);
        Mockito.when(em.createNamedQuery("PathToPathLink.findPathToPathLinkByPathListInProduct", PathToPathLink.class)).thenReturn(mockedQuery);

        //When
        ProductBaseline baseline = productBaselineService.createBaseline(baselineRuleReleased.getConfigurationItemKey(), baselineRuleReleased.getName(), baselineRuleReleased.getType(), baselineRuleReleased.getDescription(), new ArrayList<>(), baselineRuleReleased.getSubstituteLinks(), baselineRuleReleased.getOptionalUsageLinks());

        //Then
        Assert.assertTrue(baseline != null);
        Assert.assertTrue(baseline.getDescription().equals(baselineRuleReleased.getDescription()));
        Assert.assertTrue(baseline.getType().equals(ProductBaseline.BaselineType.LATEST));
        Assert.assertTrue(baseline.getConfigurationItem().getWorkspaceId().equals(baselineRuleReleased.getWorkspace().getId()));

    }

}