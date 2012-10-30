/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */
package com.docdoku.loaders;

import com.docdoku.client.ScriptingTools;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceNumberAttribute;
import com.docdoku.core.product.CADInstance;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.services.AccessRightException;
import com.docdoku.core.services.ConfigurationItemAlreadyExistsException;
import com.docdoku.core.services.CreationException;
import com.docdoku.core.services.FileAlreadyExistsException;
import com.docdoku.core.services.IProductManagerWS;
import com.docdoku.core.services.IUploadDownloadWS;
import com.docdoku.core.services.NotAllowedException;
import com.docdoku.core.services.PartMasterAlreadyExistsException;
import com.docdoku.core.services.PartRevisionNotFoundException;
import com.docdoku.core.services.UserNotActiveException;
import com.docdoku.core.services.UserNotFoundException;
import com.docdoku.core.services.WorkflowModelNotFoundException;
import com.docdoku.core.services.WorkspaceNotFoundException;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import org.junit.Before;
import org.junit.Test;

public class ProductStructureLoaderTest {

    private String login;
    private String password;
    private String workspace;
    private IProductManagerWS pm;
    private IUploadDownloadWS fm;

    @Before
    public void setUp() throws MalformedURLException, Exception {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/com/docdoku/loaders/config.properties"));
        login = props.getProperty("login");
        password = props.getProperty("password");
        workspace = props.getProperty("workspace");
        String serverURL = props.getProperty("serverURL","http://localhost:8080");
        //pm = ScriptingTools.createProductService("http://plm.docdoku.net/services/product?wsdl", LOGIN, PASSWORD);
        //fm = ScriptingTools.createFileManagerService("http://plm.docdoku.net/services/UploadDownload?wsdl", LOGIN, PASSWORD);

        pm = ScriptingTools.createProductService(serverURL + "/services/product?wsdl", login, password);
        fm = ScriptingTools.createFileManagerService(serverURL + "/services/UploadDownload?wsdl", login, password);


    }

    @Test
    public void createBikeSampleProduct() throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, PartRevisionNotFoundException, UserNotActiveException, FileAlreadyExistsException, IOException {

        PartMaster rootBMX = pm.createPartMaster(workspace, "BMX", "BMX", "", false, null, "created by loader");
        pm.createConfigurationItem(workspace, "Bike", "Bicycle Motocross", "BMX");

        PartMaster componentM = pm.createPartMaster(workspace, "BPM12VTX", "", "", false, null, "");
        List<PartUsageLink> subParts = new ArrayList<PartUsageLink>();
        PartUsageLink link = new PartUsageLink();
        link.setAmount(1);
        link.setComponent(componentM);
        List<CADInstance> cads = new ArrayList<CADInstance>();
        cads.add(new CADInstance(0D, 0D, 0D, 0D, 0D, 0D, CADInstance.Positioning.ABSOLUTE));
        link.setCadInstances(cads);
        subParts.add(link);

        pm.updatePartIteration(new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspace, "BMX"), "A"), 1), "created by loader", PartIteration.Source.MAKE, subParts, null);


        PartRevision componentR = componentM.getLastRevision();
        PartIteration componentI = componentR.getLastIteration();

        List<InstanceAttribute> attrs = new ArrayList<InstanceAttribute>();
        InstanceNumberAttribute instanceAttribute = new InstanceNumberAttribute("radius", 9000000000f);
        attrs.add(instanceAttribute);
        pm.updatePartIteration(new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspace, "BPM12VTX"), "A"), 1), "created by loader", PartIteration.Source.MAKE, null, attrs);


        URL jsonURL = getClass().getResource("/com/docdoku/samples/models/bike.js");
        URL binURL = getClass().getResource("/com/docdoku/samples/models/bike.bin");

        DataHandler dh = new DataHandler(jsonURL);
        fm.uploadGeometryToPart(workspace, "BPM12VTX", "A", 1, "bike.js", 0, dh);

        dh = new DataHandler(binURL);
        fm.uploadToPart(workspace, "BPM12VTX", "A", 1, "bike.bin", dh);

    }
}
