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

package com.docdoku.server.storage.googlestorage;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Asmae Chadid
 */
public class GoogleStorageProperties {
    private Properties properties;

    public GoogleStorageProperties() {
        properties = new Properties();
        load();
    }

    public String getProjectId() {
        return properties.getProperty("googlestorage.projectId");
    }

    public String getApiVersion() {
        return properties.getProperty("googlestorage.apiVersion");
    }

    public String getURI() {
        return properties.getProperty("googlestorage.uri");
    }

    public String getClientId() {
        return properties.getProperty("googlestorage.clientId");
    }

    public String getClientSecret() {
        return properties.getProperty("googlestorage.clientSecret");
    }

    public String getAuthorisationCode() {
        return properties.getProperty("googlestorage.authorisationCode");
    }

    public String getRedirectUri() {
        return properties.getProperty("googlestorage.redirectUri");
    }

    public String getScope() {
        return properties.getProperty("googlestorage.scope");
    }

    public String getRefreshToken() {
        return properties.getProperty("googlestorage.refreshToken");
    }

    public String getBucketName() {
        return properties.getProperty("googlestorage.bucketName");
    }

    private Properties load() {
        try {
            properties.load(getClass().getResourceAsStream("/com/docdoku/server/storage/googlestorage/conf.properties"));
        } catch (IOException e) {
            throw new RuntimeException("googlestorage.properties not found. Error: ", e);
        }
        return properties;
    }

}
