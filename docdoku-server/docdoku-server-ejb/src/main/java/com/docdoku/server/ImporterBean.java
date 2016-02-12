package com.docdoku.server;


import com.docdoku.core.services.IImporterManagerLocal;
import com.docdoku.server.importers.AttributesImporter;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Attributes importer
 *
 * @author Elisabel Généreux
 */
@Stateless(name = "ImporterBean")
public class ImporterBean implements IImporterManagerLocal {

    @Inject
    private AttributesImporter importer;

    @Override
    @Asynchronous
    @AttributesImport
    public Future<Map<String, List<String>>> importPartAttributes(String workspaceId, File file, String revisionNote, boolean autoCheckout, boolean autoCheckin, boolean permissiveUpdate) {
        // First check if provided data are ok
        Map<String, List<String>> importResult = importer.checkPartAttributesImport(workspaceId, file);
        // Then update the part iterations attributes
        if (importResult == null) {
            importer.savePartAttributes(workspaceId, file, revisionNote, autoCheckout, autoCheckin, permissiveUpdate);
        }

        return new Future<Map<String, List<String>>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Map<String, List<String>> get() throws InterruptedException, ExecutionException {
                return importResult;
            }

            @Override
            public Map<String, List<String>> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return importResult;
            }
        };
    }

    @Override
    @Asynchronous
    @AttributesImport
    public Future<Map<String, List<String>>> importPathDataAttributes(String workspaceId, File file, String revisionNote, boolean autoFreezeAfterUpdate, boolean permissiveUpdate) {
        // First check if provided data are ok
        Map<String, List<String>> importResult = importer.checkPathDataAttributesImport(workspaceId, file);
        // Then update the path data iterations attributes
        if (importResult == null) {
            importer.savePathDataAttributes(workspaceId, file, revisionNote, autoFreezeAfterUpdate, permissiveUpdate);
        }

        return new Future<Map<String, List<String>>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Map<String, List<String>> get() throws InterruptedException, ExecutionException {
                return importResult;
            }

            @Override
            public Map<String, List<String>> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return importResult;
            }
        };
    }

}
