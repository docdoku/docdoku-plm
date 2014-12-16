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

package com.docdoku.server.converters.kmz;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import javax.ejb.EJB;
import javax.xml.stream.*;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@KMZFileConverter
public class KMZFileConverterImpl implements CADConverter{

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDataManagerLocal dataManager;

    @Override
    public File convert(PartIteration partToConvert, final BinaryResource cadFile) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, XMLStreamException, StorageException {
        File tmpDir = Files.createTempDir();
        File tmpCadFile = new File(tmpDir, cadFile.getName());
        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                try {
                    return dataManager.getBinaryResourceInputStream(cadFile);
                } catch (StorageException e) {
                    Logger.getLogger(KMZFileConverterImpl.class.getName()).log(Level.SEVERE, null, e);
                    throw new IOException(e);
                }
            }
        }, tmpCadFile);

        FileIO.unzipArchive(tmpCadFile, tmpDir);
        File tmpDAEFile= new File(tmpDir, "models/untitled.dae");

        File tmpTexturesDir = new File(tmpDir, "models/untitled");

        File daeFile = null;
        File tmpNewDAEFile;
        try {
            tmpNewDAEFile = parseDAEFile(tmpDAEFile, tmpDir);
            PartIterationKey partIPK = partToConvert.getKey();

            // Upload each textures
            if(tmpTexturesDir.isDirectory()) {
                for (File tempTexture : tmpTexturesDir.listFiles()) {
                    if (!tempTexture.isDirectory()) {
                        BinaryResource finalTextureBinaryResource = productService.saveFileInPartIteration(partIPK, tempTexture.getName(), tempTexture.length());
                        OutputStream finalTextureOutputStream = null;
                        try {
                            finalTextureOutputStream = dataManager.getBinaryResourceOutputStream(finalTextureBinaryResource);
                            Files.copy(tempTexture, finalTextureOutputStream);
                        } finally {
                            if(finalTextureOutputStream!=null){
                                finalTextureOutputStream.flush();
                                finalTextureOutputStream.close();
                            }
                        }
                    }
                }
            }

            // Upload dae
            BinaryResource daeBinaryResource = productService.saveGeometryInPartIteration(partIPK, tmpNewDAEFile.getName(), 0, tmpNewDAEFile.length(),null);
            OutputStream daeOutputStream = null;
            try {
                daeOutputStream = dataManager.getBinaryResourceOutputStream(daeBinaryResource);
                Files.copy(tmpNewDAEFile, daeOutputStream);
            } finally {
                if(daeOutputStream!=null){
                    daeOutputStream.flush();
                    daeOutputStream.close();
                }
            }
            return daeFile;                                                                                             // Todo Check "why are we always return null?"
        }
        finally {
            FileIO.rmDir(tmpDir);
        }
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return "kmz".equalsIgnoreCase(cadFileExtension);
    }

    public File parseDAEFile (File daeFile, File tempDir) throws XMLStreamException, IOException {

        File newDaeFile = new File(tempDir, "models/untitled_final.dae");

        XMLEventFactory XMLef = XMLEventFactory.newInstance();
        XMLInputFactory XMLif = XMLInputFactory.newInstance();
        XMLOutputFactory XMLof = XMLOutputFactory.newInstance();

        XMLEventReader reader = XMLif.createXMLEventReader(new FileInputStream(daeFile));
        XMLEventWriter writer = XMLof.createXMLEventWriter(new FileOutputStream(newDaeFile));

        while(reader.hasNext()){

            XMLEvent event = reader.nextEvent();

            if(event.getEventType() == XMLStreamConstants.CHARACTERS){
                Characters characters = event.asCharacters();
                String text = characters.getData();
                if (text.contains("untitled/")) {
                    text = text.replaceAll("untitled/","");
                    Characters texture = XMLef.createCharacters(text);
                    writer.add(texture);
                } else {
                    writer.add(event);
                }
            } else {
                writer.add(event);
            }
        }
        writer.flush();
        reader.close();
        writer.close();

        return newDaeFile;
    }
}
