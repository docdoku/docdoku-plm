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
package com.docdoku.core.gcm;

import com.docdoku.core.common.Account;

import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Table(name="GCMACCOUNT")
@javax.persistence.Entity
public class GCMAccount implements Serializable {

    @Id
    @OneToOne
    private Account account;

    @NotNull
    private String gcmId;

    public GCMAccount() {
    }

    public GCMAccount(Account account, String gcmId) {
        this.account = account;
        this.gcmId = gcmId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getGcmId() {
        return gcmId;
    }

    public void setGcmId(String gcmId) {
        this.gcmId = gcmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GCMAccount that = (GCMAccount) o;
        return account.equals(that.account) && gcmId.equals(that.gcmId);
    }

    @Override
    public int hashCode() {
        int result = account.hashCode();
        result = 31 * result + gcmId.hashCode();
        return result;
    }
}
