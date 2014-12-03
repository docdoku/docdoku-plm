/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

package com.docdoku.arquillian.tests;

import com.docdoku.arquillian.tests.services.TestDocumentManagerBean;
import com.docdoku.arquillian.tests.services.TestPartManagerBean;
import com.docdoku.arquillian.tests.services.TestProductManagerBean;
import com.docdoku.arquillian.tests.services.TestUserManagerBean;
import com.docdoku.arquillian.tests.util.TestUtil;
import com.docdoku.core.common.*;
import com.docdoku.core.configuration.BaselineCreation;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.ConfigurationItem;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
import com.docdoku.server.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESMapper;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.esindexer.ESTools;
import com.docdoku.server.gcm.GCMSenderBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Asmae CHADID
 */

@RunWith(Arquillian.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class ProductTest {

    @EJB
    private ESIndexer esIndexer;

    @EJB
    private TestDocumentManagerBean documentManagerBean;
    @EJB
    private TestUserManagerBean userManagerBean;

    @EJB
    private TestPartManagerBean partManagerBean;

    @EJB
    private TestProductManagerBean productManagerBean;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    private static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(PartTests.class);
    private static final int COUNT = 5;

    @Deployment
    public static Archive<?> createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests.war")
                .addPackage(Workspace.class.getPackage())
                .addClasses(
                        Account.class,
                        Organization.class,
                        Credential.class,
                        DataManagerBean.class,
                        DocumentManagerBean.class,
                        ESIndexer.class,
                        ESMapper.class,
                        ESSearcher.class,
                        ESTools.class,
                        GCMAccount.class,
                        GCMSenderBean.class,
                        IDataManagerLocal.class,
                        IDocumentManagerLocal.class,
                        IProductManagerLocal.class,
                        IProductBaselineManagerLocal.class,
                        IGCMSenderLocal.class,
                        IMailerLocal.class,
                        IUserManagerLocal.class,
                        IWorkspaceManagerLocal.class,
                        JsonValue.class,
                        MailerBean.class,
                        TestDocumentManagerBean.class,
                        TestProductManagerBean.class,
                        TestPartManagerBean.class,
                        TestUserManagerBean.class,
                        UserManagerBean.class,
                        Workspace.class,
                        WorkspaceManagerBean.class,
                        WorkspaceUserMembership.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("WEB-INF/sun-web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");


    }

    @Before
    public void insertData() throws Exception {
        utx.begin();
        em.joinTransaction();
        for (int i = 1; i <= COUNT; i++) {
            Account account = new Account("user" + i, "user" + i, "user" + i + "@docdoku.com", "FR", new Date());
            em.merge(Credential.createCredential(account.getLogin(), "password"));
            em.merge(new UserGroupMapping(account.getLogin()));
            em.merge(account);
        }
        utx.commit();
        userManagerBean.testWorkspaceCreation(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST);
    }


    @Test
    public void test1_ProductWithNullPart() {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : ProductWithNullPart");

        try {

            productManagerBean.createConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST, "produit1", "", null);
            assertTrue(productManagerBean.getListConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST).size() == 0);
        } catch (Exception e) {
            Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : ProductWithNullPart stack trace" + e);
        }
    }

    @Test
    public void test2_productWithoutInvalidePart() {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : ProductWithoutInvalidePart");

        try {
            productManagerBean.createConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST, "produit1", "", "part1");
            assertTrue(productManagerBean.getListConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST).size() == 0);
        } catch (Exception e) {
            Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : ProductWithoutInvalidePart stack trace" + e);
        }
    }

    @Test
    public void test3_productCreation() {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : productCreation");
        try {

            partManagerBean.createPartMaster(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST, "part1", " ", true, null, "", null, null, null, null);
            productManagerBean.createConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST, "produit1", "", "part1");
            assertTrue(productManagerBean.getListConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST).size() == 1);
        } catch (Exception e) {
            Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : productCreation stack trace" + e);
        }
    }

    @Test
    public void test4_baselineProductNotCheckouted() {
        BaselineCreation baselineCreation = null;
        try {
            partManagerBean.createPartMaster(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST, "part1", " ", true, null, "", null, null, null, null);
            productManagerBean.createConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST, "produit1", "", "part1");
            productManagerBean.getListConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST).get(0);
            ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(TestUtil.WORKSPACE_TEST, "produit1");
            baselineCreation = productManagerBean.baselineProduct(TestUtil.USER_TEST, configurationItemKey, "myBaseline", ProductBaseline.BaselineType.RELEASED, "");
        } catch (Exception e) {
            Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : baselineProduct stack trace" + e);
            try {
                productManagerBean.getBaseline(TestUtil.USER_TEST, baselineCreation.getProductBaseline().getId());
            } catch (UserNotFoundException | WorkspaceNotFoundException | UserNotActiveException | BaselineNotFoundException e1) {
                Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : baselineProduct stack trace" + e);
            }
        }
    }

    @Test
    public void test5_baselineProductPartNoAccess() {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : baselineProductPartNoAccess");
        BaselineCreation baselineCreation = null;
        try {

            ACLUserEntry aclUserEntry = new ACLUserEntry();
            aclUserEntry.setPermission(ACL.Permission.FORBIDDEN);
            ACLUserEntry[] aclUserEntries = new ACLUserEntry[1];
            aclUserEntries[1] = aclUserEntry;
            PartMaster partMaster = partManagerBean.createPartMaster(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST, "part1", " ", true, null, "", null, null, aclUserEntries, null);
            PartRevisionKey partRevisionKey = new PartRevisionKey(TestUtil.WORKSPACE_TEST, partMaster.getNumber(), "1");
            partManagerBean.checkoutPart(TestUtil.USER_TEST, partRevisionKey);
            productManagerBean.createConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST, "produit1", "", "part1");
            productManagerBean.getListConfigurationItem(TestUtil.USER_TEST, TestUtil.WORKSPACE_TEST).get(0);
            ConfigurationItemKey configurationItemKey = new ConfigurationItemKey(TestUtil.WORKSPACE_TEST, "produit1");
            baselineCreation = productManagerBean.baselineProduct(TestUtil.USER_TEST, configurationItemKey, "myBaseline", ProductBaseline.BaselineType.RELEASED, "");
        } catch (Exception e) {
            Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : baselineProductPartNoAccess stack trace" + e);
            try {
                productManagerBean.getBaseline(TestUtil.USER_TEST, baselineCreation.getProductBaseline().getId());
            } catch (UserNotFoundException | WorkspaceNotFoundException | UserNotActiveException | BaselineNotFoundException e1) {
                Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : baselineProductPartNoAccess stack trace" + e);
            }
        }
    }


}
