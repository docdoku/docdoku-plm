package com.docdoku.core.product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laurentlevan on 29/06/16.
 */
public class ImportPreview {

    private List<PartRevision> partRevisions;

    public ImportPreview(){
        this.partRevisions = new ArrayList<>();
    }

    public ImportPreview(List<PartRevision> partRevisions){
        this.partRevisions = partRevisions;
    }

    public List<PartRevision> getPartRevisions(){
        return partRevisions;
    }

    public void setPartRevisions(List<PartRevision> partRevisions){
        this.partRevisions = partRevisions;
    }


}
