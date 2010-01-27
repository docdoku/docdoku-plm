package com.docdoku.client.ui.common;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Desktop;

import com.docdoku.client.localization.I18N;

public class WebLink extends JLabel {
    
    public WebLink(){
        init();
    }
    
    public WebLink(String pLabel){
        super("<html><a href=\"#\">"+pLabel+"</a>");
        init();
    }

    public WebLink(String pLabel, String pTarget) {
        this(pLabel);
        try {
            setTargetLink(new URI(pTarget));
        } catch (Exception pEx) {
            System.err.println(pEx.getMessage());
        }

    }

    public WebLink(String pLabel, URI pTarget){
        this(pLabel);
        setTargetLink(pTarget);
    }

    public void setLink(String pLabel, String pTarget){
        setText("<html><a href=\"#\">"+pLabel+"</a>");
        try {
            setTargetLink(new URI(pTarget));
        } catch (Exception pEx) {
            System.err.println(pEx.getMessage());
        }
    }
    

    private void init(){
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
    private void setTargetLink(final URI pTarget){
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent pEvent) {
                try {
                    Desktop.getDesktop().browse(pTarget);
                } catch (Exception pEx) {
                    String message = pEx.getMessage()==null?I18N.BUNDLE
                            .getString("Error_unknown"):pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE
                            .getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
}
