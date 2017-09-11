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

package com.docdoku.core.notification;

import com.docdoku.core.common.Workspace;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author Morgan Guimard
 */
@Table(name = "NOTIFICATIONOPTIONS")
@Entity
public class NotificationOptions implements Serializable {

    public static final int UNIQUE_ID = 1;
    private static final boolean SEND_EMAILS_DEFAULT = true;

    @Id
    @OneToOne
    private Workspace workspace;

    private boolean sendEmails;

    public NotificationOptions() {
    }

    public NotificationOptions(Workspace workspace) {
        this.workspace = workspace;
        this.sendEmails = SEND_EMAILS_DEFAULT;
    }

    public NotificationOptions(Workspace workspace, boolean sendEmails) {
        this.workspace = workspace;
        this.sendEmails = sendEmails;
    }

    public static int getUniqueId() {
        return UNIQUE_ID;
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
