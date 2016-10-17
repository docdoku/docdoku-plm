package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Morgan Guimard
 */
@XmlRootElement
@ApiModel(value="DiskUsageSpaceDTO", description="This class provides storage information")
public class DiskUsageSpaceDTO implements Serializable {

    private long documents;
    private long parts;
    private long documentTemplates;
    private long partTemplates;

    public DiskUsageSpaceDTO() {
    }

    public long getDocuments() {
        return documents;
    }

    public void setDocuments(long documents) {
        this.documents = documents;
    }

    public long getParts() {
        return parts;
    }

    public void setParts(long parts) {
        this.parts = parts;
    }

    public long getDocumentTemplates() {
        return documentTemplates;
    }

    public void setDocumentTemplates(long documentTemplates) {
        this.documentTemplates = documentTemplates;
    }

    public long getPartTemplates() {
        return partTemplates;
    }

    public void setPartTemplates(long partTemplates) {
        this.partTemplates = partTemplates;
    }
}
