package com.docdoku.core.services;

import com.docdoku.core.common.BinaryResource;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

public interface IDocumentViewerManagerLocal {
    File prepareFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, ServletContext servletContext, File dataFile) throws Exception;
    String getHtmlForViewer(BinaryResource file);
}
