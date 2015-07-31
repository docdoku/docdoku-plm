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

package com.docdoku.cli.helpers;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class LangHelper {

    private static final String BUNDLE_NAME = "com.docdoku.cli.i18n.LocalStrings";

    public static String getLocalizedMessage(String key, String userLogin) throws IOException {
        return getLocalizedMessage(key, new AccountsManager().getUserLocale(userLogin));
    }

    public static String getLocalizedMessage(String key, Locale locale){
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        return bundle.getString(key);
    }

    private LangHelper() {
    }

}
