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

package com.docdoku.cli.commands;


import org.kohsuke.args4j.Option;
import java.io.Console;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractCommandLine implements CommandLine{


    @Option(name="-P", aliases = "--port", metaVar = "<port>", usage="port number to use for connection; default is 80")
    protected int port=80;

    @Option(name="-h", aliases = "--host", metaVar = "<host>", usage="host of the DocDokuPLM server to connect; default is docdokuplm.net")
    protected String host="docdokuplm.net";

    @Option(name="-p", aliases = "--password", metaVar = "<password>", usage="password to log in")
    protected String password;

    @Option(name="-u", aliases = "--user", metaVar = "<user>", usage="user for login")
    protected String user;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(name="-j", aliases = "--jsonparser", usage="return a JSON description of the status part")
    protected boolean jsonParser;

    private void promptForUser(){
        Console c = System.console();
        user = c.readLine("Please enter user for '" + host + "': ");
    }

    private void promptForPassword(){
        Console c = System.console();
        password = new String(c.readPassword("Please enter password for '" + user +"@" + host +"': "));
    }

    @Override
    public void exec() throws Exception {

        if(user==null){
            promptForUser();
        }
        if(password==null){
            promptForPassword();
        }
        execImpl();
    }


    public URL getServerURL() throws MalformedURLException {
        return new URL("http",host,port,"");
    }
    public abstract void  execImpl() throws Exception;
}
