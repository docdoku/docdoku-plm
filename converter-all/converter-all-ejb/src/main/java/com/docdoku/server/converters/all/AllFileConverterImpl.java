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

package com.docdoku.server.converters.all;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.ConversionResult;
import com.docdoku.server.converters.ConverterUtils;

@AllFileConverter
@Stateless
public class AllFileConverterImpl implements CADConverter {

    private static final String CONF_PROPERTIES = "/com/docdoku/server/converters/all/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(AllFileConverterImpl.class.getName());

    static {
	try (InputStream inputStream = AllFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES)) {
	    CONF.load(inputStream);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, null, e);
	}
    }

    @Override
    public ConversionResult convert(final URI cadFileUri, final URI tmpDirUri)
	    throws ConversionException {
	Path tmpDir = Paths.get(tmpDirUri);
	Path tmpCadFile = Paths.get(cadFileUri);
	
	String meshConvBinary = CONF.getProperty("meshconv_path");
	Path executable = Paths.get(meshConvBinary);

	// sanity checks
	if (!Files.exists(executable)) {
	    throw new ConversionException(
		    "Cannot convert file \"" + tmpCadFile.toString() + "\", \"" + meshConvBinary + "\" is not available");
	}

	if (!Files.isExecutable(executable)) {
	    throw new ConversionException("Cannot convert file \"" + tmpCadFile.toString() + "\", \"" + meshConvBinary
		    + "\" has no execution rights");
	}

	UUID uuid = UUID.randomUUID();
	Path convertedFile = tmpDir.resolve(uuid + ".obj");

	String[] args = { meshConvBinary, tmpCadFile.toAbsolutePath().toString(), "-c", "obj", "-o",
		convertedFile.toString() };
	ProcessBuilder pb = new ProcessBuilder(args);
	try {
	    Process proc = pb.start();

	    // Read buffers
	    String stdOutput = ConverterUtils.getOutput(proc.getInputStream());
	    String errorOutput = ConverterUtils.getOutput(proc.getErrorStream());

	    LOGGER.info(stdOutput);

	    proc.waitFor();

	    if (proc.exitValue() == 0) {
		return new ConversionResult(convertedFile);
	    } else {
		throw new ConversionException(
			"Cannot convert to obj " + tmpCadFile.toAbsolutePath() + ": " + errorOutput);
	    }
	} catch (IOException | InterruptedException e) {
	    assert (false);
	    throw new ConversionException(e);
	}
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
	return Arrays.asList("stl", "off", "ply", "3ds", "wrl").contains(cadFileExtension);
    }

}