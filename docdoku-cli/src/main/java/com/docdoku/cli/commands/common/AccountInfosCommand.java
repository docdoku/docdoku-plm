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

package com.docdoku.cli.commands.common;

import com.docdoku.cli.commands.AbstractCommandLine;
import com.docdoku.cli.helpers.LangHelper;
import com.docdoku.cli.tools.ScriptingTools;
import com.docdoku.core.common.Account;
import com.docdoku.core.services.IUserManagerWS;

import java.io.IOException;

/**
 *
 * @author Morgan Guimard
 */
public class AccountInfosCommand extends AbstractCommandLine {

    private IUserManagerWS userS;

    public void execImpl() throws Exception {
        userS = ScriptingTools.createUserManagerService(getServerURL(),user,password);
        Account account = userS.getMyAccount();
        output.printAccount(account);
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("AccountInfosCommandDescription",user);
    }
}
