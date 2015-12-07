/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.server.rest.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Elisabel Généreux
 */
public class FileDownloadTools {

    private static final Logger LOGGER = Logger.getLogger(FileDownloadTools.class.getName());
    private static final String CHARSET = "UTF-8";

    /**
     * Get the output name of file
     * @return Output name of file
     */
    public static String getFileName(String fullName, String outputFormat) {
        String fileName = fullName;

        try {
            fileName = URLEncoder.encode(fileName, CHARSET).replace("+", " ");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING,null,e);
        }

        if (outputFormat != null) {
            fileName += "."+ outputFormat;
        }

        return fileName;
    }

    /**
     * Get the Content disposition for this file
     * @return Http Response content disposition
     */
    // Todo check if we can have unencoding contentDisposition
    // Todo check accept request
    public static String getContentDisposition(String downloadType, String fileName) {
        String dispositionType = ("viewer".equals(downloadType)) ? "inline" : "attachement";
        return dispositionType+"; filename=\""+ fileName +"\" ; filename*=\""+ fileName +"\"";
    }

}
