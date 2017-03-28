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

package com.docdoku.core.services;

import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.query.PartSearchQuery;

import java.util.List;

public interface IIndexerManagerLocal {

    void createWorkspaceIndex(String workspaceId) throws AccountNotFoundException, NotAllowedException;

    void deleteWorkspaceIndex(String workspaceId) throws AccountNotFoundException;

    void indexDocumentIteration(DocumentIteration documentIteration);

    void indexDocumentIterations(List<DocumentIteration> documentIterations);

    void indexPartIteration(PartIteration partIteration);

    void indexPartIterations(List<PartIteration> partIterations);

    void removeDocumentIterationFromIndex(DocumentIteration documentIteration);

    void removePartIterationFromIndex(PartIteration partIteration);

    List<DocumentRevision> searchDocumentRevisions(DocumentSearchQuery documentSearchQuery, int from, int size) throws AccountNotFoundException, NotAllowedException;

    List<PartRevision> searchPartRevisions(PartSearchQuery partSearchQuery, int from, int size) throws AccountNotFoundException, NotAllowedException;

    void indexAllWorkspacesData();

    void indexWorkspaceData(String workspaceId) throws AccountNotFoundException;

}
