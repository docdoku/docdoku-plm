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

package com.docdoku.cli.commands;

import com.docdoku.cli.helpers.AccountsManager;
import com.docdoku.cli.helpers.CliOutput;
import com.docdoku.cli.helpers.LangHelper;
import org.kohsuke.args4j.Option;

import java.io.Console;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 *
 * @author Florent Garin
 */
public abstract class BaseCommandLine extends AbstractCommandLine {

    @Option(name="-P", aliases = "--port", metaVar = "<port>", usage="port number to use for connection; default is 80")
    protected int port=80;

    @Option(name="-h", aliases = "--host", metaVar = "<host>", usage="host of the DocDokuPLM server to connect; default is docdokuplm.net")
    protected String host="docdokuplm.net";

    @Option(name="-p", aliases = "--password", metaVar = "<password>", usage="password to log in")
    protected String password;

    @Option(name="-S", aliases = "--ssl", usage="use a ssl (tls) connection")
    protected boolean ssl;

    private void promptForUser(Locale locale){
        Console c = System.console();
        if(c == null){
            return;
        }

        user = c.readLine(LangHelper.getLocalizedMessage("PromptUser",locale) + " '" + host + "': ");
    }

    private void promptForPassword(Locale locale){
        Console c = System.console();
        if(c == null){
            return;
        }
        password = new String(c.readPassword(LangHelper.getLocalizedMessage("PromptPassword",locale) + " '"  + user + "@" + host +"': "));
    }

    @Override
    public void exec() throws Exception {

        Locale userLocale = new AccountsManager().getUserLocale(user);
        output = CliOutput.getOutput(format,userLocale);
        if(user==null && format.equals(CliOutput.formats.HUMAN)){
            promptForUser(userLocale);
        }
        if(password==null && format.equals(CliOutput.formats.HUMAN)){
            promptForPassword(userLocale);
        }

        execImpl();

    }

    public URL getServerURL() throws MalformedURLException {
        return new URL( ssl ? "https":"http",host,port,"");
    }
}
