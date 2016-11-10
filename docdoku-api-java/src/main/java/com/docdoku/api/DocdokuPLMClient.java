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

import java.text.SimpleDateFormat;

/**
 * This class helps to create the swagger client.
 * <p>
 * This client does not use any authentication.
 *
 * @author Morgan Guimard
 */
public class DocdokuPLMClient {

    /**
     * Swagger ApiClient
     */
    protected ApiClient client;

    /**
     * Api endpoint (http://localhost:8080/api)
     */
    protected String host;

    /**
     * Debug flag. Turn on to log detailed http requests.
     */
    protected boolean debug;

    /**
     * DocdokuPLMClient constructor
     */
    public DocdokuPLMClient(String host, boolean debug) {
        this.host = host;
        this.debug = debug;
        createClient();
    }

    /**
     * Get the ApiClient
     */
    public ApiClient getClient() {
        return client;
    }

    /**
     * Instantiate the swagger ApiClient
     */
    protected void createClient() {
        client = new ApiClient();
        client.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        client.setBasePath(host);
        client.setDebugging(debug);
    }

}
