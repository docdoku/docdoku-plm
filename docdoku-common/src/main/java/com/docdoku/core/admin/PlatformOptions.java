package com.docdoku.core.admin;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author Morgan Guimard
 */
@Table(name = "PLATFORMOPTIONS")
@Entity
public class PlatformOptions implements Serializable {

    public static final int UNIQUE_ID = 1 ;

    @Id
    private int id = UNIQUE_ID;

    private OperationSecurityStrategy registrationStrategy;

    private OperationSecurityStrategy workspaceCreationStrategy;

    public PlatformOptions() {
    }

    public int getId() {
        return id;
    }

    public OperationSecurityStrategy getRegistrationStrategy() {
        return registrationStrategy;
    }

    public void setRegistrationStrategy(OperationSecurityStrategy registrationStrategy) {
        this.registrationStrategy = registrationStrategy;
    }

    public OperationSecurityStrategy getWorkspaceCreationStrategy() {
        return workspaceCreationStrategy;
    }

    public void setWorkspaceCreationStrategy(OperationSecurityStrategy workspaceCreationStrategy) {
        this.workspaceCreationStrategy = workspaceCreationStrategy;
    }

    public void setDefaults() {
        registrationStrategy = OperationSecurityStrategy.NONE;
        workspaceCreationStrategy = OperationSecurityStrategy.NONE;
    }

    public enum OperationSecurityStrategy {
        NONE, ADMIN_VALIDATION
    }

}
