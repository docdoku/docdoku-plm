/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.gwt.explorer.shared;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Florent GARIN
 */
public abstract class SearchQueryDTO {


    public static abstract class AbstractAttributeQueryDTO implements Serializable{
        protected String name;

        public String getName() {
            return name;
        }
        public AbstractAttributeQueryDTO(){}
        public AbstractAttributeQueryDTO(String name){
            this.name=name;
        }
    }
    public static class TextAttributeQueryDTO extends AbstractAttributeQueryDTO{
        private String textValue;
        public TextAttributeQueryDTO(){}
        public TextAttributeQueryDTO(String name, String value){
            super(name);
            this.textValue=value;
        }
        public String getTextValue(){
            return textValue;
        }
    }
    public static class NumberAttributeQueryDTO extends AbstractAttributeQueryDTO{
        private float numberValue;
        public NumberAttributeQueryDTO(){}
        public NumberAttributeQueryDTO(String name, float value){
            super(name);
            numberValue=value;
        }
        public float getNumberValue(){
            return numberValue;
        }
    }
    public static class BooleanAttributeQueryDTO extends AbstractAttributeQueryDTO{
        private boolean booleanValue;
        public BooleanAttributeQueryDTO(){}
        public BooleanAttributeQueryDTO(String name, boolean value){
            super(name);
            booleanValue=value;
        }
        public boolean isBooleanValue(){
            return booleanValue;
        }
    }
    public static class URLAttributeQueryDTO extends AbstractAttributeQueryDTO{
        private String urlValue;
        public URLAttributeQueryDTO(){}
        public URLAttributeQueryDTO(String name, String value){
            super(name);
            urlValue=value;
        }
        public String getUrlValue(){
            return urlValue;
        }
    }
    public static class DateAttributeQueryDTO extends AbstractAttributeQueryDTO{
        private Date fromDate;
        private Date toDate;
        public DateAttributeQueryDTO(){}
        public DateAttributeQueryDTO(String name, Date fromDate, Date toDate){
            super(name);
            this.fromDate=fromDate;
            this.toDate=toDate;
        }

        public Date getFromDate() {
            return fromDate;
        }

        public Date getToDate() {
            return toDate;
        }

    }
}
