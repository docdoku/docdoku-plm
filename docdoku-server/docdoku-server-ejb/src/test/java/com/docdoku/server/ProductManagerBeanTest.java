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

package com.docdoku.server;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;
import com.docdoku.core.meta.InstanceTextAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IUserManagerLocal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.MockitoAnnotations.initMocks;

public class ProductManagerBeanTest {

    private static final String WORKSPACE_ID="TestWorkspace";
    private static final String VERSION ="A" ;
    private static final int ITERATION = 1;
    private static final String PART_ID ="TestPart";
    private static final String PART_MASTER_TEMPLATE_ID="template";
    private static final String PART_TYPE="PartType";


    @InjectMocks
    ProductManagerBean productManagerBean = new ProductManagerBean();

    @Mock
    private EntityManager em;
    @Mock
    private IUserManagerLocal userManager;

    @Mock
    private Account account;
    @Mock
    private Workspace workspace ;
    @Mock
    private User user;

    @Mock
    PartMaster partMaster;
    @Mock
    PartMasterTemplate partMasterTemplate;
    @Mock
    PartIteration partIteration;
    @Mock
    PartRevision partRevision;


    @Before
    public void setup() throws Exception {
        initMocks(this);
        account = new Account("user2", "user2", "user2@docdoku.com", "en", new Date(), null);
        workspace = new Workspace(WORKSPACE_ID,account, "pDescription", false);
        user = new User(workspace, "user1" , "user1", "user1@docdoku.com", "en");

        partMaster = new PartMaster(workspace, PART_ID, user);
        partMasterTemplate = new PartMasterTemplate(workspace, PART_MASTER_TEMPLATE_ID, user, PART_TYPE, "", true);
        partRevision = new PartRevision(partMaster,VERSION,user);
        partIteration = new PartIteration(partRevision, ITERATION,user);
        ArrayList<PartIteration> iterations = new ArrayList<PartIteration>();
        iterations.add(partIteration);

        partRevision.setPartIterations(iterations);
        partRevision.setCheckOutUser(user);
        partRevision.setCheckOutDate(new Date());

        partIteration.setPartRevision(partRevision);

    }

    @Test
    public void updatePartWithLockedAttributes() throws Exception {
        //Creation of current attributes of the iteration
        InstanceAttribute attribute = new InstanceTextAttribute("Test", "Testeur", false);
        List<InstanceAttribute> attributesOfIteration = new ArrayList<>();
        attributesOfIteration.add(attribute);
        partIteration.setInstanceAttributes(attributesOfIteration);

        PartRevisionKey partRevisionKey = new PartRevisionKey(workspace.getId(),PART_ID, VERSION);
        partMaster.setAttributesLocked(true);

        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.find(PartRevision.class, null)).thenReturn(partRevision);
        Mockito.when(em.find(PartRevision.class, partRevisionKey)).thenReturn(partRevision);

        //PartIterationKey pKey, String pIterationNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes, DocumentIterationKey[] pLinkKeys
        ArrayList<PartUsageLink> partUsageLinks = new ArrayList<>();

        ArrayList<InstanceAttribute> newAttributes = new ArrayList<>();



        try{
            //Test to remove attribute
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{}, null);
            Assert.assertTrue("updatePartIteration should have raise an exception because we have removed attributes", false);
        }catch (NotAllowedException notAllowedException){
            try{
                //Test with a swipe of attribute
                newAttributes.add(new InstanceDateAttribute("Test", new Date(), false));
                productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{}, null);
                Assert.assertTrue("updateDocument should have raise an exception because we have changed the attribute type attributes", false);
            }catch (NotAllowedException notAllowedException2){
                try {
                    //Test without modifying the attribute
                    newAttributes = new ArrayList<>();
                    newAttributes.add(attribute);
                    productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{}, null);
                    //Test with a new value of the attribute
                    newAttributes = new ArrayList<>();
                    newAttributes.add(new InstanceTextAttribute("Test", "newValue", false));
                    productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{}, null);
                } catch (NotAllowedException notAllowedException3){
                    Assert.assertTrue("updateDocument shouldn't have raised an exception because we haven't change the number of attribute or the type", false);
                }
            }
        }

    }

    @Test
    public void updatePartWithUnlockedAttributes() throws Exception {
        //Creation of current attributes of the iteration
        InstanceAttribute attribute = new InstanceTextAttribute("Test", "Testeur", false);
        List<InstanceAttribute> attributesOfIteration = new ArrayList<>();
        attributesOfIteration.add(attribute);
        partIteration.setInstanceAttributes(attributesOfIteration);

        PartRevisionKey partRevisionKey = new PartRevisionKey(workspace.getId(),PART_ID, VERSION);
        partMaster.setAttributesLocked(false);

        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.find(PartRevision.class, null)).thenReturn(partRevision);
        Mockito.when(em.find(PartRevision.class, partRevisionKey)).thenReturn(partRevision);

        //PartIterationKey pKey, String pIterationNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes, DocumentIterationKey[] pLinkKeys
        ArrayList<PartUsageLink> partUsageLinks = new ArrayList<>();

        ArrayList<InstanceAttribute> newAttributes = new ArrayList<>();



        try{
            //Test to remove attribute
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{}, null);
            //Test with a swipe of attribute
            newAttributes.add(new InstanceDateAttribute("Test", new Date(), false));
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{}, null);
            //Test without modifying the attribute
            newAttributes = new ArrayList<>();
            newAttributes.add(attribute);
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{}, null);
            //Test with a new value of the attribute
            newAttributes = new ArrayList<>();
            newAttributes.add(new InstanceTextAttribute("Test", "newValue", false));
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{}, null);

        }catch (NotAllowedException notAllowedException){
            Assert.assertTrue("updateDocument shouldn't have raised an exception because the attributes are not frozen", false);
        }

    }
}
