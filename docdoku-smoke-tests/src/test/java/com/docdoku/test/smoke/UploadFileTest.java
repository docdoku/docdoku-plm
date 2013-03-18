package com.docdoku.test.smoke;

import com.docdoku.test.smoke.UploadFile;
import org.junit.Test;

import static junit.framework.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: asmaechadid
 * Date: 14/03/13
 * Time: 17:03
 * To change this template use File | Settings | File Templates.
 */
public class UploadFileTest {
    @Test
    public void test(){
        UploadFile uploadFile = new UploadFile();
        try {
            uploadFile.upload();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
