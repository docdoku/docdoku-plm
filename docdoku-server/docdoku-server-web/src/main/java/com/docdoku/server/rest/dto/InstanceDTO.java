package com.docdoku.server.rest.dto;

import java.io.Serializable;
import java.util.List;

/**
 * User: jmaffre
 * Date: 13/11/12
 * Time: 12:51
 */
public class InstanceDTO implements Serializable {

    /**
     * Id of part iteration
     */
    private String partIterationId;

    /**
     * Absolute translation on x axis.
     */
    private double tx;

    /**
     * Absolute translation on y axis.
     */
    private double ty;

    /**
     * Absolute translation on z axis.
     */
    private double tz;

    /**
     * Absolute radian orientation on x axis.
     */
    private double rx;

    /**
     * Absolute radian orientation on y axis.
     */
    private double ry;

    /**
     * Absolute radian orientation on z axis.
     */
    private double rz;

    /**
     * List of geometry files with their qualities
     */
    private List<GeometryDTO> files;

    private List<InstanceAttributeDTO> attributes;

    public InstanceDTO() {}

    public InstanceDTO(String partIterationId, double tx, double ty, double tz, double rx, double ry, double rz, List<GeometryDTO> files, List<InstanceAttributeDTO> attributes) {
        this.partIterationId = partIterationId;
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.files = files;
        this.attributes = attributes;
    }

    public String getPartIterationId() {
        return partIterationId;
    }

    public void setPartIterationId(String partIterationId) {
        this.partIterationId = partIterationId;
    }

    public double getTx() {
        return tx;
    }

    public void setTx(double tx) {
        this.tx = tx;
    }

    public double getTy() {
        return ty;
    }

    public void setTy(double ty) {
        this.ty = ty;
    }

    public double getTz() {
        return tz;
    }

    public void setTz(double tz) {
        this.tz = tz;
    }

    public double getRx() {
        return rx;
    }

    public void setRx(double rx) {
        this.rx = rx;
    }

    public double getRy() {
        return ry;
    }

    public void setRy(double ry) {
        this.ry = ry;
    }

    public double getRz() {
        return rz;
    }

    public void setRz(double rz) {
        this.rz = rz;
    }

    public List<GeometryDTO> getFiles() {
        return files;
    }

    public void setFiles(List<GeometryDTO> files) {
        this.files = files;
    }

    public List<InstanceAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<InstanceAttributeDTO> attributes) {
        this.attributes = attributes;
    }

}
