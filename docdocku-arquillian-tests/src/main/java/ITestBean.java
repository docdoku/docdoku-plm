import com.docdoku.core.common.Workspace;

import javax.ejb.Local;

/**
 * @author: Asmae CHADID
 */

@Local
public interface ITestBean {

    public void testEJB(String name);
    public void createUserGroup(Workspace workspace);
}