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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;

public class MaxLengthDocument extends PlainDocument {

    private int mMaxChars;

    public MaxLengthDocument(int pMaxChars) {
        mMaxChars = pMaxChars;
    }

    @Override
    public void insertString(int pOffset, String pS, AttributeSet pA) throws BadLocationException {
        if (getLength() + pS.length() > mMaxChars) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        super.insertString(pOffset, pS, pA);
    }
}
