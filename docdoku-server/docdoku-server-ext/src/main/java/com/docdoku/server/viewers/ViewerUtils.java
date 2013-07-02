package com.docdoku.server.viewers;

import com.docdoku.core.common.BinaryResource;

public class ViewerUtils {

    private ViewerUtils(){}

    public static String getURI(BinaryResource binaryResource, String uuid) {
        if(uuid == null){
            return "/files/" + binaryResource.getFullName();
        }else{
            return "/shared-files/" + uuid +  "/" + binaryResource.getOwnerIteration() + "/" + binaryResource.getName();
        }
    }

}
