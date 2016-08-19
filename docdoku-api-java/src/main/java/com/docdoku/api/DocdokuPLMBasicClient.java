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

package com.docdoku.api;

import com.docdoku.api.client.ApiClient;
import okhttp3.Credentials;

/**
 * This class helps to create the swagger client.
 * @author Morgan Guimard
 */
public class DocdokuPLMBasicClient extends DocdokuPLMClient{

    public DocdokuPLMBasicClient(String host, String login, String password) {
        this(host,login,password,false);
    }

    public DocdokuPLMBasicClient(String host, String login, String password, boolean debug)  {
        super(host,debug);
        client.addDefaultHeader("Authorization", Credentials.basic(login, password));
    }

    public ApiClient getClient(){
        return client;
    }

}
