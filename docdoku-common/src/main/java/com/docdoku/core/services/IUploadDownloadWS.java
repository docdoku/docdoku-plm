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

import java.io.IOException;
import javax.activation.DataHandler;
import javax.jws.WebService;

/**
 *
 * @author Florent Garin
 */
@WebService
public interface IUploadDownloadWS {

    DataHandler downloadFromDocument(String workspaceId, String docMId, String docMVersion, int iteration, String fileName) throws NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    DataHandler downloadFromTemplate(String workspaceId, String templateId, String fileName) throws NotAllowedException, FileNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException;
    DataHandler downloadFromPart(String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException;
    void uploadToDocument(String workspaceId, String docMId, String docMVersion, int iteration, String fileName, DataHandler data) throws IOException, CreationException, WorkspaceNotFoundException, NotAllowedException, DocumentMasterNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException;
    void uploadToTemplate(String workspaceId, String templateId, String fileName, DataHandler data) throws IOException, CreationException, WorkspaceNotFoundException, NotAllowedException, DocumentMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException;
    void uploadGeometryToPart(String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName, int quality, DataHandler data) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, IOException;
    void uploadToPart(String workspaceId, String partMNumber, String partRVersion, int iteration, String fileName, DataHandler data) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException, IOException;

}
