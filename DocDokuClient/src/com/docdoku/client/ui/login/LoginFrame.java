package com.docdoku.client.ui.login;

import com.docdoku.client.localization.I18N;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.docdoku.client.ui.common.OKButton;

public class LoginFrame extends JFrame implements ActionListener {
    
    private OKButton mValidButton;
    private LoginPanel mLoginPanel;
    private ActionListener mAction;
    
    public LoginFrame(ActionListener pAction) {
        super(I18N.BUNDLE.getString("LoginFrame_title"));
        setLocationRelativeTo(null);
        mAction = pAction;
        mLoginPanel = new LoginPanel();
        mValidButton = new OKButton(I18N.BUNDLE.getString("Login_title"));
        Image img =
                Toolkit.getDefaultToolkit().getImage(LoginFrame.class.getResource("/com/docdoku/client/resources/icons/key1.png"));
        setIconImage(img);
        createLayout();
        createListener();
        pack();
        setVisible(true);
    }
    
    public String getUser() {
        return mLoginPanel.getUser();
    }
    
    public char[] getPassword() {
        return mLoginPanel.getPassword();
    }
    
    public String getWorkspace() {
        return mLoginPanel.getWorkspace();
    }
    
    public void clear() {
        mLoginPanel.clear();
    }
    
    private void createLayout() {
        getRootPane().setDefaultButton(mValidButton);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mLoginPanel, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        southPanel.add(mValidButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
        
    }
    
    private void createListener() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent pWE) {
                System.exit(0);
            }
        });
        mValidButton.addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
