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
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.*;
import com.docdoku.core.product.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.products.ProductBaselineManagerBean;
import com.docdoku.server.util.ProductUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

import static org.mockito.MockitoAnnotations.initMocks;

public class ProductManagerBeanTest {

    @InjectMocks
    ProductManagerBean productManagerBean = new ProductManagerBean();

    @Mock
    private EntityManager em;
    @Mock
    private IUserManagerLocal userManager;
    @Mock
    SessionContext ctx;
    @Spy
    private ESIndexer esIndexer = new ESIndexer();
    @Mock
    TypedQuery<Tag> tagsQuery;
    @Mock
    ProductBaselineManagerBean productBaselineManager;


    private Account account;
    private Workspace workspace ;
    private User user;
    private User user2;
    private PartMaster partMaster;
    private PartMasterTemplate partMasterTemplate;
    private PartIteration partIteration;
    private PartRevision partRevision;




    @Before
    public void setup() throws Exception {
        initMocks(this);
        account = new Account(ProductUtil.USER_2_LOGIN, ProductUtil.USER_2_NAME, ProductUtil.USER_1_MAIL, ProductUtil.USER_1_LANGUAGE, new Date(), null);
        workspace = new Workspace(ProductUtil.WORKSPACE_ID,account, "pDescription", false);
        user = new User(workspace, ProductUtil.USER_1_LOGIN , ProductUtil.USER_1_LOGIN, ProductUtil.USER_1_MAIL,ProductUtil.USER_1_LANGUAGE);
        user2 = new User(workspace, ProductUtil.USER_2_LOGIN , ProductUtil.USER_2_LOGIN, ProductUtil.USER_2_MAIL,ProductUtil.USER_2_LANGUAGE);
        partMaster = new PartMaster(workspace, ProductUtil.PART_ID, user);
        partMasterTemplate = new PartMasterTemplate(workspace, ProductUtil.PART_MASTER_TEMPLATE_ID, user, ProductUtil.PART_TYPE, "", true);
        partRevision = new PartRevision(partMaster,ProductUtil.VERSION,user);
        partIteration = new PartIteration(partRevision, ProductUtil.ITERATION,user);
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

        PartRevisionKey partRevisionKey = new PartRevisionKey(workspace.getId(), ProductUtil.PART_ID, ProductUtil.VERSION);
        partMaster.setAttributesLocked(true);

        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.find(PartRevision.class, null)).thenReturn(partRevision);
        Mockito.when(em.find(PartRevision.class, partRevisionKey)).thenReturn(partRevision);

        //PartIterationKey pKey, String pIterationNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes, DocumentIterationKey[] pLinkKeys
        ArrayList<PartUsageLink> partUsageLinks = new ArrayList<>();

        ArrayList<InstanceAttribute> newAttributes = new ArrayList<>();
        ArrayList<InstanceAttributeTemplate> newAttributeTemplates = new ArrayList<>();
        String[] lovNames = new String[0];



        try{
            //Test to remove attribute
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, newAttributeTemplates, new DocumentIterationKey[]{}, null, lovNames);
            Assert.assertTrue("updatePartIteration should have raise an exception because we have removed attributes", false);
        }catch (NotAllowedException notAllowedException){
            try{
                //Test with a swipe of attribute
                newAttributes.add(new InstanceDateAttribute("Test", new Date(), false));
                productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, newAttributeTemplates, new DocumentIterationKey[]{}, null, lovNames);
                Assert.assertTrue("updateDocument should have raise an exception because we have changed the attribute type attributes", false);
            }catch (NotAllowedException notAllowedException2){
                try {
                    //Test without modifying the attribute
                    newAttributes = new ArrayList<>();
                    newAttributes.add(attribute);
                    productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, newAttributeTemplates, new DocumentIterationKey[]{}, null, lovNames);
                    //Test with a new value of the attribute
                    newAttributes = new ArrayList<>();
                    newAttributes.add(new InstanceTextAttribute("Test", "newValue", false));
                    productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, newAttributeTemplates, new DocumentIterationKey[]{}, null, lovNames);
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

        PartRevisionKey partRevisionKey = new PartRevisionKey(workspace.getId(),ProductUtil.PART_ID, ProductUtil.VERSION);
        partMaster.setAttributesLocked(false);

        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.find(PartRevision.class, null)).thenReturn(partRevision);
        Mockito.when(em.find(PartRevision.class, partRevisionKey)).thenReturn(partRevision);

        //PartIterationKey pKey, String pIterationNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes, DocumentIterationKey[] pLinkKeys
        ArrayList<PartUsageLink> partUsageLinks = new ArrayList<>();

        ArrayList<InstanceAttribute> newAttributes = new ArrayList<>();
        ArrayList<InstanceAttributeTemplate> newAttributeTemplates = new ArrayList<>();
        String[] lovNames = new String[0];


        try{
            //Test to remove attribute
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, newAttributeTemplates, new DocumentIterationKey[]{}, null, lovNames);
            //Test with a swipe of attribute
            newAttributes.add(new InstanceDateAttribute("Test", new Date(), false));
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, newAttributeTemplates, new DocumentIterationKey[]{}, null, lovNames);
            //Test without modifying the attribute
            newAttributes = new ArrayList<>();
            newAttributes.add(attribute);
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, newAttributeTemplates, new DocumentIterationKey[]{}, null, lovNames);
            //Test with a new value of the attribute
            newAttributes = new ArrayList<>();
            newAttributes.add(new InstanceTextAttribute("Test", "newValue", false));
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, newAttributeTemplates, new DocumentIterationKey[]{}, null, lovNames);

        }catch (NotAllowedException notAllowedException){
            Assert.assertTrue("updateDocument shouldn't have raised an exception because the attributes are not frozen", false);
        }

    }

    /**
     * test the add of new tags to a part that doesn't have any tag
     * @throws UserNotFoundException
     * @throws WorkspaceNotFoundException
     * @throws UserNotActiveException
     * @throws PartRevisionNotFoundException
     * @throws AccessRightException
     */
    @Test
    public void addTagToPartWithNoTags() throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, TagException {


        PartRevisionKey partRevisionKey = partRevision.getKey();

        String[]tags = new String[3];
        tags[0]="Important";
        tags[1]="ToCheck";
        tags[2]="ToDelete";

        Mockito.when(userManager.checkWorkspaceReadAccess(ProductUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceWriteAccess(ProductUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(PartRevision.class, partRevisionKey)).thenReturn(partRevision);

        Mockito.when(em.createQuery("SELECT DISTINCT t FROM Tag t WHERE t.workspaceId = :workspaceId")).thenReturn(tagsQuery);
        Mockito.when(tagsQuery.setParameter("workspaceId", ProductUtil.WORKSPACE_ID)).thenReturn(tagsQuery);
        Mockito.when(tagsQuery.getResultList()).thenReturn(new ArrayList<Tag>());

        PartRevision partRevisionResult = productManagerBean.saveTags(partRevisionKey, (String[]) tags);

        Assert.assertEquals(partRevisionResult.getTags().size() ,3);
        int i = 0;
        for (Iterator<Tag> it = partRevisionResult.getTags().iterator(); it.hasNext(); ) {
            Tag tag = it.next();
            Assert.assertEquals(tag.getLabel() ,tags[i++]);
        }

    }


    @Test
    public void removeTagFromPart() throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {
        Set<Tag> tags = new LinkedHashSet<Tag>();
        tags.add(new Tag(workspace, "Important"));
        tags.add(new Tag(workspace, "ToRemove"));
        tags.add(new Tag(workspace, "Urgent"));
        partRevision.setTags(tags);

        PartRevisionKey partRevisionKey = partRevision.getKey();
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)).thenReturn(true);
        Mockito.when(userManager.checkWorkspaceReadAccess(ProductUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceWriteAccess(ProductUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(PartRevision.class, partRevisionKey)).thenReturn(partRevision);

        PartRevision partRevisionResult = productManagerBean.removeTag(partRevision.getKey(), "Important");

        Assert.assertEquals(partRevisionResult.getTags().size() ,2);
        Assert.assertFalse(partRevisionResult.getTags().contains(new Tag(workspace,"Important")));
        Assert.assertTrue(partRevisionResult.getTags().contains(new Tag(workspace,"Urgent")));
        Assert.assertTrue(partRevisionResult.getTags().contains(new Tag(workspace,"ToRemove")));

    }

    @Test(expected = TagException.class)
    public void addNullTagToOnePart() throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException, TagException {
        String[] tags = null;
        partRevision.setTags(null);
        PartRevisionKey partRevisionKey = partRevision.getKey();
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)).thenReturn(true);
        Mockito.when(userManager.checkWorkspaceReadAccess(ProductUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceWriteAccess(ProductUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(PartRevision.class, partRevisionKey)).thenReturn(partRevision);
        PartRevision partRevisionResult = productManagerBean.saveTags(partRevisionKey,tags);
    }


}
