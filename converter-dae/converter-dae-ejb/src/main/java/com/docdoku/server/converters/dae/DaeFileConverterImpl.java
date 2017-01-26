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

package com.docdoku.server.converters.dae;

import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.ConversionResult;
import com.docdoku.server.converters.ConverterUtils;

import javax.ejb.Stateless;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@DaeFileConverter
@Stateless
public class DaeFileConverterImpl implements CADConverter {

    static final String CONF_PROPERTIES = "/com/docdoku/server/converters/dae/conf.properties";
    static final Properties CONF = new Properties();
    static final Logger LOGGER = Logger.getLogger(DaeFileConverterImpl.class.getName());

    static {
        try (InputStream inputStream = DaeFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES)) {
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

        String assimp = CONF.getProperty("assimp");
        Path executable = Paths.get(assimp);

        // Sanity checks

        if (!Files.exists(executable)) {
            throw new ConversionException(
                    "Cannot convert file \"" + tmpCadFile.toString() + "\", \"" + assimp + "\" is not available");
        }

        if (!Files.isExecutable(executable)) {
            throw new ConversionException(
                    "Cannot convert file \"" + tmpCadFile.toString() + "\", \"" + assimp + "\" has no execution rights");
        }

        UUID uuid = UUID.randomUUID();
        Path convertedFile = tmpDir.resolve(uuid + ".obj");
        Path convertedMtlFile = tmpDir.resolve(uuid + ".obj.mtl");

        String[] args = {assimp, "export", tmpCadFile.toAbsolutePath().toString(), convertedFile.toString()};
        ProcessBuilder pb = new ProcessBuilder(args);
        try {
            Process process = pb.start();

            // Read buffers
            String stdOutput = ConverterUtils.inputStreamToString(process.getInputStream());
            String errorOutput = ConverterUtils.inputStreamToString(process.getErrorStream());

            LOGGER.info(stdOutput);

            process.waitFor();

            if (process.exitValue() == 0) {
                List<Path> materials = new ArrayList<>();
                materials.add(convertedMtlFile);
                return new ConversionResult(convertedFile, materials);
            } else {
                throw new ConversionException(
                        "Cannot convert to obj " + tmpCadFile.toAbsolutePath() + ": " + errorOutput);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new ConversionException(e);
        }
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("dxf", "dae", "lwo", "x", "ac", "cob", "scn", "ms3d").contains(cadFileExtension);
    }

}
