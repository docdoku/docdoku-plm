/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.CADInstance;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.InternalService;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.catia.product.parser.ComponentDTK;
import com.docdoku.server.converters.catia.product.parser.ComponentDTKSaxHandler;
import com.docdoku.server.converters.utils.ConversionResult;
import com.docdoku.server.converters.utils.ConverterUtils;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@CatiaProductFileParser
public class CatiaProductFileParserImpl implements CADConverter {

    private static final String CONF_PROPERTIES = "/com/docdoku/server/converters/catia/product/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(CatiaProductFileParserImpl.class.getName());

    @InternalService
    @Inject
    private IProductManagerLocal productService;

    @InternalService
    @Inject
    private IDataManagerLocal dataManager;

    static {
        try (InputStream inputStream = CatiaProductFileParserImpl.class.getResourceAsStream(CONF_PROPERTIES)){
            CONF.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @Override
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, File tempDir) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {

        File tmpCadFile;
        File tmpXMLFile = new File(tempDir, cadFile.getName() + "_dtk.xml");

        String catProductReader = CONF.getProperty("catProductReader");

        File executable = new File(catProductReader);

        if(!executable.exists()){
            LOGGER.log(Level.SEVERE, "Cannot convert file \""+cadFile.getName()+"\", \""+catProductReader+"\" is not available");
            return null;
        }

        if(!executable.canExecute()){
            LOGGER.log(Level.SEVERE, "Cannot convert file \""+cadFile.getName()+"\", \""+catProductReader+"\" has no execution rights");
            return null;
        }

        tmpCadFile = new File(tempDir, cadFile.getName());

        try(InputStream in = dataManager.getBinaryResourceInputStream(cadFile)) {
            Files.copy(in, tmpCadFile.toPath());
        } catch (StorageException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new IOException(e);
        }

        String[] args = {"sh", catProductReader, tmpCadFile.getAbsolutePath()};

        ProcessBuilder pb = new ProcessBuilder(args);
        Process process = pb.start();

        // Read buffers
        String stdOutput = ConverterUtils.getOutput(process.getInputStream());
        String errorOutput = ConverterUtils.getOutput(process.getErrorStream());

        LOGGER.info(stdOutput);

        process.waitFor();

        int exitCode = process.exitValue();

        if (exitCode == 0 && tmpXMLFile.exists() && tmpXMLFile.length() > 0) {

            try {

                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                ComponentDTKSaxHandler handler = new ComponentDTKSaxHandler();
                saxParser.parse(tmpXMLFile, handler);

                syncAssembly(handler.getComponent(), partToConvert);

            } catch (ParserConfigurationException | SAXException | IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }else {
            LOGGER.log(Level.SEVERE, "Cannot parse catia product file: " + tmpCadFile.getAbsolutePath(), errorOutput);
        }

        return null;
    }

    private void syncAssembly(ComponentDTK componentDtk, PartIteration partToConvert) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        List<PartUsageLink> partUsageLinks = new ArrayList<>();

        Map<String, List<CADInstance>> mapInstances = new HashMap<>();

        parseSubComponents(mapInstances, componentDtk);

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



    private void parseSubComponents(Map<String, List<CADInstance>> mapInstances, ComponentDTK root) {

        List<ComponentDTK> subComponentDtkList = root.getSubComponentDtkList();
        if (subComponentDtkList != null) {

            for (ComponentDTK componentDTK : subComponentDtkList) {

                if (componentDTK.isLinkable()) {

                    List<CADInstance> cadInstances = mapInstances.get(componentDTK.getName());

                    if (cadInstances == null) {
                        cadInstances = new LinkedList<>();
                        mapInstances.put(componentDTK.getName(),cadInstances);
                    }

                    mapInstances.put(componentDTK.getName(),cadInstances);

                    CADInstance instance = componentDTK.getPositioning().toCADInstance();

                    if (instance != null) {
                        cadInstances.add(instance);
                    }
                }

                parseSubComponents(mapInstances, componentDTK);
            }
        }
    }


    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return "catproduct".equals(cadFileExtension);
    }

}
