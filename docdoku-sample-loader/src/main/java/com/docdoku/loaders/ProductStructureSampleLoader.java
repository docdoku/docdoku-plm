/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceNumberAttribute;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductManagerWS;
import com.docdoku.core.services.IUploadDownloadWS;

import javax.activation.DataHandler;
import java.io.Console;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductStructureSampleLoader {

    private static String workspace;
    private static IProductManagerWS pm;
    private static IUploadDownloadWS fm;
    private static final Logger LOGGER = Logger.getLogger(ProductStructureSampleLoader.class.getName());

    private ProductStructureSampleLoader(){
    }

    public static void main(String[] args) throws Exception {
        try{
            String serverURL;
            String login;
            String password;
            boolean completlySuccess = true;
            if (args.length >= 3) {
                login =args[0];
                password =args[1];
                workspace=args[2];
                serverURL= (args.length==4) ? args[3] : "http://localhost:8080";
            }else{
                Console c = System.console();
                login = c.readLine("Please enter your login: ");
                password = new String(c.readPassword("Please enter your password: "));
                workspace = c.readLine("Please enter the workspace into which the sample data will be imported: ");
                serverURL = c.readLine("Please enter the URL of your DocDokuPLM server, http://localhost:8080 for example: ");
            }

            pm = ScriptingTools.createProductService(serverURL + "/services/product?wsdl", login, password);
            fm = ScriptingTools.createFileManagerService(serverURL + "/services/UploadDownload?wsdl", login, password);

            LOGGER.log(Level.INFO, "importing data...");
            completlySuccess &= createBikeSampleProduct();
            //createBuildingSampleProduct();
            if(completlySuccess){
                LOGGER.log(Level.INFO, "...done!");
            }else{
                LOGGER.log(Level.WARNING, "...incomplete!");
            }
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "...FAIL!",e);
        }
    }

/*
    private static void createBuildingSampleProduct() throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, PartRevisionNotFoundException, UserNotActiveException, FileAlreadyExistsException, IOException {
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
*/

    private static boolean createBikeSampleProduct() {
        final String createBikeSampleProductFail = "The Bike creation fail.\n";

        try {
            pm.createPartMaster(workspace, "BMX", "BMX", false, null, "created by loader", null, null, null, null);
            pm.createConfigurationItem(workspace, "Bike", "Bicycle Motocross", "BMX");

            PartMaster componentM = pm.createPartMaster(workspace, "BPM12VTX", "", false, null, "", null, null, null, null);
            List<PartUsageLink> subParts = new ArrayList<>();
            PartUsageLink link = new PartUsageLink();
            link.setAmount(1);
            link.setComponent(componentM);
            List<CADInstance> cads = new ArrayList<>();
            cads.add(new CADInstance(0D, 0D, 0D, 0D, 0D, 0D));
            link.setCadInstances(cads);
            subParts.add(link);

            pm.updatePartIteration(new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspace, "BMX"), "A"), 1), "created by loader", PartIteration.Source.MAKE, subParts, null, null);

            List<InstanceAttribute> attrs = new ArrayList<>();
            InstanceNumberAttribute instanceAttribute = new InstanceNumberAttribute("radius", 9000000000f, false);
            attrs.add(instanceAttribute);
            pm.updatePartIteration(new PartIterationKey(new PartRevisionKey(new PartMasterKey(workspace, "BPM12VTX"), "A"), 1), "created by loader", PartIteration.Source.MAKE, null, attrs, null);


            URL jsonURL = ProductStructureSampleLoader.class.getResource("/com/docdoku/loaders/bike.js");
            URL binURL = ProductStructureSampleLoader.class.getResource("/com/docdoku/loaders/bike.bin");

            DataHandler dh = new DataHandler(jsonURL);
            fm.uploadGeometryToPart(workspace, "BPM12VTX", "A", 1, "bike.js", 0, dh);

            dh = new DataHandler(binURL);
            fm.uploadToPart(workspace, "BPM12VTX", "A", 1, "bike.bin", dh);
            return true;
        } catch (AccessRightException | UserNotFoundException | UserNotActiveException e) {
            LOGGER.log(Level.SEVERE, createBikeSampleProductFail + "Forbidden Exception provide by DocdokuPLM", e);
            return false;
        } catch (NotAllowedException e) {
            LOGGER.log(Level.SEVERE, createBikeSampleProductFail + "Not Allowed Exception provide by DocdokuPLM", e);
            return false;
        } catch (CreationException e) {
            LOGGER.log(Level.SEVERE, createBikeSampleProductFail + "Internal Server Error Exception provide by DocdokuPLM", e);
            return false;
        }catch (PartMasterAlreadyExistsException | ConfigurationItemAlreadyExistsException | FileAlreadyExistsException e) {
            LOGGER.log(Level.SEVERE, createBikeSampleProductFail + "Conflict Exception provide by DocdokuPLM", e);
            return false;
        } catch (WorkspaceNotFoundException | PartRevisionNotFoundException | WorkflowModelNotFoundException | RoleNotFoundException | PartMasterNotFoundException | PartMasterTemplateNotFoundException e) {
            LOGGER.log(Level.SEVERE, createBikeSampleProductFail + "Not Found Exception provide by DocdokuPLM", e);
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, createBikeSampleProductFail + "IO Exception provide by DocdokuPLM", e);
            return false;
        }
    }
}
