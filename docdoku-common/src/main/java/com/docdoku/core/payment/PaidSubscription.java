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

package com.docdoku.core.payment;

import com.docdoku.core.common.Account;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "PAIDSUBSCRIPTION")
@Entity
public class PaidSubscription implements Serializable {


    @Id
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private Account account;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date expirationDate;



    public PaidSubscription() {

    }

    public boolean isExpired(){
        return new Date().after(expirationDate);
    }
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}