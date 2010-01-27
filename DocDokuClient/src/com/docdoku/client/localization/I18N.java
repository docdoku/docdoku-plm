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