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

import com.docdoku.core.product.Import;
import com.docdoku.core.product.ImportResult;
import com.docdoku.core.services.IProductManagerLocal;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@FileImport
@Interceptor
public class FileImportInterceptor {

    @EJB
    private IProductManagerLocal productService;

    private static final Logger LOGGER = Logger.getLogger(FileImportInterceptor.class.getName());
    
    @AroundInvoke
    public Object createImport(InvocationContext ctx) throws Exception {

        Object[] parameters = ctx.getParameters();
        // TODO : check parameters before cast
        String workspaceId = (String) parameters[0];
        String originalFileName = (String) parameters[2];

        Import newImport = productService.createImport(workspaceId, originalFileName);
        String id = newImport.getId();

        Future<ImportResult> result = null;

        try{
            // Run the import
            Object proceed = ctx.proceed();
            result = (Future<ImportResult>) proceed;
            return proceed;
        }catch(Exception e){
            return null;
        }finally {
            ImportResult importResult = result != null ? result.get() : null;
            productService.endImport(workspaceId, id, importResult);
        }
        
    }

    
}
