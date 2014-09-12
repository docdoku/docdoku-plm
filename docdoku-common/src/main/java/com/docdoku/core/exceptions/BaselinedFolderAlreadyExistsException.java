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

package com.docdoku.core.exceptions;

import com.docdoku.core.document.baseline.BaselinedFolder;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Exception throw when you try to create a folder in a baseline which already have it.
 *
 * @author Taylor LABEJOF
 * @version 2.0, 05/09/14
 * @since   V2.0
 */
public class BaselinedFolderAlreadyExistsException extends ApplicationException {
    private final BaselinedFolder mFolder;

    public BaselinedFolderAlreadyExistsException(String pMessage) {
        super(pMessage);
        mFolder = null;
    }

    public BaselinedFolderAlreadyExistsException(Locale pLocale, BaselinedFolder pFolder) {
        this(pLocale, pFolder, null);
    }

    public BaselinedFolderAlreadyExistsException(Locale pLocale, BaselinedFolder pFolder, Throwable pCause) {
        super(pLocale, pCause);
        mFolder=pFolder;
    }

    @Override
    public String getLocalizedMessage() {
        String message = getBundleDefaultMessage();
        return MessageFormat.format(message, mFolder);
    }

}
