import com.docdoku.core.services.IDocumentManagerLocal;
import com.sun.appserv.security.ProgrammaticLogin;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * @author: Asmae CHADID
 */

@DeclareRoles("users")
@Stateless
@RunAs("users")
public class TestEJBBean implements ITestBean {

    @EJB
    private IDocumentManagerLocal documentManagerLocal;

    public void testEJB(String name) {
        com.sun.appserv.security.ProgrammaticLogin loginP = new ProgrammaticLogin();
        try{
            System.out.println("LOGIN RETURN::::"+loginP.login("user1", "password".toCharArray()));
            System.out.println( "who am i returns:::::"+documentManagerLocal.whoAmI("w1").getEmail());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}