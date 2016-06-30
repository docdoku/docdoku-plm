package com.docdoku.api.models.utils;

import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.PartIterationDTO;
import com.docdoku.api.models.PartRevisionDTO;

public class LastIterationHelper {

    public static DocumentIterationDTO getLastIteration(DocumentRevisionDTO documentRevisionDTO){
        int iterations = documentRevisionDTO.getDocumentIterations().size();
        return documentRevisionDTO.getDocumentIterations().get(iterations - 1);
    }

    public static PartIterationDTO getLastIteration(PartRevisionDTO partRevisionDTO){
        int iterations = partRevisionDTO.getPartIterations().size();
        return partRevisionDTO.getPartIterations().get(iterations - 1);
    }

}
