package com.docdoku.server.viewers;

import com.docdoku.core.common.BinaryResource;

public class ViewerUtils {

    private ViewerUtils(){}

    public static String getURI(BinaryResource binaryResource) {
        return "/files/" + binaryResource.getFullName();
    }

}
