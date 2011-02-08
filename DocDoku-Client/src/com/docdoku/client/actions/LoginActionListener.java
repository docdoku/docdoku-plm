/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.client.actions;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.login.LoginFrame;
import com.docdoku.core.services.UserNotActiveException;
import com.docdoku.core.services.UserNotFoundException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.xml.ws.WebServiceException;

public class LoginActionListener implements ActionListener {
    
    private int mTry=0;
    private final static int MAX_TRY=3;
    
    @Override
    public void actionPerformed(ActionEvent pAE) {
        LoginFrame source = (LoginFrame) pAE.getSource();
        if(authenticate(source)){
            ExplorerFrame explorerFrame = new ExplorerFrame(new MDocTransferHandler(), new EditFolderActionListener());
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