package com.docdoku.gwt.explorer.client.util;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import org.cobogw.gwt.user.client.ui.RoundedPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;

/**
 *
 * @author Florent GARIN
 */
public class HTMLUtil {

    private HTMLUtil() {

    }
    public static String imageItemHTML(ImageResource imageResource, String title) {
        return "<span>" + AbstractImagePrototype.create(imageResource).getHTML() + " " + title + "</span>";
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

    public static String getWebContext(){
        String moduleBaseURL = GWT.getModuleBaseURL();
        String moduleName = GWT.getModuleName();
        String webContext = moduleBaseURL.split("/")[3];
        if (webContext.equals(moduleName)) {
            return null;
        } else {
            return webContext;
        }
    }
}
