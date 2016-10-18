package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Morgan Guimard
 */
@XmlRootElement
@ApiModel(value="DiskUsageSpaceDTO", description="This class provides storage information")
public class DiskUsageSpaceDTO implements Serializable {

    @ApiModelProperty(value = "Storage size for document files")
    private long documents;

    @ApiModelProperty(value = "Storage size for part files")
    private long parts;

    @ApiModelProperty(value = "Storage size for documentTemplates files")
    private long documentTemplates;

    @ApiModelProperty(value = "Storage size for partTemplates files")
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
