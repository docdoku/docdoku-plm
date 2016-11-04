/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.server;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.ConvertedResourceException;
import com.docdoku.core.exceptions.DocumentRevisionNotFoundException;
import com.docdoku.core.exceptions.FileNotFoundException;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IPublicEntityManagerLocal;
import com.docdoku.server.dao.BinaryResourceDAO;
import com.docdoku.server.dao.DocumentRevisionDAO;
import com.docdoku.server.dao.PartRevisionDAO;
import com.docdoku.server.resourcegetters.DocumentResourceGetter;

import javax.annotation.security.DeclareRoles;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.InputStream;
import java.util.Locale;

/**
 * EJB that trusts REST layer. Provide public documents, parts and binary resources services.
 *
 * @Author Morgan Guimard
 **/

@DeclareRoles({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IPublicEntityManagerLocal.class)
@Stateless(name = "PublicEntityBean")
public class PublicEntityManagerBean implements IPublicEntityManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    @Any
    private Instance<DocumentResourceGetter> documentResourceGetters;

    @Override
    public PartRevision getPublicPartRevision(PartRevisionKey partRevisionKey) {
        PartRevision partRevision = em.find(PartRevision.class, partRevisionKey);
        if (partRevision != null && partRevision.isPublicShared()) {
            if (partRevision.isCheckedOut()) {
                em.detach(partRevision);
                partRevision.removeLastIteration();
            }
            return partRevision;
        }
        return null;
    }

    @Override
    public DocumentRevision getPublicDocumentRevision(DocumentRevisionKey documentRevisionKey) {
        DocumentRevision documentRevision = em.find(DocumentRevision.class, documentRevisionKey);
        if (documentRevision != null && documentRevision.isPublicShared()) {
            if (documentRevision.isCheckedOut()) {
                em.detach(documentRevision);
                documentRevision.removeLastIteration();
            }
            return documentRevision;
        }
        return null;
    }

    @Override
    public BinaryResource getPublicBinaryResourceForDocument(String fullName) throws FileNotFoundException {
        BinaryResource binaryResource = new BinaryResourceDAO(em).loadBinaryResource(fullName);
        String workspaceId = binaryResource.getWorkspaceId();
        String documentMasterId = binaryResource.getHolderId();
        String documentVersion = binaryResource.getHolderRevision();
        DocumentRevision documentRevision = getPublicDocumentRevision(new DocumentRevisionKey(workspaceId, documentMasterId, documentVersion));
        return documentRevision != null ? binaryResource : null;
    }

    @Override
    public BinaryResource getPublicBinaryResourceForPart(String fileName) throws FileNotFoundException {
        BinaryResource binaryResource = new BinaryResourceDAO(em).loadBinaryResource(fileName);
        String workspaceId = binaryResource.getWorkspaceId();
        String partNumber = binaryResource.getHolderId();
        String partVersion = binaryResource.getHolderRevision();
        PartRevision partRevision = getPublicPartRevision(new PartRevisionKey(workspaceId, partNumber, partVersion));
        return partRevision != null ? binaryResource : null;
    }

    @Override
    public BinaryResource getBinaryResourceForSharedPart(String fileName) throws FileNotFoundException {
        return new BinaryResourceDAO(em).loadBinaryResource(fileName);
    }

    @Override
    public BinaryResource getBinaryResourceForSharedDocument(String fileName) throws FileNotFoundException {
        return new BinaryResourceDAO(em).loadBinaryResource(fileName);
    }

    @Override
    public InputStream getPartConvertedResource(String outputFormat, BinaryResource binaryResource) throws ConvertedResourceException {

        // todo : use authenticated user locale, or request locale, or default locale.
        Locale locale = Locale.getDefault();
        String workspaceId = binaryResource.getWorkspaceId();
        String partNumber = binaryResource.getHolderId();
        String partVersion = binaryResource.getHolderRevision();
        PartRevision partRevision = getPublicPartRevision(new PartRevisionKey(workspaceId, partNumber, partVersion));
        // todo : handle risk of NPE on partRevision
        PartIteration partIteration = partRevision.getLastCheckedInIteration();
        DocumentResourceGetter selectedDocumentResourceGetter = null;
        for (DocumentResourceGetter documentResourceGetter : documentResourceGetters) {
            if (documentResourceGetter.canGetConvertedResource(outputFormat, binaryResource)) {
                selectedDocumentResourceGetter = documentResourceGetter;
                break;
            }
        }
        if (selectedDocumentResourceGetter != null) {
            return selectedDocumentResourceGetter.getConvertedResource(outputFormat, binaryResource, partIteration, locale);
        }

        return null;
    }

    @Override
    public boolean canAccess(PartIterationKey partIKey) throws PartRevisionNotFoundException {
        PartRevision partRevision = new PartRevisionDAO(em).loadPartR(partIKey.getPartRevision());
        return partRevision.isPublicShared() && partRevision.getLastCheckedInIteration().getIteration() >= partIKey.getIteration();
    }

    @Override
    public boolean canAccess(DocumentIterationKey docIKey) throws DocumentRevisionNotFoundException {
        DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(docIKey.getDocumentRevision());
        return documentRevision.isPublicShared() && documentRevision.getLastCheckedInIteration().getIteration() >= docIKey.getIteration();
    }

    @Override
    public BinaryResource getBinaryResourceForProductInstance(String fullName) throws FileNotFoundException {
        return new BinaryResourceDAO(em).loadBinaryResource(fullName);
    }

    @Override
    public BinaryResource getBinaryResourceForPathData(String fullName) throws FileNotFoundException {
        return new BinaryResourceDAO(em).loadBinaryResource(fullName);
    }

    @Override
    public InputStream getDocumentConvertedResource(String outputFormat, BinaryResource binaryResource) throws ConvertedResourceException {

        // todo : use authenticated user locale, or request locale, or default locale.
        Locale locale = Locale.getDefault();
        String workspaceId = binaryResource.getWorkspaceId();
        String documentMasterId = binaryResource.getHolderId();
        String documentVersion = binaryResource.getHolderRevision();
        DocumentRevision documentRevision = getPublicDocumentRevision(new DocumentRevisionKey(workspaceId, documentMasterId, documentVersion));
        // todo : handle risk of NPE on documentRevision
        DocumentIteration documentIteration = documentRevision.getLastCheckedInIteration();
        DocumentResourceGetter selectedDocumentResourceGetter = null;
        for (DocumentResourceGetter documentResourceGetter : documentResourceGetters) {
            if (documentResourceGetter.canGetConvertedResource(outputFormat, binaryResource)) {
                selectedDocumentResourceGetter = documentResourceGetter;
                break;
            }
        }
        if (selectedDocumentResourceGetter != null) {
            return selectedDocumentResourceGetter.getConvertedResource(outputFormat, binaryResource, documentIteration, locale);
        }

        return null;
    }
}
