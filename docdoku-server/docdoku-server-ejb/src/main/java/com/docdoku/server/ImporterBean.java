package com.docdoku.server;


import com.docdoku.core.product.ImportResult;
import com.docdoku.core.services.IImporterManagerLocal;
import com.docdoku.server.importers.PartImporter;
import com.docdoku.server.importers.PathDataImporter;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Attributes importer
 *
 * @author Elisabel Généreux
 */
@Stateless(name = "ImporterBean")
public class ImporterBean implements IImporterManagerLocal {



    private static final Logger LOGGER = Logger.getLogger(ImporterBean.class.getName());

    @Inject
    @Any
    private Instance<PartImporter> partImporters;

    @Inject
    @Any
    private Instance<PathDataImporter> pathDataImporters;

    @Override
    @Asynchronous
    @FileImport
    public Future<ImportResult> importIntoParts(String workspaceId, File file, String originalFileName, String revisionNote, boolean autoCheckout, boolean autoCheckin, boolean permissiveUpdate) throws Exception{
        PartImporter selectedImporter = null;

        for (PartImporter importer : partImporters) {
            if (importer.canImportFile(file.getName())) {
                selectedImporter = importer;
                break;
            }
        }

        ImportResult result;

        if (selectedImporter != null) {
            result = selectedImporter.importFile(workspaceId, file, revisionNote, autoCheckout, autoCheckin, permissiveUpdate);
        } else {
            result = getNoImporterAvailableError(file, originalFileName);
        }

        return new AsyncResult<>(result);
    }

    @Override
    @Asynchronous
    @FileImport
    public Future<ImportResult> importIntoPathData(String workspaceId, File file, String originalFileName, String revisionNote, boolean autoFreezeAfterUpdate, boolean permissiveUpdate) throws Exception{
        PathDataImporter selectedImporter = null;

        for (PathDataImporter importer : pathDataImporters) {
            if (importer.canImportFile(file.getName())) {
                selectedImporter = importer;
                break;
            }
        }

        ImportResult result;

        if (selectedImporter != null) {
            result = selectedImporter.importFile(workspaceId, file, revisionNote, autoFreezeAfterUpdate, permissiveUpdate);
        } else {
            result = getNoImporterAvailableError(file, originalFileName);
        }

        return new AsyncResult<>(result);
    }

    public ImportResult getNoImporterAvailableError(File file, String fileName) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        errors.add("No importer available");
        ImportResult result = new ImportResult(file, fileName, warnings, errors);
        return result;
    }
}
