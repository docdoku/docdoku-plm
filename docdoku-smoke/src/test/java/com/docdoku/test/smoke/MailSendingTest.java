package com.docdoku.test.smoke;

import com.docdoku.test.smoke.MailSending;
import org.junit.Test;

import static junit.framework.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: asmaechadid
 * Date: 14/03/13
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */
public class MailSendingTest {

    @Test
    public void test(){
    MailSending mailSending = new MailSending();

        try {
            mailSending.checkInCheckOut();
            mailSending.checkMailReception();
        } catch (Exception e) {
             fail(e.getMessage());
        }
    }
}
