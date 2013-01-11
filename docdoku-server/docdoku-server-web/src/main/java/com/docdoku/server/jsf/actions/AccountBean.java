/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
import com.docdoku.core.services.AccountNotFoundException;
import com.docdoku.core.services.IUserManagerLocal;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

@ManagedBean(name = "accountBean")
@RequestScoped
public class AccountBean {

    @EJB
    private IUserManagerLocal userManager;
    
    private String password;

    public AccountBean() {
    }

    public String updateAccount() throws AccountNotFoundException {

        //TODO switch to a more JSF style code
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
        Account account = (Account) request.getSession().getAttribute("account");
        //Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        String language = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        account.setLanguage(language);
                
        userManager.updateAccount(account.getName(), account.getEmail(), account.getLanguage(), password);

        return "/admin/editAccount.xhtml";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
