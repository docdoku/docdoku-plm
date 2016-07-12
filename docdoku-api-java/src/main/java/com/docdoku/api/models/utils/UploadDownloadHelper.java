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

package com.docdoku.api.models.utils;

import com.docdoku.api.client.ApiClient;
import com.docdoku.api.client.ApiException;
import com.docdoku.api.client.Pair;
import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.PartIterationDTO;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * This class helps to upload and download files on documents and parts
 *
 * @implNote : This is a temporary class and will be removed as soon as fix generated upload and download
 *             methods with swagger-codegen
 *
 * @author Morgan Guimard
 */
public class UploadDownloadHelper {

    private static final int CHUNK_SIZE = 1024 * 8;

    public static void uploadAttachedFile(PartIterationDTO partIterationDTO, ApiClient client, File file) throws ApiException {
        String path = "/files/{workspaceId}/parts/{number}/{version}/{iteration}/attachedfiles".replaceAll("\\{format\\}", "json")
                .replaceAll("\\{" + "workspaceId" + "\\}", client.escapeString(partIterationDTO.getWorkspaceId()))
                .replaceAll("\\{" + "number" + "\\}", client.escapeString(partIterationDTO.getNumber()))
                .replaceAll("\\{" + "version" + "\\}", client.escapeString(partIterationDTO.getVersion()))
                .replaceAll("\\{" + "iteration" + "\\}", String.valueOf(partIterationDTO.getIteration()));

        upload(path, file, client);
    }

    public static void uploadAttachedFile(DocumentIterationDTO documentIterationDTO, ApiClient client, File file) throws ApiException {

        String path = "/files/{workspaceId}/documents/{documentId}/{version}/{iteration}".replaceAll("\\{format\\}", "json")
                .replaceAll("\\{" + "workspaceId" + "\\}", client.escapeString(documentIterationDTO.getWorkspaceId()))
                .replaceAll("\\{" + "documentId" + "\\}", client.escapeString(documentIterationDTO.getDocumentMasterId()))
                .replaceAll("\\{" + "version" + "\\}", client.escapeString(documentIterationDTO.getVersion()))
                .replaceAll("\\{" + "iteration" + "\\}", String.valueOf(documentIterationDTO.getIteration()));

        upload(path,file,client);

    }

    public static void uploadNativeCADFile(PartIterationDTO partIterationDTO, ApiClient client, File file) throws ApiException {
        String path = "/files/{workspaceId}/parts/{number}/{version}/{iteration}/nativecad".replaceAll("\\{format\\}", "json")
                .replaceAll("\\{" + "workspaceId" + "\\}", client.escapeString(partIterationDTO.getWorkspaceId()))
                .replaceAll("\\{" + "number" + "\\}", client.escapeString(partIterationDTO.getNumber()))
                .replaceAll("\\{" + "version" + "\\}", client.escapeString(partIterationDTO.getVersion()))
                .replaceAll("\\{" + "iteration" + "\\}", String.valueOf(partIterationDTO.getIteration()));

        upload(path,file,client);
    }

    private static void upload(String path, File file, ApiClient client) throws ApiException {
        List<Pair> queryParams = new ArrayList<Pair>();
        Map<String, String> headerParams = new HashMap<String, String>();
        Map<String, Object> formParams = new HashMap<String, Object>();
        formParams.put("upload", file);
        final String[] accepts = {};
        final String accept = client.selectHeaderAccept(accepts);
        final String[] contentTypes = {MediaType.MULTIPART_FORM_DATA};
        final String contentType = client.selectHeaderContentType(contentTypes);
        String[] authNames = new String[]{};
        client.invokeAPI(path, "POST", queryParams, file, headerParams, formParams, accept, contentType, authNames, null);
    }

    public static File downloadFile(String fileName, ApiClient client) throws ApiException {
        String path = "/files/" + fileName;
        List<Pair> queryParams = new ArrayList<Pair>();
        Map<String, String> headerParams = new HashMap<String, String>();
        Map<String, Object> formParams = new HashMap<String, Object>();
        final String[] accepts = {"application/octet-stream"};
        final String accept = client.selectHeaderAccept(accepts);
        final String[] contentTypes = {};
        final String contentType = client.selectHeaderContentType(contentTypes);
        String[] authNames = new String[]{};
        GenericType<File> returnType = new GenericType<File>() {
        };
        File file = client.invokeAPI(path, "GET", queryParams, null, headerParams, formParams, accept, contentType, authNames, returnType);
        return uncompress(file);
    }


    private static File uncompress(File file) {
        File out = new File(file.getAbsolutePath() + ".unzip");
        int length;
        byte[] data = new byte[CHUNK_SIZE];
        try (GZIPInputStream is = new GZIPInputStream(new FileInputStream(file)); OutputStream os = new FileOutputStream(out)) {
            while ((length = is.read(data)) != -1) {
                os.write(data, 0, length);
            }
            os.flush();
            return out;
        } catch (IOException e) {
            return file;
        }
    }

}
