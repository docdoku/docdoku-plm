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

package com.docdoku.server.viewers;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.IDataManagerLocal;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ViewerUtils {

    private ViewerUtils(){

    }

    public static String getURI(BinaryResource binaryResource, String uuid) {
        if(uuid == null){
            return "/api/files/" + binaryResource.getFullName();
        }else{
            return "/api/files/" + binaryResource.getFullName() + "/uuid/" + uuid;
        }
    }

    public static String getViewerTemplate(IDataManagerLocal dataManager, BinaryResource binaryResource, String uuid, String viewer) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("com/docdoku/server/viewers/viewer_template.mustache");
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("uriResource", ViewerUtils.getURI(binaryResource, uuid));

        String externalURL = dataManager.getExternalStorageURI(binaryResource);
        scopes.put("externalUriResource", externalURL);

        String shortenExternalURL = dataManager.getShortenExternalStorageURI(binaryResource);
        scopes.put("shortenExternalUriResource", shortenExternalURL);

        scopes.put("fileName", binaryResource.getName());
        scopes.put("thisId", UUID.randomUUID().toString());

        scopes.put("viewer", viewer);

        StringWriter templateWriter = new StringWriter();
        mustache.execute(templateWriter, scopes).flush();

        return templateWriter.toString();
    }

}
