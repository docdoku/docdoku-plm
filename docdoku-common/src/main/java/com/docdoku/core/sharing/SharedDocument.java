package com.docdoku.core.sharing;


import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentMaster;

import javax.persistence.*;
import java.util.Date;

@Table(name="SHAREDDOCUMENT")
@Entity
@NamedQueries({
        @NamedQuery(name="SharedDocument.findDocumentMasterByWorkspace", query="SELECT sd.documentMaster FROM SharedDocument sd WHERE sd.documentMaster.workspaceId = :workspaceId")
})
public class SharedDocument extends SharedEntity{

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "DOCUMENTMASTER_ID", referencedColumnName = "ID"),
            @JoinColumn(name = "DOCUMENTMASTER_VERSION", referencedColumnName = "VERSION"),
            @JoinColumn(name = "ENTITY_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private DocumentMaster documentMaster;

    public SharedDocument(){
    }

    public SharedDocument(Workspace workspace, User author, Date expireDate, String password, DocumentMaster documentMaster) {
        super(workspace, author, expireDate, password);
        this.documentMaster = documentMaster;
    }

    public SharedDocument(Workspace workspace, User author, DocumentMaster documentMaster) {
        super(workspace, author);
        this.documentMaster = documentMaster;
    }

    public SharedDocument(Workspace workspace, User author, Date expireDate, DocumentMaster documentMaster) {
        super(workspace, author, expireDate);
        this.documentMaster = documentMaster;
    }

    public SharedDocument(Workspace workspace, User author, String password, DocumentMaster documentMaster) {
        super(workspace, author, password);
        this.documentMaster = documentMaster;
    }

    public DocumentMaster getDocumentMaster() {
        return documentMaster;
    }

    public void setDocumentMaster(DocumentMaster documentMaster) {
        this.documentMaster = documentMaster;
    }
}
