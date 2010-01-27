/*
 * ClassLoaderHackBoot.java
 *
 * Created on 2 octobre 2007, 09:49
 *
 */

package com.docdoku.client;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Florent.Garin
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

