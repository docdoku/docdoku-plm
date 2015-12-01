package com.docdoku.server.jsf.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Map;

@Named("errorBean")
@ApplicationScoped
public class ErrorBean implements Serializable {

    public String getExceptionMessage() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map requestMap = context.getExternalContext().getRequestMap();
        Throwable cause = (Throwable) requestMap.get("javax.servlet.error.exception");
        while(cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

}
