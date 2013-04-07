package com.docdoku.server.viewers;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.server.viewers.utils.ScormManifestParser;
import com.docdoku.server.viewers.utils.ScormOrganization;
import com.docdoku.server.viewers.utils.ScormUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class ScormViewerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        System.out.println("scorm com.docdoku.server.viewers biatch");

        BinaryResource scormResource = (BinaryResource) httpServletRequest.getAttribute("attachedFile");
        String vaultPath = (String) httpServletRequest.getAttribute("vaultPath");

        System.out.println("in scormviewer servlet vault path is " + vaultPath);

        try {
            ScormOrganization scormOrganization = new ScormManifestParser(ScormUtil.getManifest(scormResource.getFullName(), vaultPath)).parse();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }


}
