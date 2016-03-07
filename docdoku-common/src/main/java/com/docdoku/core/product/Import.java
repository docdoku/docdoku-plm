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

import com.docdoku.core.common.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "IMPORT")
public class Import implements Serializable {

    @Column(length = 255)
    @Id
    private String id="";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "USER_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "USER_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User user;

    @ElementCollection
    @CollectionTable(name="IMPORT_WARNING",
            joinColumns = {
                    @JoinColumn(name = "IMPORT_ID", referencedColumnName = "ID")
            })
    private List<String> warnings ;

    @ElementCollection
    @CollectionTable(name="IMPORT_ERROR", joinColumns = {
            @JoinColumn(name = "IMPORT_ID", referencedColumnName = "ID")
    })
    private List<String> errors ;

    private String fileName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    private boolean pending;

    private boolean succeed;

    public Import() {
    }

    public Import(User user, String fileName) {
        this(user, fileName, new Date(), null, true, false);
    }

    public Import(User user, String fileName, Date startDate, Date endDate, boolean pending, boolean succeed) {
        this.user=user;
        this.fileName=fileName;
        this.id=UUID.randomUUID().toString();
        this.startDate = startDate;
        this.endDate = endDate;
        this.pending = pending;
        this.succeed = succeed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
