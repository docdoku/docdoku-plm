package com.docdoku.server.rest.dto.baseline;

import com.docdoku.core.configuration.ResolvedPartLink;
import com.docdoku.core.product.PartIteration;
import com.docdoku.server.rest.dto.LightPartLinkDTO;
import com.docdoku.server.rest.dto.PartIterationDTO;

public class ResolvedPartLinkDTO {

    private PartIterationDTO partIteration;
    private LightPartLinkDTO partLink;

    public ResolvedPartLinkDTO() {
    }

    public ResolvedPartLinkDTO(ResolvedPartLink resolvedPartLink) {
        PartIteration resolvedIteration = resolvedPartLink.getPartIteration();
        this.partIteration = new PartIterationDTO(resolvedIteration.getWorkspaceId(),resolvedIteration.getName(),resolvedIteration.getNumber(),resolvedIteration.getVersion(),resolvedIteration.getIteration());
        this.partLink = new LightPartLinkDTO(resolvedPartLink.getPartLink());
    }

    public PartIterationDTO getPartIteration() {
        return partIteration;
    }

    public void setPartIteration(PartIterationDTO partIteration) {
        this.partIteration = partIteration;
    }

    public LightPartLinkDTO getPartLink() {
        return partLink;
    }

    public void setPartLink(LightPartLinkDTO partLink) {
        this.partLink = partLink;
    }
}
