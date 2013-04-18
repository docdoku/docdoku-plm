package com.docdoku.core.sharing;


import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.product.PartRevision;

import javax.persistence.*;
import java.util.Date;

@Table(name="SHAREDPART")
@Entity
public class SharedPart extends SharedEntity{

    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
            @JoinColumn(name="PARTREVISION_VERSION", referencedColumnName="VERSION"),
            @JoinColumn(name="ENTITY_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private PartRevision partRevision;

    public SharedPart(){
    }

    public SharedPart(Workspace workspace, User author, Date expireDate, String password, PartRevision partRevision) {
        super(workspace, author, expireDate, password);
        this.partRevision = partRevision;
    }

    public SharedPart(Workspace workspace, User author, PartRevision partRevision) {
        super(workspace, author);
        this.partRevision = partRevision;
    }

    public SharedPart(Workspace workspace, User author, Date expireDate, PartRevision partRevision) {
        super(workspace, author, expireDate);
        this.partRevision = partRevision;
    }

    public SharedPart(Workspace workspace, User author, String password, PartRevision partRevision) {
        super(workspace, author, password);
        this.partRevision = partRevision;
    }

    public PartRevision getPartRevision() {
        return partRevision;
    }

    public void setPartRevision(PartRevision partRevision) {
        this.partRevision = partRevision;
    }
}
