/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.server.converters.catia.product.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.EmptyStackException;
import java.util.Stack;

public class ComponentDTKSaxHandler extends DefaultHandler {

    private final StringBuilder buffer = new StringBuilder(128);

    private static final String COMPONENT = "Component",
            ID_ATTR = "id",
            FATHER_ID_ATTR = "fatherId",
            ASSEMBLY_ATTR = "assembly",
            TYPE_ATTR = "type",
            NAME = "Name",
            POSITIONING = "Positioning",
            METADATA = "MetaData",
            METADATA_TYPE = "MetaDataType",
            METADATA_TITLE = "Title",
            METADATA_VALUE = "Value",
            METADATA_UNITS = "Units",
            INSTANCE_NAME = "InstanceName";


    private ComponentDTK componentDtk;
    private Stack<ComponentDTK> components = new Stack<>();

    private MetaData metaData;
    private Stack<MetaData> metaDataStack = new Stack<>();

    public ComponentDTKSaxHandler() {
        super();
    }

    public ComponentDTK getComponent() {
        return componentDtk;
    }

    @Override
    public void startDocument() throws SAXException {
        // Nothing to do
    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        buffer.append(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        ComponentDTK lastComponentDtk = getLastComponentDtk();

        if (COMPONENT.equals(qName)) {

            //New component
            ComponentDTK currentComponentDtk = new ComponentDTK(
                    getInt(attributes, ID_ATTR),
                    getInt(attributes, FATHER_ID_ATTR),
                    getString(attributes, TYPE_ATTR),
                    getBoolean(attributes, ASSEMBLY_ATTR)
            );

            if (lastComponentDtk != null) {
                // Sub component
                lastComponentDtk.addSubComponent(currentComponentDtk);
            } else {
                // First component
                componentDtk = currentComponentDtk;
            }

            components.push(currentComponentDtk);

        } else if (POSITIONING.equals(qName)) {

            if (lastComponentDtk != null) {
                lastComponentDtk.setPositioning(new Positioning(
                        getDouble(attributes, "rx"),
                        getDouble(attributes, "ry"),
                        getDouble(attributes, "rz"),
                        getDouble(attributes, "tx"),
                        getDouble(attributes, "ty"),
                        getDouble(attributes, "tz")
                ));
            }

        } else if (METADATA.equals(qName)) {
            MetaData currentMetaData = new MetaData();
            MetaData lastMetaData = getLastMetaData();
            if (lastMetaData == null) {
                metaData = currentMetaData;
            }
            metaDataStack.push(metaData);
        }

    }


    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (COMPONENT.equals(qName)) {
            components.pop();
        } else if (NAME.equals(qName)) {
            getLastComponentDtk().setName(getBufferValue());
        } else if (INSTANCE_NAME.equals(qName)) {
            getLastComponentDtk().setInstanceName(getBufferValue());
        } else if (METADATA.equals(qName)) {
            MetaData lastMetaData = getLastMetaData();
            ComponentDTK lastComponentDtk = getLastComponentDtk();
            if (lastMetaData != null && lastComponentDtk != null) {
                lastComponentDtk.addMetaData(lastMetaData);
            }
            metaDataStack.pop();
        } else if (METADATA_TITLE.equals(qName)) {
            MetaData lastMetaData = getLastMetaData();
            if (lastMetaData != null) {
                lastMetaData.title = getBufferValue();
            }
        } else if (METADATA_TYPE.equals(qName)) {
            MetaData lastMetaData = getLastMetaData();
            if (lastMetaData != null) {
                lastMetaData.metaDataType = getBufferValue();
            }
        } else if (METADATA_UNITS.equals(qName)) {
            MetaData lastMetaData = getLastMetaData();
            if (lastMetaData != null) {
                lastMetaData.units = getBufferValue();
            }
        } else if (METADATA_VALUE.equals(qName)) {
            MetaData lastMetaData = getLastMetaData();
            if (lastMetaData != null) {
                lastMetaData.value = getBufferValue();
            }
        }

        buffer.setLength(0);
    }


    private ComponentDTK getLastComponentDtk() {
        try {
            return components.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    private MetaData getLastMetaData() {
        try {
            return metaDataStack.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    private String getBufferValue() {
        if (buffer.length() == 0) {
            return null;
        }
        String value = buffer.toString().trim();
        buffer.setLength(0);
        return value.length() == 0 ? null : value;
    }

    private static boolean getBoolean(Attributes attributes, String name) {
        String s = getString(attributes, name);
        return "1".equals(s) || Boolean.parseBoolean(s);
    }

    private static Integer getInt(Attributes attributes, String name) {
        String s = getString(attributes, name);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Double getDouble(Attributes attributes, String name) {
        String s = getString(attributes, name);
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.00;
        }
    }

    private static String getString(Attributes attributes, String name) {
        String s = attributes.getValue(name);
        if (s == null) {
            return null;
        }
        // trim leading and trailing spaces.
        s = s.trim();
        // see if empty string.
        if (s.length() == 0) {
            return null;
        }
        return s;
    }


}