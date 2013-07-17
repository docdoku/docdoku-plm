import javax.ejb.Local;

/**
 * @author: Asmae CHADID
 */

@Local
public interface ITestBean {

    void testEJB(String name);
}