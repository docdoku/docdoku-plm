/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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
 * @author Florent Garin
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
            @Override
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
