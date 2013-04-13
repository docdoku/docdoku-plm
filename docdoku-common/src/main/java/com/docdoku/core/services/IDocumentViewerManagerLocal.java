package com.docdoku.core.services;

import com.docdoku.core.common.BinaryResource;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

public interface IDocumentViewerManagerLocal {
    InputStream prepareFileForViewer(HttpServletRequest pRequest, HttpServletResponse pResponse, ServletContext servletContext, BinaryResource dataFile) throws Exception;
    String getHtmlForViewer(BinaryResource file);
}
