package com.docdoku.gwt.explorer.client.util;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author Florent GARIN
 */
public class HTMLUtil {

    private HTMLUtil() {

    }
    public static String imageItemHTML(AbstractImagePrototype imageProto, String title) {
        return "<span>" + imageProto.getHTML() + " " + title + "</span>";
    }

    public static void showError(String message) {
        final PopupPanel popup=new PopupPanel(true);
        popup.setStyleName("myPopup");
        Label label = new Label(message);
        label.setStyleName("myPopup-Element");
        RoundedPanel rp = new RoundedPanel(label);
        rp.setStyleName("myPopup");
        rp.setCornerStyleName("myPopup-Element");
        popup.setWidget(rp);

        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
          public void setPosition(int offsetWidth, int offsetHeight) {
            int left = (Window.getClientWidth() - offsetWidth) / 2;
            int top = (Window.getClientHeight() - offsetHeight) / 3;
            popup.setPopupPosition(left, top);
          }
        });

    }
}
