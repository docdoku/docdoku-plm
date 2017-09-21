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

package com.docdoku.i18n;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Properties files helper
 * * */
public class PropertiesLoader {

    public final static String[] SUPPORTED_LANGUAGES = {"fr", "en", "ru"};

    private PropertiesLoader() {
    }


    private final static Logger LOGGER = Logger.getLogger(PropertiesLoader.class.getName());

    /**
     * Load from given class resources an UTF8 encoded properties file with a lang suffix.
     * * */
    public static Properties loadLocalizedProperties(Locale locale, String propertiesFileBaseName, Class loader) {

        Properties properties = new Properties();

        String baseName;

        switch (locale.getLanguage()) {
            case "fr":
                baseName = propertiesFileBaseName + "_fr.properties";
                break;

            case "ru":
                baseName = propertiesFileBaseName + "_ru.properties";
                break;

            default:
                baseName = propertiesFileBaseName + "_en.properties";
                break;
        }

        try (InputStream is = loader.getResourceAsStream(baseName)) {
            properties.load(new InputStreamReader(is, "UTF-8"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }

        return properties;
    }

}
