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

package com.docdoku.cli.helpers;

import com.docdoku.core.common.Account;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

public class AccountsManager {

    private static final String ACCOUNTS_FILE = ".dplm_account_properties";
    private static final String LANGUAGE_PROP = "language";

    private File accountsFile;
    private Properties accountsProps;

    public AccountsManager() throws IOException {

        accountsFile = new File(System.getProperty("user.home"),ACCOUNTS_FILE);
        accountsProps = new Properties();

        if(!accountsFile.exists()){
            accountsFile.createNewFile();
        }

        try{
            accountsProps.loadFromXML(new BufferedInputStream(new FileInputStream(accountsFile)));
        }catch(IOException ex){
            accountsFile.delete();
        }

    }

    private void saveIndex() throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(accountsFile));
        accountsProps.storeToXML(out, null);
    }

    public String getUserLanguage(String userLogin){
        return accountsProps.getProperty(userLogin + "." + LANGUAGE_PROP);
    }

    public void setUserLanguage(String userLogin, String language) throws IOException {
        accountsProps.setProperty(userLogin + "." + LANGUAGE_PROP, language);
        saveIndex();
    }

    public Locale getUserLocale(String userLogin){
        if(userLogin == null || userLogin.isEmpty()){
            return Locale.getDefault();
        }else{
            String userLanguage = getUserLanguage(userLogin);
            if(userLanguage == null)
                return Locale.getDefault();
            else
                return new Locale(getUserLanguage(userLogin));
        }
    }

    public void saveAccount(Account account) throws IOException {
        setUserLanguage(account.getLogin(),account.getLanguage());
    }
}
