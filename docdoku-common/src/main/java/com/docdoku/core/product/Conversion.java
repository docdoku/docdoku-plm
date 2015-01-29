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

package com.docdoku.core.product;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@IdClass(PartIterationKey.class)
public class Conversion implements Serializable {

    @Id
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private PartIteration partIteration;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    private boolean pending;

    private boolean succeed;

    public Conversion() {
    }

    public Conversion(PartIteration partIteration) {
        this(new Date(), null, true, false, partIteration);
    }

    public Conversion(Date startDate, Date endDate, boolean pending, boolean succeed, PartIteration partIteration) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.pending = pending;
        this.succeed = succeed;
        this.partIteration = partIteration;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }

    public PartIteration getPartIteration() {
        return partIteration;
    }

    public void setPartIteration(PartIteration partIteration) {
        this.partIteration = partIteration;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
