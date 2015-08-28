package com.docdoku.server;

import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class ServiceLocator {
    private static final String DATA_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/DataManagerBean!com.docdoku.core.services.IDataManagerLocal";
    private static final String PRODUCT_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/ProductManagerBean";

    private Context context;


    private static final Logger LOGGER = Logger.getLogger(ServiceLocator.class.getName());

    @PostConstruct
    private void init(){
        try{
            context=new InitialContext();
        }
        catch (NamingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }






    @InternalService
    @Produces
    public IProductManagerLocal findProductManager() throws NamingException {
        return (IProductManagerLocal) context.lookup(PRODUCT_MANAGER);
    }

    @InternalService
    @Produces
    public IDataManagerLocal findDataManager() throws NamingException {
        return (IDataManagerLocal) context.lookup(DATA_MANAGER);
    }

}
