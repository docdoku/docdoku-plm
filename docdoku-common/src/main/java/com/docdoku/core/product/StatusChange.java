package com.docdoku.core.product;

import com.docdoku.core.common.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by kelto on 20/07/15.
 */
@Embeddable
public class StatusChange implements Serializable{


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="USER_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name="USER_WORKSPACE",referencedColumnName = "WORKSPACE_ID")})
    private User statusChangeAuthor;

    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private java.util.Date statusModificationDate;

    public java.util.Date getStatusModificationDate() {
        return statusModificationDate;
    }

    public void setStatusModificationDate(Date statusModificationDate) {
        this.statusModificationDate = statusModificationDate;
    }

    public User getStatusChangeAuthor() {
        return statusChangeAuthor;
    }

    public void setStatusChangeAuthor(User statusChangeAuthor) {
        this.statusChangeAuthor = statusChangeAuthor;
    }

}
