package com.docdoku.client.actions;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.login.LoginFrame;
import com.docdoku.core.UserNotActiveException;
import com.docdoku.core.UserNotFoundException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.xml.ws.WebServiceException;

public class LoginActionListener implements ActionListener {
    
    private int mTry=0;
    private final static int MAX_TRY=3;
    
    public void actionPerformed(ActionEvent pAE) {
        LoginFrame source = (LoginFrame) pAE.getSource();
        if(authenticate(source)){
            ExplorerFrame explorerFrame = new ExplorerFrame(new MDocTransferHandler());
            ActionFactoryImpl connector = new ActionFactoryImpl(explorerFrame);
            explorerFrame.addElementSelectedListener(connector);
            explorerFrame.setActions(connector);
            explorerFrame.pack();
            explorerFrame.setVisible(true);
            source.dispose();
        }else{
            mTry++;
            if(mTry==MAX_TRY)
                System.exit(-1);
            source.clear();
        }
        
    }
    
    private boolean authenticate(LoginFrame pSource){
        try {
            String login = pSource.getUser();
            char[] password = pSource.getPassword();
            String workspace = pSource.getWorkspace();
            MainController.getInstance().login(login, new String(password), workspace);
        } catch (WebServiceException pWSEx) {
            Throwable cause=pWSEx.getCause();
            JOptionPane.showMessageDialog(pSource, I18N.BUNDLE.getString("Error_authentication"),
                    I18N.BUNDLE.getString("Error_title"), JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (UserNotActiveException pUNAEx) {
            JOptionPane.showMessageDialog(pSource,
                    pUNAEx.getMessage(), I18N.BUNDLE.getString("Error_title"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (UserNotFoundException pUNFEx) {
            JOptionPane.showMessageDialog(pSource,
                    pUNFEx.getMessage(), I18N.BUNDLE.getString("Error_title"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception pEx) {
            pEx.printStackTrace();
            String message = pEx.getMessage()==null?I18N.BUNDLE
                    .getString("Error_unknown"):pEx.getMessage();
            JOptionPane.showMessageDialog(null,
                    message, I18N.BUNDLE
                    .getString("Error_title"),
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        return true;
    }
}