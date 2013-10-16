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

package com.docdoku.server.converters.catia.product;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.CADInstance;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.services.*;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.catia.product.parser.ComponentDTKSaxHandler;
import com.docdoku.server.converters.catia.product.parser.Component_DTK;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.xml.sax.SAXException;

import javax.ejb.EJB;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;


@CatiaProductFileParser
public class CatiaProductFileParserImpl implements CADConverter {

    private final static String CONF_PROPERTIES = "/com/docdoku/server/converters/catia/product/conf.properties";
    private final static Properties CONF = new Properties();

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDataManagerLocal dataManager;

    static {
        try {
            CONF.load(CatiaProductFileParserImpl.class.getResourceAsStream(CONF_PROPERTIES));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File convert(PartIteration partToConvert, final BinaryResource cadFile) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {
        String woExName = FileIO.getFileNameWithoutExtension(cadFile.getName());
        File tmpDir = Files.createTempDir();
        File tmpCadFile;
        File tmpXMLFile = new File(tmpDir, cadFile.getName() + "_dtk.xml");
        try {

            String catProductReader = CONF.getProperty("catProductReader");

            tmpCadFile = new File(tmpDir, cadFile.getName());

            Files.copy(new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    try {
                        return dataManager.getBinaryResourceInputStream(cadFile);
                    } catch (StorageException e) {
                        e.printStackTrace();
                        throw new IOException(e);
                    }
                }
            }, tmpCadFile);

            String[] args = {"sh", catProductReader, tmpCadFile.getAbsolutePath()};

            ProcessBuilder pb = new ProcessBuilder(args);
            Process process = pb.start();

            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            String line = "";
            while ((line = br.readLine()) != null);

            process.waitFor();

            int exitCode = process.exitValue();

            if (exitCode == 0) {
                if (tmpXMLFile.exists() && tmpXMLFile.length() > 0) {

                    try {

                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        SAXParser saxParser = factory.newSAXParser();
                        ComponentDTKSaxHandler handler = new ComponentDTKSaxHandler();
                        saxParser.parse(tmpXMLFile, handler);

                        syncAssembly(handler.getComponent(), partToConvert);

                    } catch (ParserConfigurationException pce) {
                        pce.printStackTrace();
                    } catch (SAXException se) {
                        se.printStackTrace();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileIO.rmDir(tmpDir);
        }

        return null;
    }

    private void syncAssembly(Component_DTK component_dtk, PartIteration partToConvert) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        List<PartUsageLink> partUsageLinks = new ArrayList<PartUsageLink>();

        Map<String, List<CADInstance>> mapInstances = new HashMap<String, List<CADInstance>>();

        parseSubComponents(mapInstances, component_dtk);

        for(Map.Entry<String,List<CADInstance>> entry : mapInstances.entrySet()){
            String cadFileName = entry.getKey();
            List<CADInstance> instances = entry.getValue();

            PartMaster partMaster = productService.findPartMasterByCADFileName(partToConvert.getWorkspaceId(), cadFileName);
            if(partMaster != null){
                PartUsageLink partUsageLink = new PartUsageLink();
                partUsageLink.setAmount(instances.size());
                partUsageLink.setComponent(partMaster);
                partUsageLink.setCadInstances(instances);
                partUsageLinks.add(partUsageLink);
            }

        }

        // Erase old structure
        partToConvert.setComponents(partUsageLinks);

    }



    private void parseSubComponents(Map<String, List<CADInstance>> mapInstances, Component_DTK root) {

        List<Component_DTK> subComponentDtkList = root.getSubComponentDtkList();
        if (subComponentDtkList != null) {

            for (Component_DTK component_dtk : subComponentDtkList) {

                if (component_dtk.isLinkable()) {

                    List<CADInstance> cadInstances = mapInstances.get(component_dtk.getName());

                    if (cadInstances == null) {
                        cadInstances = new LinkedList<CADInstance>();
                        mapInstances.put(component_dtk.getName(),cadInstances);
                    }

                    mapInstances.put(component_dtk.getName(),cadInstances);

                    CADInstance instance = component_dtk.getPositioning().toCADInstance();

                    if (instance != null) {
                        cadInstances.add(instance);
                    }
                }

                parseSubComponents(mapInstances, component_dtk);
            }
        }
    }


    @Override
    public boolean canConvertToJSON(String cadFileExtension) {
        return "catproduct".equals(cadFileExtension);
    }

}
