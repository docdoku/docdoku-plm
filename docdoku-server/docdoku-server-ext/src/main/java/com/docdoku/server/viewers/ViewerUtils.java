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

    private ViewerUtils(){}

    public static String getURI(BinaryResource binaryResource, String uuid) {
        if(uuid == null){
            return "/api/files/" + binaryResource.getFullName();
        }else{
            return "/api/files/" + uuid +  "/" + binaryResource.getOwnerIteration() + "/" + binaryResource.getName();
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
