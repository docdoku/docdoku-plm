package com.docdoku.server.rest.dto;


public class PartUsageLinkDTO {

    private int id;
    private double amount;
    private String comment;
    private ComponentDTO component;
    private String referenceDescription;
    private String unit;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ComponentDTO getComponent() {
        return component;
    }

    public void setComponent(ComponentDTO component) {
        this.component = component;
    }

    public String getReferenceDescription() {
        return referenceDescription;
    }

    public void setReferenceDescription(String referenceDescription) {
        this.referenceDescription = referenceDescription;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
