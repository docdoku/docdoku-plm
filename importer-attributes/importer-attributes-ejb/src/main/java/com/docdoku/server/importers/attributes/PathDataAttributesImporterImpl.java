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

package com.docdoku.server.importers.attributes;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.polarsys.eplmp.core.util.FileIO;
import org.polarsys.eplmp.i18n.PropertiesLoader;
import org.polarsys.eplmp.server.importers.AttributesImporterUtils;
import org.polarsys.eplmp.server.importers.PathDataImporter;
import org.polarsys.eplmp.server.importers.PathDataImporterResult;
import org.polarsys.eplmp.server.importers.PathDataToImport;

import javax.ejb.Stateless;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that imports attribute modification on attribute Path Data from an Excel File.
 *
 * @author Laurent Le Van
 * @version 1.0.0
 * @since 12/02/16.
 */

@PathDataAttributesImporter
@Stateless
public class PathDataAttributesImporterImpl implements PathDataImporter {

    private static final String[] EXTENSIONS = {"xls"};
    private static final Logger LOGGER = Logger.getLogger(PathDataAttributesImporterImpl.class.getName());

    private static final String I18N_CONF = "/com/docdoku/server/importers/attributes/ExcelImport";

    private Properties properties;

    /**
     * Checks if valid extension
     *
     * @param importFileName name of the file we want to import
     * @return true if good extension, false else
     */
    @Override
    public boolean canImportFile(String importFileName) {
        String ext = FileIO.getExtension(importFileName);
        return Arrays.asList(EXTENSIONS).contains(ext);
    }

    /**
     * This method import data of a file with different options
     *
     * @param workspaceId      Workspace in which we work
     * @param file             file containing data we want to update
     * @param autoFreeze       autofreeze after modification
     * @param permissiveUpdate boolean to indicate if allow or not permissive update
     * @return an ImportResult object containing file, and warnings, errors
     */
    @Override
    public PathDataImporterResult importFile(Locale locale, String workspaceId, File file, boolean autoFreeze, boolean permissiveUpdate) {

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        properties = PropertiesLoader.loadLocalizedProperties(locale, I18N_CONF, PartAttributesImporterImpl.class);

        Map<String, PathDataToImport> result = new HashMap<>();

        try {
            ExcelParser excelParser = new ExcelParser(file, locale);
            List<String> checkFileErrors = excelParser.checkFile();
            errors.addAll(checkFileErrors);
            result = excelParser.importPathData();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            errors.add(AttributesImporterUtils.createError(properties, "InternalError", "IOException"));
        } catch (InvalidFormatException e) {
            LOGGER.log(Level.SEVERE, null, e);
            errors.add(AttributesImporterUtils.createError(properties, "InvalidFormatException"));
        } catch (WrongCellCommentException e) {
            errors.add(AttributesImporterUtils.createError(properties, "WrongCellCommentException"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            errors.add(AttributesImporterUtils.createError(properties, "InternalError", e.toString()));
        }

        if (!errors.isEmpty()) {
            return new PathDataImporterResult(file, warnings, errors, null, null, null);
        }

        return new PathDataImporterResult(file, warnings, errors, null, null, result);
    }

}

