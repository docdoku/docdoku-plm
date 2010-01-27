package com.docdoku.client.ui.common;

import com.docdoku.core.entities.keys.Version;
import com.docdoku.core.entities.keys.VersionFormatException;

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
