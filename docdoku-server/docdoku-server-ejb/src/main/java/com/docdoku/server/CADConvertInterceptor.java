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

import com.docdoku.core.exceptions.EntityConstraintException;
import com.docdoku.core.product.Conversion;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IProductManagerLocal;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.logging.Level;
import java.util.logging.Logger;

@CADConvert
@Interceptor
public class CADConvertInterceptor {

    @EJB
    private IProductManagerLocal productService;

    private static final Logger LOGGER = Logger.getLogger(CADConvertInterceptor.class.getName());
    
    @AroundInvoke
    public Object createConversion(InvocationContext ctx) throws Exception {
                
        PartIterationKey partIterationKey = getPartIterationKey(ctx);

        if (partIterationKey != null) {
            
            Conversion existingConversion = productService.getConversion(partIterationKey);
            
            // Don't try to convert if any conversions pending
            if(existingConversion != null && existingConversion.isPending()){
                LOGGER.log(Level.SEVERE, "Conversion already running for part iteration " + partIterationKey);
                throw new EntityConstraintException("");
            }

            // Clean old non pending conversions
            if(existingConversion != null){
                productService.removeConversion(partIterationKey);
            }
            
            // Creates the new one
            productService.createConversion(partIterationKey);
            
        }

        boolean succeed = false;
        
        try{
            // Run the conversion
            Object proceed = ctx.proceed();
            succeed = true;
            return proceed;
        }catch(Exception e){
            return null;
        }finally {
            productService.endConversion(partIterationKey,succeed);
        }
        
    }


    private PartIterationKey getPartIterationKey(InvocationContext ctx){
        if (ctx.getParameters() != null && ctx.getParameters()[0] instanceof PartIterationKey ) {
            return (PartIterationKey) ctx.getParameters()[0];
        }else{
            return null;
        }
    }

    /*
        // Needs to fetch the object that was created in an other transaction
        Conversion conversion = productService.getConversion(pPartIPK);

        if(conversion != null){
            conversion.setSucceed(succeed);
            conversion.setPending(false);
            conversion.setEndDate(new Date());
        }
*/
    
}
