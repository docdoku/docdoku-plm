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

package com.docdoku.server.converters.catia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

@CatiaFileConverter
@Stateless
public class CatiaFileConverterImpl implements CADConverter {

    private static final String CONF_PROPERTIES = "/com/docdoku/server/converters/catia/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(CatiaFileConverterImpl.class.getName());

    static {
	try (InputStream inputStream = CatiaFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES)) {
	    CONF.load(inputStream);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, null, e);
	}
    }

    @Override
    public ConversionResult convert(PartIteration partToConvert, final BinaryResource cadFile, Path tempDir)
	    throws ConversionException {

	String catPartConverter = CONF.getProperty("catPartConverter");
	Path executable = Paths.get(catPartConverter);

	// Sanity checks
	if (!Files.exists(executable)) {
	    throw new ConversionException(
		    "Cannot convert file \"" + cadFile.getName() + "\", \"" + catPartConverter + "\" is not available");
	}

	if (!Files.isExecutable(executable)) {
	    throw new ConversionException("Cannot convert file \"" + cadFile.getName() + "\", \"" + catPartConverter
		    + "\" has no execution rights");
	}

	UUID uuid = UUID.randomUUID();
	Path tmpCadFile = tempDir.resolve(cadFile.getName());
	Path tmpDAEFile = tempDir.resolve(uuid + ".dae");

	String[] args = { "sh", catPartConverter, tmpCadFile.toAbsolutePath().toString(),
		tmpDAEFile.toAbsolutePath().toString() };

	ProcessBuilder pb = new ProcessBuilder(args);
	try {
	    Process process1 = pb.start();

	    // Read buffers
	    String stdOutput1 = ConverterUtils.getOutput(process1.getInputStream());
	    String errorOutput1 = ConverterUtils.getOutput(process1.getErrorStream());

	    LOGGER.info(stdOutput1);

	    process1.waitFor();

	    // Convert to OBJ once converted to DAE
	    if (process1.exitValue() == 0 && Files.exists(tmpDAEFile) && Files.size(tmpDAEFile) > 0) {
		String assimp = CONF.getProperty("assimp");
		Path convertedFileName = tempDir.resolve(uuid + ".obj");
		String[] argsOBJ = { assimp, "export", tmpDAEFile.toAbsolutePath().toString(),
			convertedFileName.toString() };
		pb = new ProcessBuilder(argsOBJ);
		Process process2 = pb.start();

		// Read buffers
		String stdOutput2 = ConverterUtils.getOutput(process2.getInputStream());
		String errorOutput2 = ConverterUtils.getOutput(process2.getErrorStream());

		LOGGER.info(stdOutput2);

		process2.waitFor();

		if (process2.exitValue() == 0) {
		    return new ConversionResult(convertedFileName);
		} else {
		    throw new ConversionException(
			    "Cannot convert to obj : " + tmpCadFile.toAbsolutePath() + ": " + errorOutput2);
		}
	    } else {
		throw new ConversionException(
			"Cannot convert to dae : " + tmpCadFile.toAbsolutePath() + ": " + errorOutput1);
	    }
	} catch (IOException | InterruptedException e) {
	    assert (false);
	    throw new ConversionException(e);
	}
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
	return Arrays.asList("catpart").contains(cadFileExtension);
    }
}
