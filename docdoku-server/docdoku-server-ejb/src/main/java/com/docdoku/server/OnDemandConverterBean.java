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
import com.docdoku.core.common.User;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.*;
import com.docdoku.server.converters.OnDemandConverter;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.Locale;


/**
 * Resource Getter
 */
@Stateless(name="OnDemandConverterBean")
public class OnDemandConverterBean implements IOnDemandConverterManagerLocal {

    @Inject
    @Any
    private Instance<OnDemandConverter> documentResourceGetters;

    @Inject
    private IDocumentManagerLocal documentService;

    @Inject
    private IProductManagerLocal productService;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IUserManagerLocal userManager;

    @Override
    public InputStream getDocumentConvertedResource(String outputFormat, BinaryResource binaryResource)
            throws WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException, ConvertedResourceException, WorkspaceNotEnabledException {

        DocumentIteration docI;
        Locale locale;

        if(contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)) {
            User user = userManager.whoAmI(binaryResource.getWorkspaceId());
            locale = new Locale(user.getLanguage());
        }else{
            locale = Locale.getDefault();
        }

        docI = documentService.findDocumentIterationByBinaryResource(binaryResource);

        OnDemandConverter selectedOnDemandConverter = null;
        for (OnDemandConverter onDemandConverter : documentResourceGetters) {
            if (onDemandConverter.canConvert(outputFormat, binaryResource)) {
                selectedOnDemandConverter = onDemandConverter;
                break;
            }
        }
        if (selectedOnDemandConverter != null) {
            return selectedOnDemandConverter.getConvertedResource(outputFormat, binaryResource,docI,locale);
        }

        return null;
    }

    @Override
    public InputStream getPartConvertedResource(String outputFormat, BinaryResource binaryResource)
            throws WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException, ConvertedResourceException, WorkspaceNotEnabledException {

        PartIteration partIteration;
        Locale locale;

        if(contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)) {
            User user = userManager.whoAmI(binaryResource.getWorkspaceId());
            locale = new Locale(user.getLanguage());
        }else{
            locale = Locale.getDefault();
        }

        partIteration = productService.findPartIterationByBinaryResource(binaryResource);

        OnDemandConverter selectedOnDemandConverter = null;
        for (OnDemandConverter onDemandConverter : documentResourceGetters) {
            if (onDemandConverter.canConvert(outputFormat, binaryResource)) {
                selectedOnDemandConverter = onDemandConverter;
                break;
            }
        }
        if (selectedOnDemandConverter != null) {
            return selectedOnDemandConverter.getConvertedResource(outputFormat, binaryResource, partIteration, locale);
        }

        return null;
    }

}
