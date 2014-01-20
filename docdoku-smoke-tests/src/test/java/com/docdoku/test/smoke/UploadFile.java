/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.test.smoke;

import com.docdoku.cli.ScriptingTools;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductManagerWS;
import com.docdoku.core.util.FileIO;

import java.io.File;

/**
 * @author Asmae Chadid
 *
 */
public class UploadFile {

    private SmokeTestProperties properties = new SmokeTestProperties();
    private final static String CAD_FILE = "/com/docdoku/test/smoke/KTM.obj";
    private final static String PART_NUMBER = "Test-Part";
    public void upload() throws Exception {

        File cadFile =  FileIO.urlToFile(UploadFile.class.getResource(CAD_FILE));
        IProductManagerWS productS = ScriptingTools.createProductService(properties.getURL(), properties.getLoginForUser2(), properties.getPassword());
        productS.createPartMaster(properties.getWorkspace(), PART_NUMBER, "", true, null, "", null, null, null,null);

        PartMasterKey partMPK = new PartMasterKey(properties.getWorkspace(), PART_NUMBER);

        PartRevisionKey partRPK = new PartRevisionKey(partMPK, "A");
        PartRevision pr = productS.getPartRevision(partRPK);
        PartIteration pi = pr.getLastIteration();
        PartIterationKey partIPK = new PartIterationKey(partRPK, pi.getIteration());
        FileHelper fh = new FileHelper(properties.getLoginForUser2(), properties.getPassword());
        fh.uploadNativeCADFile(properties.getURL(), cadFile, partIPK);
        productS.deletePartMaster(partMPK);
    }


}


