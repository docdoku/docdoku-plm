/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.jsf.actions;

import com.docdoku.core.common.Account;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.exceptions.PasswordRecoveryRequestNotFoundException;
import com.docdoku.core.security.PasswordRecoveryRequest;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IUserManagerLocal;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Named("recoveryBean")
@RequestScoped
public class RecoveryBean {

    @EJB
    private IMailerLocal mailer;
    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IAccountManagerLocal accountManager;

    private String login;
    private String newPassword;
    private String passwordRRUuid;

    public RecoveryBean() {
    }

    public String changePassword() throws PasswordRecoveryRequestNotFoundException {

        String uuid = passwordRRUuid;

        if(uuid == null){
            uuid = "";
        }

        userManager.recoverPassword(uuid, newPassword);
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        return request.getContextPath() + "/recovery.xhtml";
    }

    public String sendRecoveryMessage() throws AccountNotFoundException {
        Account account = accountManager.getAccount(login);
        PasswordRecoveryRequest passwdRR = userManager.createPasswordRecoveryRequest(account.getLogin());
        mailer.sendPasswordRecovery(account, passwdRR.getUuid());
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        return request.getContextPath() + "/recoveryRequested.xhtml";
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getPasswordRRUuid() {
        return passwordRRUuid;
    }

    public void setPasswordRRUuid(String passwordRRUuid) {
        this.passwordRRUuid = passwordRRUuid;
    }

}
