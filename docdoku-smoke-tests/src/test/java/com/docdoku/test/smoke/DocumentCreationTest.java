package com.docdoku.test.smoke;

import com.docdoku.test.smoke.DocumentCreation;
import org.junit.Test;

import static junit.framework.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: asmaechadid
 * Date: 14/03/13
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class DocumentCreationTest {


    @Test
    public void test() {
        DocumentCreation documentCreationTest = new DocumentCreation();
        try {
            documentCreationTest.createDocument();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}
