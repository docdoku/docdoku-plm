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
import com.docdoku.api.client.ApiResponse;
import com.docdoku.api.client.Pair;
import com.docdoku.api.models.BinaryResourceDTO;
import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.PartIterationDTO;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * This class helps to upload and download files on documents and parts
 *
 * @author Morgan Guimard
 * @implNote : This is a temporary class and will be removed as soon as fix generated upload and download
 * methods with swagger-codegen
 */
public class UploadDownloadHelper {

    private static final int CHUNK_SIZE = 1024 * 8;

    /**
     * Upload an attached file to a {@link com.docdoku.core.product.PartIteration}
     *
     * @param partIteration: the part iteration to attach the file
     * @param client:        the client to use for authentication
     * @param file:          the file to upload
     */
    public static void uploadAttachedFile(PartIterationDTO partIteration, ApiClient client, File file) throws ApiException {
        String path = "/files/{workspaceId}/parts/{number}/{version}/{iteration}/attachedfiles".replaceAll("\\{format\\}", "json")
                .replaceAll("\\{" + "workspaceId" + "\\}", client.escapeString(partIteration.getWorkspaceId()))
                .replaceAll("\\{" + "number" + "\\}", client.escapeString(partIteration.getNumber()))
                .replaceAll("\\{" + "version" + "\\}", client.escapeString(partIteration.getVersion()))
                .replaceAll("\\{" + "iteration" + "\\}", String.valueOf(partIteration.getIteration()));

        upload(path, file, client);
    }

    /**
     * Upload an attached file to a {@link com.docdoku.core.document.DocumentIteration}
     *
     * @param documentIteration: the document iteration to attach the file
     * @param client:            the client to use for authentication
     * @param file:              the file to upload
     */
    public static void uploadAttachedFile(DocumentIterationDTO documentIteration, ApiClient client, File file) throws ApiException {

        String path = "/files/{workspaceId}/documents/{documentId}/{version}/{iteration}".replaceAll("\\{format\\}", "json")
                .replaceAll("\\{" + "workspaceId" + "\\}", client.escapeString(documentIteration.getWorkspaceId()))
                .replaceAll("\\{" + "documentId" + "\\}", client.escapeString(documentIteration.getDocumentMasterId()))
                .replaceAll("\\{" + "version" + "\\}", client.escapeString(documentIteration.getVersion()))
                .replaceAll("\\{" + "iteration" + "\\}", String.valueOf(documentIteration.getIteration()));

        upload(path, file, client);

    }

    /**
     * Upload a native CAD file to a {@link com.docdoku.core.product.PartIteration}
     *
     * @param partIteration: the document iteration to attach the file
     * @param client:        the client to use for authentication
     * @param file:          the file to upload
     */
    public static void uploadNativeCADFile(PartIterationDTO partIteration, ApiClient client, File file) throws ApiException {
        String path = "/files/{workspaceId}/parts/{number}/{version}/{iteration}/nativecad".replaceAll("\\{format\\}", "json")
                .replaceAll("\\{" + "workspaceId" + "\\}", client.escapeString(partIteration.getWorkspaceId()))
                .replaceAll("\\{" + "number" + "\\}", client.escapeString(partIteration.getNumber()))
                .replaceAll("\\{" + "version" + "\\}", client.escapeString(partIteration.getVersion()))
                .replaceAll("\\{" + "iteration" + "\\}", String.valueOf(partIteration.getIteration()));

        upload(path, file, client);
    }

    /**
     * Download files from any entities
     *
     * @param binaryResource: the resource holding the file
     * @param client:   the client to use for authentication
     * @return the downloaded file
     */
    public static File downloadFile(BinaryResourceDTO binaryResource, ApiClient client) throws ApiException {
      return downloadFile(binaryResource.getFullName(),client);
    }

    /**
     * Download files from any entities
     *
     * @param fileName: the file full name
     * @param client:   the client to use for authentication
     * @return the downloaded file
     */
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
        headerParams.put("Content-Type", contentType);
        headerParams.put("Accept", accept);
        Type returnType = new TypeToken<File>(){}.getType();
        Call post = client.buildCall(path, "GET", queryParams, null, headerParams, formParams, authNames, null);
        ApiResponse<File> response = client.execute(post, returnType);
        Map<String, List<String>> responseHeaders = response.getHeaders();
        List<String> encoding = responseHeaders.get("Content-Encoding");

        return encoding != null && "gzip".equals(encoding.get(0)) ? uncompress(response.getData()) : response.getData();

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
        headerParams.put("Content-Type", contentType);
        headerParams.put("Accept", accept);
        String[] authNames = new String[]{};
        Call post = client.buildCall(path, "POST", queryParams, file, headerParams, formParams, authNames, null);
        client.execute(post);
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
