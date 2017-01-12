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

package com.docdoku.server.converters;

import java.nio.file.Path;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.PartIteration;

/**
 * CADConverter plugin interface Extension point for 3D files conversion
 */

public interface CADConverter {

    /**
     * Exception reporting a problem during convertion process.
     */
    class ConversionException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConversionException(String message) {
	    super(message);
	}

	public ConversionException(Throwable cause) {
	    super(cause);
	}

	public ConversionException(String message, Throwable cause) {
	    super(message, cause);
	}
    }

    /**
     * Convert the given file to obj
     *
     * @param partToConvert
     *            the part iteration concerned
     * @param cadFile
     *            the 3D file to convert
     * @param tempDir
     *            a given temporary directory for plugin operations (soon
     *            deprecated)
     * @return the conversion result
     * @throws Exception
     *             Note: plugins should handle errors and add them in the
     *             ConversionResult object
     */
    ConversionResult convert(PartIteration partToConvert, BinaryResource cadFile, Path tempDir)
	    throws ConversionException;

    /**
     * Determine if plugin is able to convert given extension to obj files
     *
     * @param cadFileExtension
     *            the extension of the cadFile
     * @return true if plugin can handle the conversion, false otherwise
     */
    boolean canConvertToOBJ(String cadFileExtension);
}
