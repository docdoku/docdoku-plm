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

package com.docdoku.test.smoke;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Asmae Chadid
 *
 */
public class SmokeTestProperties {
    private Properties properties;

    public SmokeTestProperties() {
        properties = new Properties();
        load();
    }

    public String getWebappURL(){
      return properties.getProperty("webapp.url");
    }

    public int getWebappPort(){
        return Integer.parseInt(properties.getProperty("webapp.port")) ;
    }

    public String getLoginForUser1(){
        return properties.getProperty("login.user1") ;
    }
    public String getLoginForUser2(){

        return properties.getProperty("login.user2") ;
    }
    public String getPassword(){
        return properties.getProperty("password");
    }
    public String getWorkspace(){
        return properties.getProperty("workspace");
    }

    public String getImapServer(){
        return properties.getProperty("imap.server");
    }
    public String getImapLogin(){
        return properties.getProperty("imap.login");
    }
    public String getImapPassword(){
        return properties.getProperty("imap.password");
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http",this.getWebappURL(), this.getWebappPort(), "");
    }
    private Properties load() {
        try {
            properties.load(getClass().getResourceAsStream("/com/docdoku/test/smoke/conf.properties"));
        }
        catch (IOException e) {
            throw new RuntimeException("conf.properties not found. Error: ", e);
        }
        return properties;
    }
}
