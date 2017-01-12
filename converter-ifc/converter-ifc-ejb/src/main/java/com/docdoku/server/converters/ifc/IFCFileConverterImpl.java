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

package com.docdoku.server.converters.ifc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.PartIteration;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.ConversionResult;
import com.docdoku.server.converters.ConverterUtils;

@IFCFileConverter
@Stateless
public class IFCFileConverterImpl implements CADConverter {

    private static final String CONF_PROPERTIES = "/com/docdoku/server/converters/ifc/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(IFCFileConverterImpl.class.getName());

    static {
	try (InputStream inputStream = IFCFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES)) {
	    CONF.load(inputStream);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, null, e);
	}
    }

    @Override
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, Path tempDir)
	    throws ConversionException {

	String ifcConverter = CONF.getProperty("ifc_convert_path");
	Path executable = Paths.get(ifcConverter);

	// Sanity checks

	if (!Files.exists(executable)) {
	    throw new ConversionException(
		    "Cannot convert file \"" + cadFile.getName() + "\", \"" + ifcConverter + "\" is not available");
	}

	if (!Files.isExecutable(executable)) {
	    throw new ConversionException("Cannot convert file \"" + cadFile.getName() + "\", \"" + ifcConverter
		    + "\" has no execution rights");
	}

	UUID uuid = UUID.randomUUID();
	// String extension = FileIO.getExtension(cadFile.getName());

	Path tmpCadFile = tempDir.resolve(cadFile.getName());
	Path convertedFile = tempDir.resolve(uuid + ".obj");
	Path convertedMtl = tempDir.resolve(uuid + ".mtl");

	String[] args = { ifcConverter, "--sew-shells", tmpCadFile.toAbsolutePath().toString(),
		convertedFile.toString() };
	ProcessBuilder pb = new ProcessBuilder(args);

	try {
	    Process process = pb.start();

	    // Read buffers
	    String stdOutput = ConverterUtils.getOutput(process.getInputStream());
	    String errorOutput = ConverterUtils.getOutput(process.getErrorStream());

	    LOGGER.info(stdOutput);

	    process.waitFor();

	    if (process.exitValue() == 0) {
		List<Path> materials = new ArrayList<>();
		materials.add(convertedMtl);
		return new ConversionResult(convertedFile, materials);
	    } else {
		throw new ConversionException(
			"Cannot convert to obj " + tmpCadFile.toAbsolutePath() + ": " + errorOutput);
	    }
	} catch (IOException | InterruptedException e) {
	    throw new ConversionException(e);
	}
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
	return Arrays.asList("ifc").contains(cadFileExtension);
    }

}