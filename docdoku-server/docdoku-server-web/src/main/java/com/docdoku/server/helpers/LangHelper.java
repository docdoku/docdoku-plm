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

package com.docdoku.server.helpers;


import java.util.Locale;
import java.util.ResourceBundle;

public class LangHelper {

    private static final String DEFAULT_BUNDLE_NAME = "com.docdoku.server.localization.LocalStrings";
    private static ResourceBundle mResourceBundle;


    public LangHelper() {
        mResourceBundle=ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, Locale.getDefault());

    }

    public LangHelper(Locale locale) {
        mResourceBundle=ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, locale);

    }

    public String getLocalizedMessage(String key, Locale locale){
        return mResourceBundle.getString(key);
    }

}
