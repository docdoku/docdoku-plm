package com.docdoku.server;

import javax.annotation.PostConstruct;
import javax.ejb.SessionContext;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by morgan on 04/09/15.
 */

@ApplicationScoped
public class SessionContextProducer {

    private static final Logger LOGGER = Logger.getLogger(SessionContextProducer.class.getName());

    private Context context;

    @PostConstruct
    private void init(){
        try{
            context = new InitialContext();
        }
        catch (NamingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Produces
    @Default
    @RequestScoped
    public SessionContext create() throws NamingException {
        return (SessionContext) context.lookup("java:comp/EJBContext");
    }


}
