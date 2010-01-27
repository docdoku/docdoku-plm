/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

import java.util.Locale;

public class DisplayableLocale {

    public Locale locale;

    public DisplayableLocale(Locale pLocale) {
        locale = pLocale;
    }

    @Override
    public boolean equals(Object pObj) {
        if (!(pObj instanceof DisplayableLocale)) {
            return false;
        } else {
            DisplayableLocale disp = (DisplayableLocale) pObj;
            if (locale == null) {
                return locale == disp.locale;
            } else {
                return locale.equals(disp.locale);
            }
        }
    }

    @Override
    public int hashCode() {
        return locale == null ? 0 : locale.hashCode();
    }

    @Override
    public String toString() {
        return locale == null ? null : locale.getDisplayName();
    }
}
