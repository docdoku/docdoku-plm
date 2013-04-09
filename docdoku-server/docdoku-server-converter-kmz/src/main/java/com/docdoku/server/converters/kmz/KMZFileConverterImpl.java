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

package com.docdoku.server.converters.kmz;

import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.*;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.google.common.io.Files;

import javax.ejb.EJB;
import java.io.*;
import java.util.Properties;

import javax.xml.stream.*;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;


@KMZFileConverter
public class KMZFileConverterImpl implements CADConverter{

    @EJB
    private IProductManagerLocal productService;

    @Override
    public File convert(PartIteration partToConvert, File cadFile) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, XMLStreamException {
        File tempDir = Files.createTempDir();
        FileIO.unzipArchive(cadFile, tempDir);
        File tmpDAEFile= new File(tempDir, "models/untitled.dae");

        File tmpTexturesDir = new File(tempDir,"models/untitled");

        File daeFile = null;
        File tmpNewDAEFile = null;
        try {
            tmpNewDAEFile = parseDAEFile(tmpDAEFile, tempDir);
            PartIterationKey partIPK = partToConvert.getKey();

            // Upload each textures
            if(tmpTexturesDir.isDirectory()) {
                for (File tempTexture : tmpTexturesDir.listFiles()) {
                    if (!tempTexture.isDirectory()) {
                        File finalTexture = productService.saveFileInPartIteration(partIPK, tempTexture.getName(), tempTexture.length());
                        Files.copy(tempTexture,finalTexture);
                    }
                }
            }

            // Upload dae
            daeFile = productService.saveGeometryInPartIteration(partIPK, tmpNewDAEFile.getName(), 0, tmpNewDAEFile.length());
            Files.copy(tmpNewDAEFile,daeFile);
            return daeFile;
        }
        finally {
            if(tmpDAEFile!=null)
                tmpDAEFile.delete();

            if(tmpNewDAEFile!=null)
                tmpNewDAEFile.delete();

            if(tmpTexturesDir!=null)
                FileIO.rmDir(tmpTexturesDir);

            if(tempDir!=null)
                FileIO.rmDir(tempDir);
        }
    }

    @Override
    public boolean canConvertToJSON(String cadFileExtension) {
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
