package com.docdoku.server.rest.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class IterationNoteDTO implements Serializable {
    private String iterationNote;

    public IterationNoteDTO() {
    }

    public IterationNoteDTO(String iterationNote) {
        this.iterationNote = iterationNote;
    }

    public String getIterationNote() {
        return iterationNote;
    }

    public void setIterationNote(String iterationNote) {
        this.iterationNote = iterationNote;
    }
}
