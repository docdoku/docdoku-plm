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
package com.docdoku.server.rest.exceptions;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Taylor LABEJOF
 */
public class NotModifiedException extends RestApiException  {
    private static final int CACHE_SECOND = 60 * 60 * 24;

    private final String eTag;

    public NotModifiedException(String eTag) {
        super();
        this.eTag = eTag;
    }

    public String getETag() {
        return eTag;
    }
    public Date getExpireDate(){
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.add(Calendar.SECOND, CACHE_SECOND);
        return expirationDate.getTime();
    }
}
