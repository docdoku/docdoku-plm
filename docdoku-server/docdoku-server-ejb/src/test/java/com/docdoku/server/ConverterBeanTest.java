package com.docdoku.server;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.Geometry;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IBinaryStorageManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.converters.CADConverter;
import com.docdoku.server.converters.ConversionResult;

@RunWith(MockitoJUnitRunner.class)
public class ConverterBeanTest {

    @Mock
    BeanLocator locator;

    @Mock
    EntityManager em;

    @Mock
    IProductManagerLocal product;

    @Mock
    IBinaryStorageManagerLocal storage;

    @InjectMocks
    ConverterBean bean;

    @Mock
    PartIterationKey ipk;

    @Mock
    PartIteration partIter;

    @Mock
    BinaryResource cadBinRes;

    @Mock
    CADConverter conv;

    @Mock
    ConversionResult result;

    @Mock
    Geometry lod;

    @Mock
    BinaryResource attachedFile;

    @Before
    public void setup() throws Exception {
	when(cadBinRes.getName()).thenReturn("foo.dae");
	when(em.find(PartIteration.class, ipk)).thenReturn(partIter);
	when(storage.getBinaryResourceInputStream(cadBinRes))
	.thenReturn(new ByteArrayInputStream("fake content".getBytes()));

	when(product.saveGeometryInPartIteration(eq(ipk), anyString(), anyInt(), anyLong(), any(double[].class)))
	.thenReturn(lod);
	when(storage.getBinaryResourceOutputStream(lod)).thenReturn(new ByteArrayOutputStream());
	
	when(product.saveFileInPartIteration(eq(ipk), anyString(), eq("attachedfiles"), anyLong()))
	.thenReturn(attachedFile, attachedFile);
	when(storage.getBinaryResourceOutputStream(attachedFile)).thenReturn(new ByteArrayOutputStream());
	
	when(conv.canConvertToOBJ("dae")).thenReturn(true);

	when(result.getConvertedFile()).thenReturn(Paths.get("src/test/resources/fake.obj"));
	when(result.getMaterials()).thenReturn(Arrays.asList(Paths.get("src/test/resources/fake.obj.1.mtl"),
		Paths.get("src/test/resources/fake.obj.2.mtl")));

	when(conv.convert(any(URI.class), any(URI.class))).thenReturn(result);

	when(locator.search(CADConverter.class)).thenReturn(Arrays.asList(conv));
	
	bean.init();	
    }

    @Test
    public void testNominalConvert() throws Exception {
	// * test *
	bean.convertCADFileToOBJ(ipk, cadBinRes);
	verify(locator).search(CADConverter.class);
	verify(conv).canConvertToOBJ("dae");
	verify(conv).convert(any(URI.class), any(URI.class));
	verify(product).saveGeometryInPartIteration(eq(ipk), anyString(), anyInt(), anyLong(), any(double[].class));
	verify(storage).getBinaryResourceOutputStream(lod);
	verify(product, times(2)).saveFileInPartIteration(eq(ipk), anyString(), eq("attachedfiles"), anyLong());
	verify(storage, times(2)).getBinaryResourceOutputStream(attachedFile);
    }

    @Test
    public void testNoConverter() throws Exception {
	// * setup *
	when(cadBinRes.getName()).thenReturn("foo.unknown");

	// * test *
	bean.convertCADFileToOBJ(ipk, cadBinRes);

	verify(locator).search(CADConverter.class);
	verify(conv).canConvertToOBJ("unknown");
	verify(conv, never()).convert(any(), any());
	verify(product, never()).saveGeometryInPartIteration(any(), anyString(), anyInt(), anyLong(), any());
	verify(product, never()).saveFileInPartIteration(any(), anyString(), anyString(), anyLong());
	verify(storage, never()).getBinaryResourceOutputStream(any());
    }

    @Test
    public void testBrokenConvert() throws Exception {
	// * setup *
	when(conv.convert(any(URI.class), any(URI.class))).thenThrow(new CADConverter.ConversionException("error"));
	
	// * test *
	bean.convertCADFileToOBJ(ipk, cadBinRes);
	
	verify(conv).canConvertToOBJ("dae");
	verify(conv).convert(any(URI.class), any(URI.class));
	verify(product, never()).saveGeometryInPartIteration(any(), anyString(), anyInt(), anyLong(), any());
	verify(product, never()).saveFileInPartIteration(any(), anyString(), anyString(), anyLong());
	verify(storage, never()).getBinaryResourceOutputStream(any());
    }

}
