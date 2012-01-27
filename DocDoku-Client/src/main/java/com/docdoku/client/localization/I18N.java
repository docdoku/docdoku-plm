/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import com.docdoku.client.data.Prefs;

public class I18N {

    
        private I18N(){}
        
	private final static String BASE_NAME = "com.docdoku.client.localization.docdoku_resource";
	private static DisplayableLocale[] sDisplayableLocales;

	public final static ResourceBundle BUNDLE = ResourceBundle.getBundle(BASE_NAME, Prefs.getLocale());

	public static DisplayableLocale[] getDisplayableLocales() {
		if (sDisplayableLocales == null)
			initDisplayableLocales();
		return sDisplayableLocales;
	}

	private static void initDisplayableLocales() {
		List<DisplayableLocale> locales = new ArrayList<DisplayableLocale>();
		Locale[] availableLocales = Locale.getAvailableLocales();
		for (int i = 0; i < availableLocales.length; i++) {
			if (ResourceBundle.getBundle(BASE_NAME, availableLocales[i])
					.getLocale().equals(availableLocales[i]))
				locales.add(new DisplayableLocale(availableLocales[i]));
		}
		sDisplayableLocales = locales
				.toArray(new DisplayableLocale[locales.size()]);
	}
        
        public static char getCharBundle(String property){
            return BUNDLE.getString(property).charAt(0);
        }

}