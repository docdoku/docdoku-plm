package com.docdoku.server.filters;


import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.product.PartRevisionKey;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public class FilterUtils {

    public static PartRevisionKey getPartRevisionKey(HttpServletRequest pRequest) throws UnsupportedEncodingException {
        String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
        int offset = pRequest.getContextPath().equals("") ? 2 : 3;
        String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
        String partNumber = URLDecoder.decode(pathInfos[offset+1],"UTF-8");
        String partVersion = pathInfos[offset+2];
        return new PartRevisionKey(workspaceId,partNumber,partVersion);
    }

    public static DocumentMasterKey getDocumentMasterKey(HttpServletRequest pRequest) throws UnsupportedEncodingException {
        String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
        int offset = pRequest.getContextPath().equals("") ? 2 : 3;
        String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");
        String documentMasterId = URLDecoder.decode(pathInfos[offset+1],"UTF-8");
        String documentMasterVersion = pathInfos[offset+2];
        return new DocumentMasterKey(workspaceId,documentMasterId,documentMasterVersion);
    }
}
