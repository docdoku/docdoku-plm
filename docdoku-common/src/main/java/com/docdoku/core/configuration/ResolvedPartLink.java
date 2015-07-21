package com.docdoku.core.configuration;

import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartLink;

/**
 * Created by morgan on 21/07/15.
 */
public class ResolvedPartLink {

    private PartIteration partIteration;
    private PartLink partLink;

    public ResolvedPartLink(PartIteration partIteration, PartLink partLink) {
        this.partIteration = partIteration;
        this.partLink = partLink;
    }

    public PartIteration getPartIteration() {
        return partIteration;
    }

    public void setPartIteration(PartIteration partIteration) {
        this.partIteration = partIteration;
    }

    public PartLink getPartLink() {
        return partLink;
    }

    public void setPartLink(PartLink partLink) {
        this.partLink = partLink;
    }
}
