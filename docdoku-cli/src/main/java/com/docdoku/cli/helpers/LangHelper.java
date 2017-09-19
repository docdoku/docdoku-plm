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

package com.docdoku.cli.helpers;

import com.docdoku.core.util.PropertiesLoader;

import java.util.Locale;
import java.util.Properties;

public class LangHelper {

    private static final String PROPERTIES_FILE_BASE_NAME = "/com/docdoku/cli/i18n/LocalStrings";

    private final Properties properties;

    public LangHelper(Locale locale) {
        properties = PropertiesLoader.loadLocalizedProperties(locale, PROPERTIES_FILE_BASE_NAME, getClass());
    }

    public String getLocalizedMessage(String key) {
        return properties.getProperty(key);
    }

}
