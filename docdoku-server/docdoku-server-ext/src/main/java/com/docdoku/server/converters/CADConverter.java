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

import java.net.URI;

import javax.ejb.Remote;

/**
 * CADConverter Extension point interface for 3D files conversion.
 * 
 * Converters are supposed to be (standalone) remote EJB module that can be
 * deployed independently of DocdokuPLM application.
 */
@Remote
public interface CADConverter {

    /**
     * Exception reporting a unrecoverable problem during conversion process.
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
     * Convert the given CAD file to Wavefront OBJ format
     *
     * @param cadFileName
     *            the CAD file to convert
     * @param tempDir
     *            a given temporary directory for converter operations
     * @return the conversion result
     * @throws ConversionException
     * 
     */
    ConversionResult convert(URI cadFileName, URI tempDir)
	    throws ConversionException;

    /**
     * Determine if this converter is able to convert given CAD file format
     * (identified by it's extension) to Wavefront OBJ format
     *
     * @param cadFileExtension
     *            the extension of the cadFile
     * @return true if the converter can handle the conversion, false otherwise
     */
    boolean canConvertToOBJ(String cadFileExtension);
}
