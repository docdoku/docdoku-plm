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

    public void insertString(int pOffset, String pS, AttributeSet pA) throws BadLocationException {
        if (getLength() + pS.length() > mMaxChars) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        super.insertString(pOffset, pS, pA);
    }
}
