/**
 * @author: Asmae CHADID
 */
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.services.*;
import com.docdoku.server.ActivityCheckerInterceptor;
import com.docdoku.server.DocumentLoggerInterceptor;
import com.docdoku.server.DocumentManagerBean;
import com.docdoku.server.IndexerBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class UserTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClasses(
                        DocumentManagerBean.class,
                        IDocumentManagerLocal.class,
                        User.class, Workspace.class,
                        DocumentLoggerInterceptor.class,
                        ActivityCheckerInterceptor.class,
                        IUserManagerLocal.class,
                        IMailerLocal.class,
                        IndexerBean.class
                )
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    //@EJB
    //UserManagerBean userManagerBean = new UserManagerBean();

    @EJB
    IDocumentManagerLocal documentManagerBean = new DocumentManagerBean();

    @Inject
    Workspace workspace = new Workspace("workspace1");

    @Inject
    User user = new User(workspace,"toto") ;

    @Test

    public void test_workspaceInsertion() throws UserAlreadyExistsException, WorkspaceNotFoundException, AccessRightException, CreationException, FolderAlreadyExistsException, AccountNotFoundException {

        //user.setLogin("use1");
        //userManagerBean.addUserInWorkspace(workspace.getId(),user.getLogin());
        //Assert.assertEquals(workspace.getId(), user.getWorkspaceId());

        try {
            User user = documentManagerBean.whoAmI(workspace.getId());

            assertTrue(user.getLogin().equals("toto"));
            assertTrue("Exception wasn't thrown", false);
        } catch (UserNotFoundException e) {
            assertTrue(true);
        } catch (UserNotActiveException e) {
            assertTrue(true);
        }


    }
}
