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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Locale;
import java.util.ResourceBundle;

@FacesValidator(value = "samePasswordValidator")
public class SamePasswordValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException {
        UIInput newPasswordInput = (UIInput) toValidate.getAttributes().get("newPasswordInput");
        UIInput confirmPasswordInput = (UIInput) toValidate.getAttributes().get("confirmPasswordInput");
        String password = (String) newPasswordInput.getValue();
        String confirmPassword = (String) confirmPasswordInput.getValue();


        if (password != null && confirmPassword != null && !confirmPassword.equals(password)) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            String messageBundleName = facesContext.getApplication().getMessageBundle();
            Locale locale = facesContext.getViewRoot().getLocale();
            ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName, locale);

            String msg = bundle.getString("confirmPassword");
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
            //FacesContext.getCurrentInstance().addMessage(toValidate.getClientId(), message);
            throw new ValidatorException(message);
        }


    }
}
