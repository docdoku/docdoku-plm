package com.docdoku.server.rest.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class PartIterationDTO implements Serializable {

    private String workspaceId;
    private int iteration;
    private String nativeCADFile;
    //linked documents
    //private List<String> attachedFiles;
    private String iterationNote;
    private UserDTO author;
    private Date creationDate;
    private List<InstanceAttributeDTO> instanceAttributes;

    public PartIterationDTO() {
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public String getNativeCADFile() {
        return nativeCADFile;
    }

    public void setNativeCADFile(String nativeCADFile) {
        this.nativeCADFile = nativeCADFile;
    }

    public String getIterationNote() {
        return iterationNote;
    }

    public void setIterationNote(String iterationNote) {
        this.iterationNote = iterationNote;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<InstanceAttributeDTO> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(List<InstanceAttributeDTO> instanceAttributes) {
        this.instanceAttributes = instanceAttributes;
    }
}
