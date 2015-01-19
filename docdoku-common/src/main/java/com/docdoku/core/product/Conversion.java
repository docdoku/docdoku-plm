package com.docdoku.core.product;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Conversion implements Serializable {

    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Id
    private int id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    private boolean pending;

    private boolean succeed;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private PartIteration partIteration;

    public Conversion() {
    }

    public Conversion(PartIteration partIteration) {
        this(new Date(),true, false, partIteration);
    }

    public Conversion(Date startDate, boolean pending, boolean succeed, PartIteration partIteration) {
        this.startDate = startDate;
        this.pending = pending;
        this.succeed = succeed;
        this.partIteration = partIteration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public PartIteration getPartIteration() {
        return partIteration;
    }

    public void setPartIteration(PartIteration partIteration) {
        this.partIteration = partIteration;
    }
}
