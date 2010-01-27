package com.docdoku.client.ui.common;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;


public interface GUIConstants {

    public final static String MDOC_FLAVOR = DataFlavor.javaJVMLocalObjectMimeType +  ";class=com.docdoku.core.entities.MasterDocument";
    public final static Insets INSETS = new Insets(2, 5, 2, 5);
    public final static String LARGE_ICON = "LargeIcon";
    public final static Border WORKFLOW_CANVAS_MARGIN_BORDER =  new EmptyBorder(60,60,60,60);;

}