package com.docdoku.core.services;

import com.docdoku.core.common.BinaryResource;

public interface IDocumentViewerManagerLocal {
    String getHtmlForViewer(BinaryResource file, String uuid);
}
