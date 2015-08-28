package com.docdoku.server;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ServicesInjector {

    public static final String DATAMANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/DataManagerBean";
    public static final String PRODUCTMANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/ProductManagerBean";

    public static Object inject(String serviceName) throws NamingException {
        Context context = new InitialContext();
        return context.lookup(serviceName);
    }

}
