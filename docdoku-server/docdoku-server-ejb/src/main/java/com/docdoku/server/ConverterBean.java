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
package com.docdoku.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.FileAlreadyExistsException;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotEnabledException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IBinaryStorageManagerLocal;
import com.docdoku.core.services.IConverterManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.CADConverter.ConversionException;
import com.docdoku.server.converters.ConversionResult;
import com.docdoku.server.converters.GeometryParser;

/**
 * CAD File converter
 *
 * @author Florent.Garin
 */
@Stateless(name = "ConverterBean")
public class ConverterBean implements IConverterManagerLocal {

    @PersistenceContext
    private EntityManager em;

    private List<CADConverter> converters = new ArrayList<>();

    @Inject
    private IProductManagerLocal productService;

    @Inject
    private IBinaryStorageManagerLocal storageManager;

    @Inject
    private BeanLocator beanLocator;

    private static final String CONF_PROPERTIES = "/com/docdoku/server/converters/utils/conf.properties";
    private static final Properties CONF = new Properties();
    private static final float[] RATIO = new float[] { 1f, 0.6f, 0.2f };

    private static final Logger LOGGER = Logger.getLogger(ConverterBean.class.getName());

    static {
	try (InputStream inputStream = ConverterBean.class.getResourceAsStream(CONF_PROPERTIES)) {
	    CONF.load(inputStream);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, null, e);
	}
    }

    @PostConstruct
    void init() {
	// add external converters
	converters.addAll(beanLocator.search(CADConverter.class));
    }

    @Override
    @Asynchronous
    @CADConvert
    public void convertCADFileToOBJ(PartIterationKey pPartIPK, BinaryResource cadBinaryResource) {

	CADConverter selectedConverter = null;

	// look for a CADConverter
	String ext = FileIO.getExtension(cadBinaryResource.getName());
	for (CADConverter converter : converters) {
	    if (converter.canConvertToOBJ(ext)) {
		selectedConverter = converter;
		break;
	    }
	}

	if (selectedConverter != null) {
	    try {

		// PartIterationDAO partIDAO = new PartIterationDAO(em);
		// PartIteration partI = partIDAO.loadPartI(pPartIPK);

		UUID uuid = UUID.randomUUID();
		Path tempDir = Files.createDirectory(Paths.get("docdoku-" + uuid));
		Path tmpCadFile = tempDir.resolve(cadBinaryResource.getName().trim());

		// copy resource content to temp directory
		try (InputStream in = storageManager.getBinaryResourceInputStream(cadBinaryResource)) {
		    Files.copy(in, tmpCadFile);

		    // convert file
		    try (ConversionResult conversionResult = selectedConverter.convert(tmpCadFile.toUri(),
			    tempDir.toUri())) {

			// manage converted file
			Path convertedFile = conversionResult.getConvertedFile();

			double[] box = GeometryParser.calculateBox(convertedFile.toFile());
			if (decimate(pPartIPK, convertedFile, tempDir, box, RATIO)) {
			    String fileName = convertedFile.getFileName().toString();
			    for (int i = 0; i < RATIO.length; i++) {
				Path geometryFile = tempDir.resolve(fileName.replaceAll("\\.obj$", "." + Math.round((RATIO[i] * 100)) + ".obj"));
				saveGeometryFile(pPartIPK, i, geometryFile, box);
			    }
			} else {
			    // Copy the converted file if decimation failed,
			    saveGeometryFile(pPartIPK, 0, convertedFile, box);
			}

			// manage materials
			for (Path material : conversionResult.getMaterials()) {
			    saveAttachedFile(pPartIPK, material);
			}
		    } catch (ConversionException | EJBException e) {
			LOGGER.log(Level.WARNING, "Cannot convert " + cadBinaryResource.getName(), e);
		    } finally {
			Files.list(tempDir).forEach((p) -> {
			    try {
				Files.delete(p);
			    } catch (IOException e) {
				LOGGER.warning("Unable to delete "+p.getFileName());
			    }
			});
		    }
		} finally {
		    Files.deleteIfExists(tempDir);
		}
		// } catch (PartIterationNotFoundException e) {
		// LOGGER.log(Level.WARNING, "PartIteration not found", e);
	    } catch (StorageException e) {
		LOGGER.log(Level.WARNING, "Unable to read from storage", e);
	    } catch (IOException e) {
		LOGGER.log(Level.WARNING, e.getMessage(), e);
	    }
	} else {
	    LOGGER.log(Level.WARNING, "No CAD converter able to handle " + cadBinaryResource.getName());
	}
    }

    private boolean decimate(PartIterationKey pPartIPK, Path file, Path tempDir, double[] box, float[] ratio) {

	// sanity checks
	String decimater = CONF.getProperty("decimater");
	Path executable = Paths.get(decimater);
	if (!Files.exists(executable)) {
	    LOGGER.log(Level.SEVERE, "Cannot decimate file \"" + file.getFileName() + "\", decimater \"" + decimater
		    + "\" is not available");
	    return false;
	}
	if (!Files.isExecutable(executable)) {
	    LOGGER.log(Level.SEVERE, "Cannot decimate file \"" + file.getFileName() + "\", decimater \"" + decimater
		    + "\" has no execution rights");
	    return false;
	}

	boolean decimateSucceed = false;

	try {
	    String[] args = { decimater, "-i", file.toAbsolutePath().toString(), "-o",
		    tempDir.toAbsolutePath().toString(), String.valueOf(ratio[0]), String.valueOf(ratio[1]),
		    String.valueOf(ratio[2]) };
	    ProcessBuilder pb = new ProcessBuilder(args);
	    Process proc = pb.start();

	    StringBuilder output = new StringBuilder();
	    // Read buffer
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF-8"))) {
		String line;
		while ((line = br.readLine()) != null) {
		    output.append(line).append("\n");
		}
	    }

	    proc.waitFor();

	    if (proc.exitValue() == 0) {
		LOGGER.log(Level.INFO, "decimation done");
		decimateSucceed = true;
	    } else {
		LOGGER.log(Level.SEVERE, "Decimation failed with code = " + proc.exitValue(), output.toString());
	    }

	} catch (IOException | InterruptedException e) {
	    LOGGER.log(Level.SEVERE, "Decimation failed for " + file.toAbsolutePath(), e);
	}
	return decimateSucceed;
    }

    private void saveGeometryFile(PartIterationKey partIPK, int quality, Path file, double[] box) {
	try {
	    Geometry lod = (Geometry) productService.saveGeometryInPartIteration(partIPK, file.getFileName().toString(),
		    quality, Files.size(file), box);
	    try (OutputStream os = storageManager.getBinaryResourceOutputStream(lod)) {
		Files.copy(file, os);
		LOGGER.log(Level.INFO, "geometry saved");
	    } catch (IOException e) {
		LOGGER.log(Level.SEVERE, e.getMessage(), e);
	    }
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Unable to get geometry file's size", e);
	} catch (UserNotFoundException | WorkspaceNotFoundException | WorkspaceNotEnabledException | CreationException
		| FileAlreadyExistsException | PartRevisionNotFoundException | NotAllowedException
		| UserNotActiveException | StorageException e) {
	    LOGGER.log(Level.SEVERE, "Cannot save geometry to part iteration", e);
	}
    }

    private void saveAttachedFile(PartIterationKey partIPK, Path file) {
	try {
	    BinaryResource binaryResource = productService.saveFileInPartIteration(partIPK,
		    file.getFileName().toString(), "attachedfiles", Files.size(file));
	    try (OutputStream os = storageManager.getBinaryResourceOutputStream(binaryResource)) {
		Files.copy(file, os);
		LOGGER.log(Level.INFO, "Attached file copied");
	    } catch (IOException e) {
		LOGGER.log(Level.SEVERE, "Unable to save attached file", e);
	    }
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Unable to get attached file's size", e);
	} catch (UserNotFoundException | WorkspaceNotFoundException | WorkspaceNotEnabledException | CreationException
		| FileAlreadyExistsException | PartRevisionNotFoundException | NotAllowedException
		| UserNotActiveException | StorageException e) {
	    LOGGER.log(Level.SEVERE, "Cannot save attached file to part iteration", e);
	}
    }
}