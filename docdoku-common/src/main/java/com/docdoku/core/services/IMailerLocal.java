/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
package com.docdoku.core.services;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.workflow.Task;
import java.util.Collection;
import java.util.Locale;

/**
 *
 * @author Florent Garin
 */
public interface IMailerLocal {

    void sendStateNotification(User[] pSubscribers, DocumentMaster pDocumentMaster);

    void sendIterationNotification(User[] pSubscribers, DocumentMaster pDocumentMaster);

    void sendApproval(Collection<Task> pRunningTasks, DocumentMaster pDocumentMaster);

    void sendPasswordRecovery(Account account, String passwordRRUuid);

    void sendApproval(Collection<Task> runningTasks, PartRevision partRevision);
}
