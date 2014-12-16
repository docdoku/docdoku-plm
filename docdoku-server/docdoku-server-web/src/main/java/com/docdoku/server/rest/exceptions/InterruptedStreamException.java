/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Taylor LABEJOF
 */
public class InterruptedStreamException extends RestApiException {
    private Throwable cause;

    public InterruptedStreamException(InputStream inputStream, OutputStream outputStream, Throwable cause){
        super();
        this.cause=cause;
        try {
            inputStream.close();
            outputStream.close();
        }catch (IOException e){
            Logger.getLogger(InterruptedStreamException.class.getName()).log(Level.WARNING, null, e);
        }
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
