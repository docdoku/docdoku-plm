/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

package com.docdoku.client.ui.common;

import com.docdoku.core.common.Version;
import com.docdoku.core.common.VersionFormatException;

import java.text.Format;
import java.text.FieldPosition;
import java.text.ParsePosition;

public class VersionFormat extends Format {

    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        toAppendTo.append(obj.toString());
        return toAppendTo;
    }

    public Object parseObject(String source, ParsePosition pos) {
        try {
            Version version = new Version(source.substring(pos.getIndex()));
            pos.setIndex(source.length());
            return version;
        } catch (VersionFormatException pLVFEx) {
            pos.setErrorIndex(pLVFEx.getErrorOffset());
            return null;
        }
    }
}
