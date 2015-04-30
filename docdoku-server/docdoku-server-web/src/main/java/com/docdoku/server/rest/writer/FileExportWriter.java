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
package com.docdoku.server.rest.writer;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.util.FileExportEntity;
import com.docdoku.server.rest.util.InstanceBodyWriterTools;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Provider
public class FileExportWriter implements MessageBodyWriter<FileExportEntity> {

    private static Context context;
    private static IProductManagerLocal productService;
    private static IDataManagerLocal dataManager;
    private static final Logger LOGGER = Logger.getLogger(InstanceBodyWriterTools.class.getName());
    private static Mapper mapper;

    static {
        try {
            context = new InitialContext();
            productService = (IProductManagerLocal) context.lookup("java:global/docdoku-server-ear/docdoku-server-ejb/ProductManagerBean");
            dataManager = (IDataManagerLocal) context.lookup("java:global/docdoku-server-ear/docdoku-server-ejb/DataManagerBean");
            mapper = DozerBeanMapperSingletonWrapper.getInstance();
        } catch (NamingException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(FileExportEntity.class);
    }

    @Override
    public long getSize(FileExportEntity fileExportEntity, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(FileExportEntity fileExportEntity, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {

        // create a zip file
        // find all part iteration
        // for each part iteration => create a folder and store part files (pull links)
        // if context provided (serial number => put attached files in zip)
        // serve the response with file name and application/download content type

        ZipOutputStream zs = new ZipOutputStream(outputStream);

        try {
            Map<String, Set<BinaryResource>> binariesInTree = productService.getBinariesInTree(fileExportEntity.getConfigurationItemKey().getWorkspace(),fileExportEntity.getConfigurationItemKey(),fileExportEntity.getPsFilter());
            Set<Map.Entry<String, Set<BinaryResource>>> entries = binariesInTree.entrySet();
            for(Map.Entry<String, Set<BinaryResource>> entry:entries){
                String folderName = entry.getKey();
                Set<BinaryResource> files = entry.getValue();

                ZipEntry zipEntry = new ZipEntry(folderName);
                zs.putNextEntry(zipEntry);
                for(BinaryResource binaryResource:files){
                    try {
                        addToZipFile(binaryResource,zs);
                    } catch (StorageException e) {
                        e.printStackTrace();
                    }
                }
                zs.closeEntry();

            }

        } catch (UserNotFoundException e) {
            e.printStackTrace();
        } catch (UserNotActiveException e) {
            e.printStackTrace();
        } catch (WorkspaceNotFoundException e) {
            e.printStackTrace();
        } catch (ConfigurationItemNotFoundException e) {
            e.printStackTrace();
        } catch (NotAllowedException e) {
            e.printStackTrace();
        } catch (EntityConstraintException e) {
            e.printStackTrace();
        } catch (PartMasterNotFoundException e) {
            e.printStackTrace();
        }

        zs.close();


    }

    public static void addToZipFile(BinaryResource binaryResource, ZipOutputStream zos) throws IOException, StorageException {

        InputStream binaryResourceInputStream = dataManager.getBinaryResourceInputStream(binaryResource);

        ZipEntry zipEntry = new ZipEntry(binaryResource.getName());
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = binaryResourceInputStream.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        binaryResourceInputStream.close();
    }

}
