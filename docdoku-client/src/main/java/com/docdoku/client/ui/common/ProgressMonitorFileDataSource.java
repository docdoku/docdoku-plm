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

package com.docdoku.client.ui.common;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.FileDataSource;
import javax.swing.ProgressMonitorInputStream;

public class ProgressMonitorFileDataSource extends FileDataSource {

    private String transfertMessage;
    private Component parent;

    public ProgressMonitorFileDataSource(Component parent, File file, String transfertMessage) {
        super(file);
        this.transfertMessage = transfertMessage;
        this.parent = parent;
    }

    public ProgressMonitorFileDataSource(File file, String transfertMessage) {
        super(file);
        this.transfertMessage = transfertMessage;
    }

    public ProgressMonitorFileDataSource(String name, String transfertMessage) {
        super(name);
        this.transfertMessage = transfertMessage;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ProgressMonitorInputStream(parent, transfertMessage, super.getInputStream());
    }
}
