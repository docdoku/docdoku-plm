package com.docdoku.server;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;
import com.docdoku.core.meta.InstanceTextAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.services.IUserManagerLocal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by lebeaujulien on 19/02/15.
 */
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
    public void updatePartWithLockedAttributs() throws Exception {
        //Creation of current attributs of the iteration
        InstanceAttribute attribut = new InstanceTextAttribute("Test", "Testeur", false);
        HashMap<String, InstanceAttribute> attributsOfIteration = new HashMap<>();
        attributsOfIteration.put(attribut.getName(), attribut);
        partIteration.setInstanceAttributes(attributsOfIteration);

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
            //Test to remove attribut
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{});
            Assert.assertTrue("updatePartIteration should have raise an exception because we have removed attributs", false);
        }catch (NotAllowedException notAllowedException){
            try{
                //Test with a swipe of attribut
                newAttributes.add(new InstanceDateAttribute("Test", new Date(), false));
                productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{});
                Assert.assertTrue("updateDocument should have raise an exception because we have changed the attribut type attributs", false);
            }catch (NotAllowedException notAllowedException2){
                try {
                    //Test without modifying the attribut
                    newAttributes = new ArrayList<>();
                    newAttributes.add(attribut);
                    productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{});
                    //Test with a new value of the attribut
                    newAttributes = new ArrayList<>();
                    newAttributes.add(new InstanceTextAttribute("Test", "newValue", false));
                    productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{});
                } catch (NotAllowedException notAllowedException3){
                    Assert.assertTrue("updateDocument shouldn't have raised an exception because we haven't change the number of attribut or the type", false);
                }
            }
        }

    }

    @Test
    public void updatePartWithUnlockedAttributs() throws Exception {
        //Creation of current attributs of the iteration
        InstanceAttribute attribut = new InstanceTextAttribute("Test", "Testeur", false);
        HashMap<String, InstanceAttribute> attributsOfIteration = new HashMap<>();
        attributsOfIteration.put(attribut.getName(), attribut);
        partIteration.setInstanceAttributes(attributsOfIteration);

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
            //Test to remove attribut
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{});
            //Test with a swipe of attribut
            newAttributes.add(new InstanceDateAttribute("Test", new Date(), false));
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{});
            //Test without modifying the attribut
            newAttributes = new ArrayList<>();
            newAttributes.add(attribut);
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{});
            //Test with a new value of the attribut
            newAttributes = new ArrayList<>();
            newAttributes.add(new InstanceTextAttribute("Test", "newValue", false));
            productManagerBean.updatePartIteration(partIteration.getKey(), "Iteration note", null, partUsageLinks, newAttributes, new DocumentIterationKey[]{});

        }catch (NotAllowedException notAllowedException){
            Assert.assertTrue("updateDocument shouldn't have raised an exception because the attributs are not frozen", false);
        }

    }
}
