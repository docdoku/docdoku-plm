package com.docdoku.core.services;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by elisabelgenereux on 11/02/16.
 */
public interface IImporterManagerLocal {

    Future<Map<String, List<String>>> importPartAttributes(String workspaceId, File file, String revisionNote, boolean autoCheckout, boolean autoCheckin, boolean permissiveUpdate);
    Future<Map<String, List<String>>> importPathDataAttributes(String workspaceId, File file, String revisionNote, boolean autoFreezeAfterUpdate, boolean permissiveUpdate);

}
