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


package com.docdoku.core.configuration;

import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A <a href="ConfigSpec.html">ConfigSpec</a> which selects the latest released iteration.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 29/08/14
 * @since   V2.0
 */
@Table(name="LATESTRELEASEDCONFIGSPEC")
@Entity
public class LatestReleasedConfigSpec extends ConfigSpec {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User user;

    public LatestReleasedConfigSpec() {
    }

    public LatestReleasedConfigSpec(User user) {
        this.user = user;
    }

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    @Override
    public PartIteration filterConfigSpec(PartMaster part) {
        return part.getLastReleasedRevision().getLastIteration();
    }

    @Override
    public DocumentIteration filterConfigSpec(DocumentMaster documentMaster) {
        return null;
    }
}