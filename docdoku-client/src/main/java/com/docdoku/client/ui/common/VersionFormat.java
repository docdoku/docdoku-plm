/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
