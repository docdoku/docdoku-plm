/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.core.sharing;


import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentMaster;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Morgan Guimard
 */

@Table(name="SHAREDDOCUMENT")
@Entity
@NamedQueries({
        @NamedQuery(name="SharedDocument.deleteSharesForGivenDocument", query="DELETE FROM SharedDocument sd WHERE sd.documentMaster = :pDocM"),
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
