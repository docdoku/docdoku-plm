package com.docdoku.server.rest.dto;

import com.docdoku.server.rest.PartListDTO;

import java.util.List;

/**
 * Created by morgan on 09/04/15.
 */
public class QueryResultDTO {

    List<PartListDTO> partLists;

    public QueryResultDTO() {
    }

    public List<PartListDTO> getPartLists() {
        return partLists;
    }

    public void setPartLists(List<PartListDTO> partLists) {
        this.partLists = partLists;
    }
}
