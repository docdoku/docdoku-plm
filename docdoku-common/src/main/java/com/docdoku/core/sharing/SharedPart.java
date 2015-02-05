/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
import com.docdoku.core.product.PartRevision;

import javax.persistence.*;
import java.util.Date;

/**
 * SharedPart permits the creation of permanent link to part for users that do not have an account.
 *
 * @author Morgan Guimard
 */

@Table(name="SHAREDPART")
@Entity
@NamedQueries({
        @NamedQuery(name="SharedPart.deleteSharesForGivenPart", query="DELETE FROM SharedPart sp WHERE sp.partRevision = :pPartR")
})
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
