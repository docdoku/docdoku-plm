/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.cli.helpers;

import com.docdoku.api.client.ApiClient;
import com.docdoku.api.client.ApiException;
import com.docdoku.api.client.ApiResponse;
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.DocumentBinaryApi;
import com.docdoku.api.services.PartBinaryApi;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileHelper {

    private ApiClient client;
    private CliOutput output;
    private LangHelper langHelper;

    public FileHelper(ApiClient client, CliOutput output, LangHelper langHelper) {
        this.client = client;
        this.output = output;
        this.langHelper = langHelper;
    }

    public static boolean confirmOverwrite(String fileName) {
        Console c = System.console();
        String response = c.readLine("The file '" + fileName + "' has been modified locally, do you want to overwrite it [y/N]?");
        return "y".equalsIgnoreCase(response);
    }

    public File downloadPartFile(File path, String pWorkspace, String pPartNumber, String pVersion, int pIteration, String pFilename, String pType, String pSubType, boolean force) {
        PartBinaryApi partBinaryApi = new PartBinaryApi(client);
        File localFile = new File(path, pFilename);
        MetaDirectoryManager meta;
        try {
            meta = new MetaDirectoryManager(path);
            if (localFile.exists() && !force && localFile.lastModified() != meta.getLastModifiedDate(localFile.getAbsolutePath())) {
                boolean confirm = FileHelper.confirmOverwrite(localFile.getAbsolutePath());
                if (!confirm)
                    return null;
            }
            File result = partBinaryApi.downloadPartFile(pWorkspace, pPartNumber, pVersion, pIteration, pSubType, pFilename, pType, null, null, null, null, null);
            Files.move(result.toPath(), localFile.toPath(), REPLACE_EXISTING);
            output.printInfo(langHelper.getLocalizedMessage("DownloadindFileSuccess"));
            return localFile;
        } catch (ApiException | IOException e) {
            output.printInfo(langHelper.getLocalizedMessage("DownloadingFileFailure"));
            output.printException(e);
        }
        return null;
    }

    public boolean uploadPartFile(String pWorkspace, String pPartNumber, String pVersion, int pIteration, File pFile) {
        PartBinaryApi partBinaryApi = new PartBinaryApi(client);
        ApiResponse<Void> response;
        try {
            output.printInfo(
                    langHelper.getLocalizedMessage("UploadingFile")
                            + " : "
                            + pFile.getName());
            response = partBinaryApi.uploadNativeCADFileWithHttpInfo(pWorkspace, pPartNumber, pVersion, pIteration, pFile);
            if (response.getStatusCode() == 201) {
                output.printInfo(langHelper.getLocalizedMessage("UploadingFileSuccess"));
                return true;
            } else {
                output.printInfo(langHelper.getLocalizedMessage("UploadingFileFailed"));
            }
        } catch (ApiException e) {
            output.printInfo(langHelper.getLocalizedMessage("UploadingFileFailed"));
        }
        return false;
    }

    private void saveMetadata(MetaDirectoryManager meta, DocumentIterationDTO docIPK, String digest, File localFile) throws IOException {
        String filePath = localFile.getAbsolutePath();
        meta.setDigest(filePath, digest);
        meta.setDocumentId(filePath, docIPK.getDocumentMasterId());
        meta.setWorkspace(filePath, docIPK.getWorkspaceId());
        meta.setRevision(filePath, docIPK.getVersion());
        meta.setIteration(filePath, docIPK.getIteration());
        meta.setLastModifiedDate(filePath, localFile.lastModified());
    }

    public File downloadDocumentFile(String pWorkspace, String pId, String pVersion, int pIteration, String pFilename, String pType) {
        DocumentBinaryApi documentBinaryApi = new DocumentBinaryApi(client);
        try {
            output.printInfo(
                    langHelper.getLocalizedMessage("DownloadingFile")
                            + " : "
                            + pFilename);
            File result = documentBinaryApi.downloadDocumentFile(pWorkspace, pId, pVersion, pIteration, pFilename, pType, null, null, null, null, null);
            output.printInfo(langHelper.getLocalizedMessage("DownloadindFileSuccess"));
            return result;
        } catch (ApiException e) {
            output.printInfo(langHelper.getLocalizedMessage("DownloadingFileFailure"));
            output.printException(e);
        }
        return null;
    }

    public boolean uploadDocumentFile(String pWorkspace, String pId, String pVersion, int pIteration, File pFile) {
        DocumentBinaryApi documentBinaryApi = new DocumentBinaryApi(client);
        ApiResponse<Void> response;
        try {
            output.printInfo(
                    langHelper.getLocalizedMessage("UploadingFile")
                            + " : "
                            + pFile.getName());
            response = documentBinaryApi.uploadDocumentFilesWithHttpInfo(pWorkspace, pId, pVersion, pIteration, pFile);
            if (response.getStatusCode() == 201) {
                output.printInfo(langHelper.getLocalizedMessage("UploadingFileSuccess"));
                return true;
            } else {
                output.printInfo(langHelper.getLocalizedMessage("UploadingFileFailed"));
            }
        } catch (ApiException e) {
            output.printInfo(langHelper.getLocalizedMessage("UploadingFileFailed"));
        }
        return false;
    }

    public List<File> downloadDocumentFiles(File path, String user, String pWorkspace, String pId, DocumentRevisionDTO pDocumentRevision, DocumentIterationDTO pDocumentIteration, boolean force) throws IOException {
        List<File> files = new ArrayList<>();
        if(this.client == null) {
            throw new NullPointerException(langHelper.getLocalizedMessage("NullAPIClientException"));
        }
        String version = pDocumentRevision.getVersion();
        int iteration = pDocumentIteration.getIteration();
        UserDTO checkOutUser = pDocumentRevision.getCheckOutUser();
        DocumentIterationDTO lastIteration = LastIterationHelper.getLastIteration(pDocumentRevision);
        boolean writable = (checkOutUser != null) && (checkOutUser.getLogin().equals(user)) && (lastIteration.getIteration() == pDocumentIteration.getIteration());
        for(BinaryResourceDTO binaryResource : pDocumentIteration.getAttachedFiles()) {
            String fileName = binaryResource.getName();
            MetaDirectoryManager meta = new MetaDirectoryManager(path);

            File localFile = new File(path, fileName);
            if (localFile.exists() && !force && localFile.lastModified() != meta.getLastModifiedDate(localFile.getAbsolutePath())) {
                boolean confirm = FileHelper.confirmOverwrite(localFile.getAbsolutePath());
                if (!confirm)
                    continue;
            }

            File result = downloadDocumentFile(pWorkspace, pId, version, iteration, fileName, pDocumentRevision.getType());

            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(Files.readAllBytes(result.toPath()));
                byte[] digest = md.digest();
                String digestString = Base64.getEncoder().encodeToString(digest);

                result.setWritable(writable, false);

                DocumentIterationDTO docIPK = new DocumentIterationDTO();
                docIPK.setWorkspaceId(pWorkspace);
                docIPK.setDocumentMasterId(pId);
                docIPK.setVersion(version);
                docIPK.setIteration(iteration);
                saveMetadata(meta, docIPK, digestString, result);
                Files.move(result.toPath(), localFile.toPath(), REPLACE_EXISTING);
                files.add(localFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return files;
    }

    public static String getFileName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        int lastSlash = path.lastIndexOf('/');
        return path.substring(lastSlash + 1, path.length());
    }

}
