/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.client;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author Florent GARIN
 */
public class ClassLoaderHackBoot {
    
    public static void main(String[] args) throws Exception {
        System.setSecurityManager(null);
        ClassLoader oldcc = Thread.currentThread().getContextClassLoader();
        ClassLoader currentClassLoader = ClassLoaderHackBoot.class.getClassLoader();
        final ClassLoader hackedClassLoader = createClassLoader(currentClassLoader);
        Thread.currentThread().setContextClassLoader(hackedClassLoader);
        //hack to create AWT-EventQueue in case the Java Console is not shown
        JFrame waitFrame = new javax.swing.JFrame();
        waitFrame.setVisible(true);
        
        try {
            EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eq.invokeAndWait(new Runnable() {
                public void run() {
                    Thread.currentThread().setContextClassLoader(hackedClassLoader);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        waitFrame.setVisible(false);
        Class boot =hackedClassLoader.loadClass("com.docdoku.client.ExplorerBoot");
        //Class boot =hackedClassLoader.loadClass("com.docdoku.client.Test");
        Method main = boot.getMethod("main",String[].class);
        main.invoke(null,new Object[]{args});
        waitFrame.dispose();
    }
    
    private ClassLoaderHackBoot() {
    }
    
    
    
    public static String[] maskedPackages = new String[]{
        "com.docdoku.",
        "com.sun.xml.bind.",
        "com.sun.xml.ws.",
        "javax.xml.bind.",
        "javax.xml.ws."
    };
    
    
    public static ClassLoader createClassLoader(ClassLoader cl) throws ClassNotFoundException, MalformedURLException {
        
        URL[] urls = findAPIs(cl);
        List<String> mask = new ArrayList<String>(Arrays.asList(maskedPackages));
        
        // first create a protected area so that we load JAXB/WS 2.1 API
        // and everything that depends on them inside
        cl = new MaskingClassLoader(cl,mask);
        
        // then this classloader loads the API
        cl = new URLClassLoader(urls, cl);
        
        // finally load the rest of the RI. The actual class files are loaded from ancestors
        cl = new ParallelWorldClassLoader(cl,"");
        
        return cl;
    }
    
    
    private static URL[] findAPIs(ClassLoader cl) throws ClassNotFoundException, MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        
        URL res = cl.getResource("javax/xml/bind/annotation/XmlSeeAlso.class");
        if(res==null)
            throw new ClassNotFoundException("There's no JAXB 2.1 API in the classpath");
        urls.add(ParallelWorldClassLoader.toJarUrl(res));
        
        return urls.toArray(new URL[urls.size()]);
    }
    
}

