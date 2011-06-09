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
package com.docdoku.server.jsf.actions;

import com.docdoku.core.common.Account;
import com.docdoku.core.security.PasswordRecoveryRequest;
import com.docdoku.core.services.AccountNotFoundException;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.PasswordRecoveryRequestNotFoundException;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

@Named(value = "recoveryBean")
@RequestScoped
public class RecoveryBean {

    @EJB
    private IMailerLocal mailer;
    @EJB
    private IUserManagerLocal userManager;
    private String login;
    private String newPassword;
    private String passwordRRUuid;

    public RecoveryBean() {
    }

    public String changePassword() throws PasswordRecoveryRequestNotFoundException {
        if(passwordRRUuid==null)
            passwordRRUuid="";
        
        userManager.recoverPassword(passwordRRUuid, newPassword);
        return "/WEB-INF/recovery.xhtml";
    }

    public String sendRecoveryMessage() throws AccountNotFoundException {
        //Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        Account account = userManager.getAccount(login);
        PasswordRecoveryRequest passwdRR = userManager.createPasswordRecoveryRequest(account.getLogin());
        mailer.sendPasswordRecovery(account, passwdRR.getUuid());

        return "/WEB-INF/recoveryRequested.xhtml";
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
