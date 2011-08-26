package com.docdoku.server.webdav;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

public class WorkspaceResourceFactory implements ResourceFactory {



    @Override
    public Resource getResource(String host, String p) {
        Path path = Path.path(p).getStripFirst();

        if (path.isRoot()) {
            return new AllFoldersResource();
        } else if (path.getLength() == 1) {
            return new FolderResource("folder4");
        } else if (path.getLength() == 2) {
            return null;
        } else {
            return null;
        }
    }
}
