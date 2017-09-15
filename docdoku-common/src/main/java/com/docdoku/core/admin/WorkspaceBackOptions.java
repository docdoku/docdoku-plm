/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.core.admin;

import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Class that wraps setting options of a particular workspace.
 * These settings are related to back-end concerns.
 *
 * @author Morgan Guimard
 * @version 2.5, 14/09/17
 * @since V2.5
 */
@Table(name = "WORKSPACEBACKOPTIONS")
@Entity
public class WorkspaceBackOptions implements Serializable {

    private static final boolean SEND_EMAILS_DEFAULT = true;

    @Id
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;


    /**
     * Indicates that the system must not send email notifications.
     * Usually used when using a third notification system connected through
     * webhooks.
     */
    private boolean sendEmails;

    public WorkspaceBackOptions() {
    }

    public WorkspaceBackOptions(Workspace workspace) {
        this.workspace = workspace;
        this.sendEmails = SEND_EMAILS_DEFAULT;
    }

    public WorkspaceBackOptions(Workspace workspace, boolean sendEmails) {
        this.workspace = workspace;
        this.sendEmails = sendEmails;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public boolean isSendEmails() {
        return sendEmails;
    }

    public void setSendEmails(boolean sendEmails) {
        this.sendEmails = sendEmails;
    }

    public String getWorkspaceId() {
        return workspace.getId();
    }

}
