package com.docdoku.server.converters.dae;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.ConversionResult;

@RunWith(MockitoJUnitRunner.class)
public class DaeFileConverterImplTest {

    @Mock
    BinaryResource cadFile;

    DaeFileConverterImpl converter = new DaeFileConverterImpl();

    @Test
    public void testCanConvertToObj() throws Exception {
	Assert.assertTrue(converter.canConvertToOBJ("dae"));
	Assert.assertTrue(converter.canConvertToOBJ("dxf"));
	Assert.assertTrue(converter.canConvertToOBJ("lwo"));
	Assert.assertFalse(converter.canConvertToOBJ("toto"));
    }

    @Test
    public void testNominalConvert() throws Exception {
	// Assume assimp is installed
	Path assimp = Paths.get(DaeFileConverterImpl.CONF.getProperty("assimp"));
	Assume.assumeTrue(Files.exists(assimp) && Files.isExecutable(assimp));

	// setup
	Path tempDir = Paths.get("src/test/resources");
	Path daeFile = tempDir.resolve("good/good.dae");
	Mockito.when(cadFile.getName()).thenReturn(daeFile.toAbsolutePath().toString());

	ConversionResult result = converter.convert(null, cadFile, tempDir);

	Assert.assertNotNull(result.getConvertedFile());
	Assert.assertTrue(Files.exists(result.getConvertedFile()));
	Assert.assertEquals(1, result.getMaterials().size());
	Assert.assertTrue(Files.exists(result.getMaterials().get(0)));

	// cleanup
	if (result != null) {
	    result.close();
	}
    }

    @Test(expected=CADConverter.ConversionException.class)
    public void testInvalidConvert() throws Exception {
	// Assume assimp is installed
	Path assimp = Paths.get(DaeFileConverterImpl.CONF.getProperty("assimp"));
	Assume.assumeTrue(Files.exists(assimp) && Files.isExecutable(assimp));

	// setup
	Path tempDir = Paths.get("src/test/resources");
	Path daeFile = tempDir.resolve("bad/bad.dae");
	Mockito.when(cadFile.getName()).thenReturn(daeFile.toAbsolutePath().toString());

	ConversionResult result = converter.convert(null, cadFile, tempDir);

	//Test should fail
	Assert.fail();
	
	// cleanup
	if (result != null){
	    result.close();
	}
    }

}
