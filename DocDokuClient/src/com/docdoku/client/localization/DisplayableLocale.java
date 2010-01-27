package com.docdoku.client.localization;

import java.util.Locale;


public class DisplayableLocale {

	public Locale locale;
	
	public DisplayableLocale(Locale pLocale){
		locale=pLocale;
	}
	

	public boolean equals(Object pObj) {
		if(!(pObj instanceof DisplayableLocale))
			return false;
		else{
			DisplayableLocale disp=(DisplayableLocale)pObj;
			if(locale==null)
				return locale==disp.locale;
			else
				return locale.equals(disp.locale);
		}
	}

	public int hashCode() {
		return locale==null?0:locale.hashCode();
	}
	public String toString(){
		return locale==null?null:locale.getDisplayName();
	}
}
