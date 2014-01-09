package com.docdoku.server.rest.dto;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;


/**
 *
 * @author Julien Maffre
 */
@XmlRootElement
public class PartInstanceDTO implements Serializable {

    private String id;
    
    /**
     * Id of part iteration
     */
    private String partIterationId;

    /**
     * List of transformations to apply on the world space
     * starting from the end of the list.
     * Rotations have to be applied in the right order (rx, ry, rz) before translations.
     *
     */
    private List<TransformationDTO> transformations;

    /**
     * List of geometry files with their qualities
     */
    private List<GeometryDTO> files;

    private List<InstanceAttributeDTO> attributes;

    public PartInstanceDTO() {}

    public PartInstanceDTO(String id, String partIterationId, List<TransformationDTO> transformations, List<GeometryDTO> files, List<InstanceAttributeDTO> attributes) {
        this.id=id;
        this.partIterationId = partIterationId;
        this.transformations=transformations;
        this.files = files;
        this.attributes = attributes;
    }

    public String getPartIterationId() {
        return partIterationId;
    }

    public void setPartIterationId(String partIterationId) {
        this.partIterationId = partIterationId;
    }

    public List<TransformationDTO> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<TransformationDTO> transformations) {
        this.transformations = transformations;
    }

    public List<GeometryDTO> getFiles() {
        return files;
    }

    public void setFiles(List<GeometryDTO> files) {
        this.files = files;
    }

    public List<InstanceAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<InstanceAttributeDTO> attributes) {
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    

}
