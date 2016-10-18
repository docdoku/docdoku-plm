package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author laurentlevan
 */
@XmlRootElement
@ApiModel(value="LightPartRevisionDTO", description="This class is a light representation of a {@link com.docdoku.core.product.PartRevision} entity")
public class LightPartRevisionDTO implements Serializable {

    private String workspaceId;
    private String partNumber;
    private String version;

    public LightPartRevisionDTO() {
    }

    public LightPartRevisionDTO(String workspaceId, String partNumber, String version){
        this.workspaceId = workspaceId;
        this.partNumber = partNumber;
        this.version = version;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
