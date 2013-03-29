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
package com.docdoku.server.postuploaders;

import com.docdoku.core.util.FileIO;
import com.google.common.io.Files;

import java.io.File;

@ScormPostUploader
public class ScormPostUploaderImpl implements DocumentPostUploader {

    @Override
    public void process(File file) throws Exception {
        String docExName=FileIO.getFileNameWithoutExtension(file);
        File tempDir = Files.createTempDir();
        File archiveFolder = new File(tempDir,docExName);
        archiveFolder.mkdir();
        FileIO.unzipArchive(file, archiveFolder);
        if (new File(archiveFolder, "imsmanifest.xml").exists()) {
            File targetFile = new File(file.getAbsolutePath().replace(file.getName(),"") +  "scorm/" + docExName);
            targetFile.mkdirs();
            Files.move(archiveFolder, targetFile);
        }
    }

    @Override
    public boolean canProcess(String fileName) {
        return FileIO.isArchiveFile(fileName);
    }
}
