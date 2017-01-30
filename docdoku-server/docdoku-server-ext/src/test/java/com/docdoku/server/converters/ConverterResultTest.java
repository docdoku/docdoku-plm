package com.docdoku.server.converters;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConverterResultTest {

    List<URI> uris;
    List<Path> materials;
    Path convertedFile;

    @Before
    public void setup() throws Exception {
	uris = Arrays.asList(new URI("file:/mat1"), new URI("file:/mat2"), new URI("file:/mat3"));
	materials = Arrays.asList(Paths.get(uris.get(0)), Paths.get(uris.get(1)), Paths.get(uris.get(2)));
	convertedFile = Paths.get(new URI("file:/conv"));
    }

    @Test
    public void testMaterials() throws Exception {
	ConversionResult cr = new ConversionResult();
	cr.setMaterials(materials);

	List<Path> actualMaterials = cr.getMaterials();
	Assert.assertEquals(uris.size(),actualMaterials.size());
	
	for (int i = 0; i < materials.size(); i++) {
	    Assert.assertEquals(materials.get(i), actualMaterials.get(i));
	}
	
	cr.close();
    }

    @Test
    public void testConstructor() throws Exception {
	ConversionResult cr = new ConversionResult(convertedFile, materials);
	
	Assert.assertEquals(convertedFile, cr.getConvertedFile());
	List<Path> actualMaterials = cr.getMaterials();
	for (int i=0; i<materials.size(); i++){
	    Assert.assertEquals(materials.get(i), actualMaterials.get(i));
	}
	
	cr.close();
    }
}
