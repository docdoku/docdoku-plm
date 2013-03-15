package com.docdoku.test.smoke;

import com.docdoku.cli.ScriptingTools;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IProductManagerWS;

import java.io.File;

import static junit.framework.TestCase.fail;

/**
 * Created with IntelliJ IDEA.
 * User: asmaechadid
 * Date: 11/03/13
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */
public class UploadFile {

    public void upload() throws Exception {
        String pathObjFile = "/Users/asmaechadid/Downloads/obj/joint_part1.obj";
        //TestParameters parameters= new TestParameters();
        SmokeTestProperties properties = new SmokeTestProperties();
        String partNumber = "testPartt1";
        File cadFile = new File(pathObjFile);
        IProductManagerWS productS = ScriptingTools.createProductService(properties.getURL(), properties.getLoginForUser2(), properties.getPassword());
        if (productS.findPartMasters(properties.getWorkspace(),partNumber,1).isEmpty())
        {
            productS.createPartMaster(properties.getWorkspace(), partNumber, "", "", true, null, "");

        }
        PartRevisionKey partRPK = new PartRevisionKey(properties.getWorkspace(), partNumber, "A");
        PartRevision pr = productS.getPartRevision(partRPK);
        PartIteration pi = pr.getLastIteration();
        PartIterationKey partIPK = new PartIterationKey(partRPK, pi.getIteration());
        FileHelper fh = new FileHelper(properties.getLoginForUser2(), properties.getPassword());
        fh.uploadNativeCADFile(properties.getURL(), cadFile, partIPK);
        System.out.println("Upload file testing has executed with success");
        /*ConfigurationItem configItem = productS.createConfigurationItem(TestParameters.getWorkspace(), "newItem2", "no description", partNumber);
        ConfigurationItemKey configurationItemKey = new ConfigurationItemKey();
        configurationItemKey.setId(configItem.getId());
        configurationItemKey.setWorkspace(configItem.getWorkspace().getId());
        productS.deleteConfigurationItem(configurationItemKey);*/

    }


}


