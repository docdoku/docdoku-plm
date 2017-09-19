package com.docdoku.core.util;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesLoader {

    private PropertiesLoader() {
    }

    private final static Logger LOGGER = Logger.getLogger(PropertiesLoader.class.getName());

    public static Properties loadLocalizedProperties(Locale locale, String propertiesFileBaseName, Class loader) {

        Properties properties = new Properties();

        String baseName;

        switch (locale.getLanguage()) {
            case "fr":
                baseName = propertiesFileBaseName + "_fr.properties";
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
