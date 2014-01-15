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
package com.docdoku.loaders;

import com.docdoku.client.ScriptingTools;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceNumberAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.services.*;

import javax.activation.DataHandler;
import java.io.Console;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProductStructureSampleLoader {

    private static String login;
    private static String password;
    private static String workspace;
    private static IProductManagerWS pm;
    private static IUploadDownloadWS fm;

    public static void main(String[] args) throws MalformedURLException, Exception {
        Console c = System.console();
        login = c.readLine("Please enter your login: ");
        password = new String(c.readPassword("Please enter your password: "));
        workspace = c.readLine("Please enter the workspace into which the sample data will be imported: ");
        String serverURL = c.readLine("Please enter the URL of your DocDokuPLM server, http://localhost:8080 for example: ");
        pm = ScriptingTools.createProductService(serverURL + "/services/product?wsdl", login, password);
        fm = ScriptingTools.createFileManagerService(serverURL + "/services/UploadDownload?wsdl", login, password);

        System.out.println("importing data...");
        createBikeSampleProduct();
        //createBuildingSampleProduct();
        System.out.println("...done!");
    }

    private static void createBuildingSampleProduct() throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, PartRevisionNotFoundException, UserNotActiveException, FileAlreadyExistsException, IOException {
        /*
        pm.createPartMaster(workspace, "modiffinal", "modiffinal", "", false, null, "created by loader");
        pm.createConfigurationItem(workspace, "modiffinal", "modiffinal Building", "modiffinal");

        PartMaster componentM = pm.createPartMaster(workspace, "Building-modiffinal", "", "", false, null, "");
        List<PartUsageLink> subParts = new ArrayList<PartUsageLink>();
        PartUsageLink link = new PartUsageLink();
        link.setAmount(1);
        link.setComponent(componentM);
        List<CADInstance> cads = new ArrayList<CADInstance>();
        cads.add(new CADInstance(0D, 0D, 0D, 0D, 0D, 0D));
        link.setCadInstances(cads);
        subParts.add(link);

        pm.updatePartIteration(new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspace, "modiffinal"), "A"), 1), "created by loader", PartIteration.Source.MAKE, subParts, null);


        PartRevision componentR = componentM.getLastRevision();
        PartIteration componentI = componentR.getLastIteration();

        List<InstanceAttribute> attrs = new ArrayList<InstanceAttribute>();
        InstanceNumberAttribute instanceAttribute = new InstanceNumberAttribute("radius", 9000000000f);
        attrs.add(instanceAttribute);
        pm.updatePartIteration(new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspace, "Building-modiffinal"), "A"), 1), "created by loader", PartIteration.Source.MAKE, null, attrs);


        URL jsonURL = new File("/Users/flo/Documents/amsycom/exemple_sketchup/OBJ/modiffinal/modiffinal.js").toURI().toURL();
        URL binURL = new File("/Users/flo/Documents/amsycom/exemple_sketchup/OBJ/modiffinal/modiffinal.bin").toURI().toURL();

        DataHandler dh = new DataHandler(jsonURL);
        fm.uploadGeometryToPart(workspace, "Building-modiffinal", "A", 1, "modiffinal.js", 0, dh);

        dh = new DataHandler(binURL);
        fm.uploadToPart(workspace, "Building-modiffinal", "A", 1, "modiffinal.bin", dh);
        */
        //uploadMaterials(new File("/Users/flo/Documents/amsycom/exemple_sketchup/OBJ/modiffinal/"),"Building-modiffinal", "A", 1);

    }
    
            
    private static void uploadMaterials(File folder, String partNumber, String partVersion, int iteration) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, IOException{
        String[] mats = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.endsWith(".jpg") || name.endsWith(".JPG");
            }
        });
        
        for(String mat:mats){
            URL matURL = new File(folder.getAbsolutePath() + File.separator + mat).toURI().toURL();
            DataHandler dh = new DataHandler(matURL);
            fm.uploadToPart(workspace, partNumber, partVersion, iteration, mat, dh);
        }
    }
    
    private static void createBikeSampleProduct() throws Exception {

        PartMaster rootBMX = pm.createPartMaster(workspace, "BMX", "BMX", "", false, null, "created by loader",null,null,null,null);
        pm.createConfigurationItem(workspace, "Bike", "Bicycle Motocross", "BMX");

        PartMaster componentM = pm.createPartMaster(workspace, "BPM12VTX", "", "", false, null, "",null,null,null,null);
        List<PartUsageLink> subParts = new ArrayList<PartUsageLink>();
        PartUsageLink link = new PartUsageLink();
        link.setAmount(1);
        link.setComponent(componentM);
        List<CADInstance> cads = new ArrayList<CADInstance>();
        cads.add(new CADInstance(0D, 0D, 0D, 0D, 0D, 0D));
        link.setCadInstances(cads);
        subParts.add(link);

        pm.updatePartIteration(new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspace, "BMX"), "A"), 1), "created by loader", PartIteration.Source.MAKE, subParts, null, null);


        PartRevision componentR = componentM.getLastRevision();
        PartIteration componentI = componentR.getLastIteration();

        List<InstanceAttribute> attrs = new ArrayList<InstanceAttribute>();
        InstanceNumberAttribute instanceAttribute = new InstanceNumberAttribute("radius", 9000000000f);
        attrs.add(instanceAttribute);
        pm.updatePartIteration(new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspace, "BPM12VTX"), "A"), 1), "created by loader", PartIteration.Source.MAKE, null, attrs, null);


        URL jsonURL = ProductStructureSampleLoader.class.getResource("/com/docdoku/loaders/bike.js");
        URL binURL = ProductStructureSampleLoader.class.getResource("/com/docdoku/loaders/bike.bin");

        DataHandler dh = new DataHandler(jsonURL);
        fm.uploadGeometryToPart(workspace, "BPM12VTX", "A", 1, "bike.js", 0, dh);

        dh = new DataHandler(binURL);
        fm.uploadToPart(workspace, "BPM12VTX", "A", 1, "bike.bin", dh);

    }
}
