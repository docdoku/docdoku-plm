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
import com.docdoku.server.converters.CADConverter;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.InputStream;


@OBJFileConverter
public class OBJFileConverterImpl implements CADConverter{

    private final static String PYTHON_SCRIPT="/com/docdoku/server/converters/obj/convert_obj_three.py";

    @Override
    public void convert(PartIteration partToConvert, File cadFile) {
        System.out.println("convert file from obj:" + cadFile);
        try
        {
            File script = new File(OBJFileConverterImpl.class.getResource(PYTHON_SCRIPT).getPath());
            String[] args = {"python",script.getAbsolutePath(),"-t binary", "-i " +cadFile.getAbsolutePath(),"-o /Users/flo/tmp/test.bin"};
            //System.setProperty("python.home","/System/Library/Frameworks/Python.framework/Versions/2.7/");
            //PythonInterpreter.initialize(System.getProperties(), System.getProperties(), args);
            //PythonInterpreter interp = new PythonInterpreter();
            //interp.execfile(script);
            Runtime rt = Runtime.getRuntime();
            rt.exec(args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    @Override
    public boolean canConvertToJSON(String cadFileExtension) {
        return "obj".equalsIgnoreCase(cadFileExtension);
    }
}
