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
import com.docdoku.cli.helpers.CommandLine;
import org.kohsuke.args4j.Option;

import java.util.Locale;

/**
 *
 * @author Florent Garin
 */
public abstract class AbstractCommandLine implements CommandLine {

    @Option(name="-u", aliases = "--user", metaVar = "<user>", usage="user for login")
    protected String user;

    @Option(name="-F", aliases = "--format", metaVar = "<format>", usage="output format, possible value: json")
    protected CliOutput.formats format = CliOutput.formats.HUMAN;


    //A default value is set in case an exception is raised
    //inside the CmdLineParser.parseArgument(args) method.
    protected CliOutput output = CliOutput.getOutput(format, Locale.getDefault());

    @Override
    public void exec() throws Exception {
        Locale userLocale = new AccountsManager().getUserLocale(user);
        output = CliOutput.getOutput(format,userLocale);

        execImpl();

    }

    @Override
    public CliOutput getOutput(){
        return output;
    }

    public abstract void execImpl() throws Exception;
}
