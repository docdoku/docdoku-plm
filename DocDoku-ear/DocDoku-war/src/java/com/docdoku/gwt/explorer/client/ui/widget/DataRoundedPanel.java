package com.docdoku.gwt.explorer.client.ui.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author Florent GARIN
 */
public class DataRoundedPanel extends Composite {

    protected FlexTable inputPanel;
    protected RoundedPanel rp;
    protected Label headerLabel;
    protected VerticalPanel vp;

    public DataRoundedPanel() {

        rp = new RoundedPanel(RoundedPanel.ALL);
        rp.setCornerStyleName("my-Input-Zone-Corner");
        rp.setStyleName("my-Input-Zone-RP");
        inputPanel = new FlexTable();
        inputPanel.setCellSpacing(8);
        inputPanel.setStyleName("my-Input-Zone-Element");
        rp.setWidget(inputPanel);
        rp.setHeight("100%");
        initWidget(rp);
        setHeight("100%");
    }

    public DataRoundedPanel(String header, int corners) {
        
        vp = new VerticalPanel();
        rp = new RoundedPanel(corners);
        rp.setStyleName("my-Input-Zone-RP-Advanced");
        headerLabel=new Label(header);
        headerLabel.setStyleName("my-Input-Zone-Header");
        rp.setWidget(headerLabel);
        rp.setCornerStyleName("my-Input-Zone-Corner");

        inputPanel = new FlexTable();
        inputPanel.setCellSpacing(8);
        inputPanel.setStyleName("my-Input-Zone-Element-Advanced");

        vp.add(rp);
        vp.add(inputPanel);
        vp.setStyleName("my-Input-Zone");
        vp.setHeight("100%");
        initWidget(vp);
        setHeight("100%");
    }
    public DataRoundedPanel(String header) {
        this(header,RoundedPanel.TOP);
    }
}
