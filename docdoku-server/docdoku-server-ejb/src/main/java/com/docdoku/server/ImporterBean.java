package com.docdoku.server;


import com.docdoku.core.services.IImporterManagerLocal;
import com.docdoku.server.importers.PartImporter;
import com.docdoku.server.importers.PathDataImporter;
import com.docdoku.server.importers.utils.ImportResult;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.File;
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
    public void importIntoParts(String workspaceId, File file, String revisionNote, boolean autoCheckout, boolean autoCheckin, boolean permissiveUpdate) throws Exception{
        PartImporter selectedImporter = null;

        for (PartImporter importer : partImporters) {
            if (importer.canImportFile(file.getName())) {
                selectedImporter = importer;
                break;
            }
        }

        if (selectedImporter != null) {
            ImportResult result = selectedImporter.importFile(workspaceId, file, revisionNote, autoCheckout, autoCheckin, permissiveUpdate);
        }

    }

    @Override
    @Asynchronous
    public void importIntoPathData(String workspaceId, File file, String revisionNote, boolean autoFreezeAfterUpdate, boolean permissiveUpdate) throws Exception{
        PathDataImporter selectedImporter = null;

        for (PathDataImporter importer : pathDataImporters) {
            if (importer.canImportFile(file.getName())) {
                selectedImporter = importer;
                break;
            }
        }

        if (selectedImporter != null) {
            ImportResult result = selectedImporter.importFile(workspaceId, file, revisionNote, autoFreezeAfterUpdate, permissiveUpdate);

        }
    }

}
