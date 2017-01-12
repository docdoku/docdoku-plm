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

package com.docdoku.server.converters.obj;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.PartIteration;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.ConversionResult;

@ObjFileConverter
@Stateless
public class ObjFileConverterImpl implements CADConverter {

    private static final Logger LOGGER = Logger.getLogger(ObjFileConverterImpl.class.getName());

    @Override
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, Path tempDir)
	    throws ConversionException {

	Path tmpCadFile = tempDir.resolve(cadFile + ".obj");

	return new ConversionResult(tmpCadFile);
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
	return Arrays.asList("obj").contains(cadFileExtension);
    }

}