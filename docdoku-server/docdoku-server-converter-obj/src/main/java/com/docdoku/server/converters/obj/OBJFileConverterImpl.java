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

package com.docdoku.server.converters.obj;

import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.*;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.google.common.io.Files;

import javax.ejb.EJB;
import java.io.File;
import java.io.IOException;
import java.util.Properties;


@OBJFileConverter
public class OBJFileConverterImpl implements CADConverter{

    private final static String PYTHON_SCRIPT="/com/docdoku/server/converters/obj/convert_obj_three.py";
    private final static String CONF_PROPERTIES="/com/docdoku/server/converters/obj/conf.properties";
    private final static Properties CONF = new Properties();

    @EJB
    private IProductManagerLocal productService;

    static{
        try {
            CONF.load(OBJFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File convert(PartIteration partToConvert, File cadFile) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException {
        String woExName=FileIO.getFileNameWithoutExtension(cadFile);
        File tempDir = Files.createTempDir();
        File tmpJSFile=new File(tempDir, woExName+".js");
        File tmpBINFile = new File(tmpJSFile.getParentFile(), woExName + ".bin");
        File jsFile=null;
        try {
            String pythonInterpreter = CONF.getProperty("pythonInterpreter");
            File script = new File(OBJFileConverterImpl.class.getResource(PYTHON_SCRIPT).getPath());
            String[] args = {pythonInterpreter, script.getAbsolutePath(), "-t" ,"binary", "-a", "center", "-i", cadFile.getAbsolutePath(),"-o",tmpJSFile.getAbsolutePath()};
            ProcessBuilder pb = new ProcessBuilder(args);
            Process proc = pb.start();
            //Process proc = Runtime.getRuntime().exec(args);
            proc.waitFor();
            int exitCode = proc.exitValue();
            if(exitCode==0){
                PartIterationKey partIPK = partToConvert.getKey();
                File binFile = productService.saveFileInPartIteration(partIPK, woExName + ".bin", tmpBINFile.length());
                Files.copy(tmpBINFile,binFile);

                jsFile = productService.saveGeometryInPartIteration(partIPK, woExName+".js", 0, tmpJSFile.length());
                Files.copy(tmpJSFile,jsFile);
            }
            return jsFile;
        }
        finally {
            if(tmpJSFile!=null)
                tmpJSFile.delete();

            if(tmpBINFile!=null)
                tmpBINFile.delete();
        }
    }



    @Override
    public boolean canConvertToJSON(String cadFileExtension) {
        return "obj".equalsIgnoreCase(cadFileExtension);
    }
}
