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
package com.docdoku.server.viewers.utils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScormManifestParser {

    private static final String ORGANIZATIONS = "organizations";
    private static final String ORGANIZATION = "organization";
    private static final String DEFAULT = "default";
    private static final String IDENTIFIER = "identifier";
    private static final String TITLE = "title";
    private static final String ITEM = "item";
    public static final String ISVISIBLE = "isvisible";
    public static final String IDENTIFIERREF = "identifierref";
    public static final String RESOURCES = "resources";
    public static final String RESOURCE = "resource";
    public static final String HREF = "href";

    private InputStream manifestStream;
    private XMLStreamReader reader;
    private Map<String, ScormActivity> activitiesByIdentifierRef;

    private static final Logger LOGGER = Logger.getLogger(ScormManifestParser.class.getName());

    public ScormManifestParser(InputStream manifestStream) {
        this.manifestStream = manifestStream;
        this.activitiesByIdentifierRef = new HashMap<>();
    }

    public ScormOrganization parse() throws FileNotFoundException, XMLStreamException {

        XMLInputFactory factory = XMLInputFactory.newInstance();
        if (factory.isPropertySupported("javax.xml.stream.isValidating")) {
            factory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
        }

        reader = factory.createXMLStreamReader(manifestStream, "UTF-8");

        ScormOrganization scormOrganization = parseDefaultOrganization();

        try {
            this.manifestStream.close();
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return scormOrganization;
    }

    private ScormOrganization parseDefaultOrganization() throws XMLStreamException {

        boolean inResources = false;
        String defaultOrganizationIdentifier = "";
        ScormOrganization defaultOrganization = null;

        while(reader.hasNext()) {

            reader.next();

            if (reader.isStartElement()) {

                String startElementName = reader.getLocalName();

                if (startElementName.equals(ORGANIZATIONS)) {
                    defaultOrganizationIdentifier = reader.getAttributeValue(null, DEFAULT);
                } else if (startElementName.equals(ORGANIZATION)) {
                    String organizationIdentifier = reader.getAttributeValue(null, IDENTIFIER);
                    if (organizationIdentifier.equals(defaultOrganizationIdentifier)) {
                        defaultOrganization = new ScormOrganization();
                        parseActivities(defaultOrganization);
                    }
                } else if (startElementName.equals(RESOURCES)) {
                    inResources = true;
                } else if (startElementName.equals(RESOURCE) && inResources) {
                    String resourceIdentifier = reader.getAttributeValue(null, IDENTIFIER);
                    if (activitiesByIdentifierRef.containsKey(resourceIdentifier)) {
                        activitiesByIdentifierRef.get(resourceIdentifier).setResourceHref(reader.getAttributeValue(null, HREF));
                    }
                }

            } else if (reader.isEndElement()) {
                String endElementName = reader.getLocalName();
                if (endElementName.equals(RESOURCES)) {
                    break;
                }
            }

        }

        return defaultOrganization;

    }

    private void parseActivities(IScorm parent) throws XMLStreamException {

        ScormActivity subActivity;

        while (reader.hasNext()) {

            reader.next();

            if (reader.isStartElement()) {

                boolean hasPrefix = !reader.getPrefix().isEmpty();

                String startElement = reader.getLocalName();

                if (startElement.equals(ITEM)) {
                    String isVisible = reader.getAttributeValue(null, ISVISIBLE);
                    if (isVisible == null || Boolean.parseBoolean(isVisible)) {
                        String identifierRef = reader.getAttributeValue(null, IDENTIFIERREF);
                        subActivity = new ScormActivity();
                        subActivity.setResourceIdentifier(identifierRef);
                        activitiesByIdentifierRef.put(identifierRef, subActivity);
                        parent.addSubActivity(subActivity);
                        parseActivities(subActivity);
                    }
                } else if (startElement.equals(TITLE) && !hasPrefix) {
                    parent.setTitle(reader.getElementText());
                }

            } else if (reader.isEndElement()) {
                String endElementName = reader.getLocalName();
                if (endElementName.equals(ITEM)) {
                    return;
                } else if (endElementName.equals(ORGANIZATIONS)) {
                    return;
                }
            }
        }

    }

}
