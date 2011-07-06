/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.gwt.explorer.client;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

/**
 *
 * @author Florent Garin
 */
public class NewTemplatePlace extends Place{
   public String newTemplateName;
    
    public NewTemplatePlace(String token){
        this.newTemplateName = token;
    }
    
    public String getNewTemplateName(){
       return newTemplateName; 
    }
    
    public static class Tokenizer implements PlaceTokenizer<NewTemplatePlace> {
         
        public Tokenizer(){
            super();
        }
        
        @Override
        public NewTemplatePlace getPlace(String token) {
            return new NewTemplatePlace(token);
        }

        @Override
        public String getToken(NewTemplatePlace p) {
            return p.getNewTemplateName();
        }
        
    }    
    
}
