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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.exceptions.ConvertedResourceException;
import com.docdoku.core.product.PartIteration;

import java.io.InputStream;
import java.util.Locale;

/**
 * OnDemandConverter plugin interface
 * Extension point for attached files conversion
 */
public interface OnDemandConverter {

    /**
     * Determine if plugin is able to convert given resource in given output format
     *
     * @param outputFormat   the output format
     * @param binaryResource the resource to convert
     * @return true if plugin can handle the conversion, false otherwise
     */
    boolean canConvert(String outputFormat, BinaryResource binaryResource);

    /**
     * Get the converted resource in given output format for a document iteration
     *
     * @param outputFormat      the output format
     * @param binaryResource    the resource to convert
     * @param documentIteration the document iteration concerned
     * @param locale            the locale to use for conversion
     * @return the converted resource input stream
     */
    InputStream getConvertedResource(String outputFormat, BinaryResource binaryResource, DocumentIteration documentIteration, Locale locale) throws ConvertedResourceException;

    /**
     * Get the converted resource in given output format for a part iteration
     *
     * @param outputFormat   the output format
     * @param binaryResource the resource to convert
     * @param partIteration  the part iteration concerned
     * @param locale         the locale to use for conversion
     * @return the converted resource input stream
     */
    InputStream getConvertedResource(String outputFormat, BinaryResource binaryResource, PartIteration partIteration, Locale locale) throws ConvertedResourceException;
}
