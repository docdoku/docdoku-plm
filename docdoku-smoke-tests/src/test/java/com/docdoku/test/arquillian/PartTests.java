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

package com.docdoku.test.arquillian;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.Organization;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
import com.docdoku.server.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESMapper;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.esindexer.ESTools;
import com.docdoku.server.gcm.GCMSenderBean;
import com.docdoku.test.arquillian.services.TestDocumentManagerBean;
import com.docdoku.test.arquillian.services.TestPartManagerBean;
import com.docdoku.test.arquillian.services.TestUserManagerBean;
import com.docdoku.test.arquillian.util.TestUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Asmae CHADID
 */

@RunWith(Arquillian.class)
public class PartTests {

    @EJB
    private ESIndexer esIndexer;

    @EJB
    private TestDocumentManagerBean documentManagerBean;
    @EJB
    private TestUserManagerBean userManagerBean;

    @EJB
    private TestPartManagerBean partManagerBean;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    private static final int COUNT = 5;


    private TestUtil util =new TestUtil();

    @Deployment
    public static Archive<?> createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests-parts.war")
                .addPackage(DocumentManagerBean.class.getPackage())
                .addClasses(
                        Account.class,
                        ACLUserEntry.class,
                        ACLUserGroupEntry.class,
                        Organization.class,
                        Credential.class,
                        DataManagerBean.class,
                        DocumentManagerBean.class,
                        DocumentIterationKey.class,
                        ESIndexer.class,
                        ESMapper.class,
                        ESSearcher.class,
                        ESTools.class,
                        GCMAccount.class,
                        GCMSenderBean.class,
                        IDataManagerLocal.class,
                        IDocumentManagerLocal.class,
                        IProductManagerLocal.class,
                        IGCMSenderLocal.class,
                        IMailerLocal.class,
                        InstanceAttributeTemplate.class,
                        IUserManagerLocal.class,
                        IWorkspaceManagerLocal.class,
                        JsonValue.class,
                        MailerBean.class,
                        PartMaster.class,
                        PartMasterTemplate.class,
                        PartRevision.class,
                        PartRevisionKey.class,
                        ProductManagerBean.class,
                        PartUsageLink.class,
                        TestDocumentManagerBean.class,
                        TestUserManagerBean.class,
                        TestPartManagerBean.class,
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
        em.clear();
        for (int i = 1; i <= COUNT; i++) {
            Account account = new Account("user" + i, "user" + i, "user" + i + "@docdoku.com", "FR", new Date(),null);
            em.merge(Credential.createCredential(account.getLogin(), "password"));
            em.merge(new UserGroupMapping(account.getLogin()));
            em.merge(account);
        }
        utx.commit();
       util.init(userManagerBean,documentManagerBean);
    }



    @Test
    public void test1_createPartMasterFromTemplate()  {
       Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : testCreationPartMasterFromTemplate");
      try{

          partManagerBean.createPartMasterTemplate(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "template1", "planes", "plane_###", new InstanceAttributeTemplate[0], true, true);
          partManagerBean.createPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125", " ", true, null, "", "template1",new HashMap<String, String>(), new ACLUserEntry[0], new ACLUserGroupEntry[0]);
          assertTrue(partManagerBean.findPartMasterById(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125") != null);
      }catch (Exception e){
          Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : testCreationPartMasterFromTemplate"+e);
      }
           }


   @Test
    public void test2_inheritedAttributesFromTemplate() throws Exception {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : testCreationPartMasterFromTemplate");
        InstanceAttributeTemplate[] attributeTemplates = new InstanceAttributeTemplate[5];
        attributeTemplates[0]= new InstanceAttributeTemplate("attr1", InstanceAttributeTemplate.AttributeType.DATE);
        attributeTemplates[1]= new InstanceAttributeTemplate("attr2", InstanceAttributeTemplate.AttributeType.BOOLEAN);
        attributeTemplates[2]= new InstanceAttributeTemplate("attr3", InstanceAttributeTemplate.AttributeType.NUMBER);
        attributeTemplates[3]= new InstanceAttributeTemplate("attr3", InstanceAttributeTemplate.AttributeType.TEXT);
        attributeTemplates[4]= new InstanceAttributeTemplate("attr3", InstanceAttributeTemplate.AttributeType.URL);
        partManagerBean.createPartMasterTemplate(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "template1", "planes", "plane_###",attributeTemplates, true, true);
        partManagerBean.createPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125", " ", true, null, "", "template1",new HashMap<String, String>(), new ACLUserEntry[0], new ACLUserGroupEntry[0]);
        assertTrue(partManagerBean.findPartMasterById(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125") != null);
    }

    @Test
    public void test3_checkObligatoryAttributes() throws Exception {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : testCreationPartMasterFromTemplate");
        InstanceAttributeTemplate[] attributeTemplates = new InstanceAttributeTemplate[5];
        attributeTemplates[0]= new InstanceAttributeTemplate("attr1", InstanceAttributeTemplate.AttributeType.DATE);
        attributeTemplates[1]= new InstanceAttributeTemplate("attr2", InstanceAttributeTemplate.AttributeType.BOOLEAN);
        attributeTemplates[2]= new InstanceAttributeTemplate("attr3", InstanceAttributeTemplate.AttributeType.NUMBER);
        attributeTemplates[3]= new InstanceAttributeTemplate("attr4", InstanceAttributeTemplate.AttributeType.TEXT);
        attributeTemplates[4]= new InstanceAttributeTemplate("attr5", InstanceAttributeTemplate.AttributeType.URL);
        for (InstanceAttributeTemplate attr:attributeTemplates){
            attr.setMandatory(true);
        }
        partManagerBean.createPartMasterTemplate(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "template1", "planes", "plane_###",attributeTemplates, true, true);
        PartMaster partMaster= partManagerBean.createPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125", " ", true, null, "", "template1",new HashMap<String, String>(), new ACLUserEntry[0], new ACLUserGroupEntry[0]);
        List<InstanceAttribute> attrs  =  new ArrayList<InstanceAttribute>();
        attrs.add(new InstanceDateAttribute("attr1", new Date(),true));
        attrs.add(new InstanceBooleanAttribute("attr2",true,true));
        attrs.add(new InstanceNumberAttribute("attr3", 123,true));
        attrs.add(new InstanceTextAttribute("attr4", "text",true));
        attrs.add(new InstanceURLAttribute("attr5", "http://localhost:8080",true));
        PartRevision partRevision =  partManagerBean.updatePartIteration(TestUtil.USER1_TEST, partMaster.getPartRevisions().get(0).getPartIterations().get(0).getKey(), "", PartIteration.Source.BUY, new ArrayList<PartUsageLink>(), attrs, new DocumentIterationKey[0]);
        List<PartRevision> revisions = new ArrayList<PartRevision>() ;
        revisions.add(partRevision);
        partMaster.setPartRevisions(revisions);
        Map<String, InstanceAttribute> instanceAttributes = partMaster.getPartRevisions().get(0).getIteration(1).getInstanceAttributes();

        for (InstanceAttribute instanceAttribute :instanceAttributes.values()){
            assertTrue(instanceAttribute.getValue() != null);
        }
    }



    @Test
    public void test4_maskValidityPartTemplate() throws Exception {

        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : testMaskValidityPartTemplate");
        partManagerBean.createPartMasterTemplate(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "template1", "planes", "ref_##_###", new InstanceAttributeTemplate[0], true, true);

        PartMaster partMaster= partManagerBean.createPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "rof_12_526", " ", true, null, "", "template1",new HashMap<String, String>(), new ACLUserEntry[0], new ACLUserGroupEntry[0]);
        partMaster.setAttributesLocked(true);
        partManagerBean.createPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "ref_1235", " ", true, null, "", "template1",new HashMap<String, String>(), new ACLUserEntry[0], new ACLUserGroupEntry[0]);
        partManagerBean.createPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "ref_125", " ", true, null, "", "template1",new HashMap<String, String>(), new ACLUserEntry[0], new ACLUserGroupEntry[0]);
        assertTrue(partManagerBean.findPartMasterById(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "ref_1235") == null);
        assertTrue(partManagerBean.findPartMasterById(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "ref_125") == null);
        assertEquals(partManagerBean.findPartMasterById(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "ref_12_526"), 1);
        assertEquals(partManagerBean.findAllPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST), 1);
    }

    @Test
    public void test5_duplicatePartName() throws Exception {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : duplicatePartName");
        partManagerBean.createPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125", " ", true, null, "", null, new HashMap<String, String>(), new ACLUserEntry[0], new ACLUserGroupEntry[0]);
        try {
            partManagerBean.createPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125", " ", true, null, "", null,new HashMap<String, String>(), new ACLUserEntry[0], new ACLUserGroupEntry[0]);
        }catch (Exception ignored){}
       finally {
            assertTrue(partManagerBean.findAllPartMaster(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST) == 1);
        }
    }


}