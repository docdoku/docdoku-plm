package fr.senioriales.stocks.gwt.client.ui.widget.util;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * InteractiveHTML is like an HTML widget, but displays a popup tooltip on mouse over
 * 
 */
public class InteractiveHTML extends HTML implements MouseOutHandler, MouseMoveHandler, ClickHandler {

    private TooltipPanel tooltipPanel;

    public InteractiveHTML(String html, String tooltip[], boolean hideOnClick) {
        super(html);
        this.tooltipPanel = new TooltipPanel(tooltip);
        addMouseMoveHandler(this);
        addMouseOutHandler(this);
        if (hideOnClick) {
            addClickHandler(this);
        }

    }

    public InteractiveHTML(String html, String tooltip[]){
        this(html, tooltip, true) ;
    }


    public void onMouseMove(MouseMoveEvent event) {

        if (!tooltipPanel.isShowing()) {
            tooltipPanel.showRelativeTo(this);
        }
    }

    public void onMouseOut(MouseOutEvent event) {
        tooltipPanel.hide();
    }

    public void onClick(ClickEvent event) {
        tooltipPanel.hide();
    }

    private class TooltipPanel extends DecoratedPopupPanel {

        private HorizontalPanel panel;

        public TooltipPanel(String input[]) {
            panel = new HorizontalPanel();
            for (String str : input) {
                panel.add(new InlineLabel(str));
            }
            setWidget(panel);
        }
    }
}
