/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.core.product;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Value object that wraps information about the result of an import.
 *
 * Instances of this class are not persisted.
 *
 * @author Laurent Le Van
 *
 * @version 2.5, 29/06/2016
 * @since   V2.5
 */
public class ImportResult implements Serializable {

    private File importedFile;
    private List<String> warnings = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private String stdOutput;
    private String errorOutput;


    public ImportResult(File importedFile, List<String> warnings, List<String> errors) {
        this.importedFile = importedFile;
        this.warnings = warnings;
        this.errors = errors;
    }


    public File getImportedFile() {
        return importedFile;
    }

    public void setImportedFile(File importedFile) {
        this.importedFile = importedFile;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getStdOutput() {
        return stdOutput;
    }

    public void setStdOutput(String stdOutput) {
        this.stdOutput = stdOutput;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
    }

    public boolean isSucceed() {
        return errors == null || errors.isEmpty();
    }

}
