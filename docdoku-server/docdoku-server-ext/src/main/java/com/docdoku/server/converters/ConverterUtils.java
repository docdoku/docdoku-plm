/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.server.converters;


import java.io.*;

/**
 * This ConverterUtils class expose util methods around files conversion
 */
public class ConverterUtils {

    private ConverterUtils() {
    }

    /**
     * Returns input stream content as String.
     * Use it to get info and error messages from process output
     *
     * @param is an InputStream
     * @return the representation as String
     */
    public static String getOutput(InputStream is) throws IOException {
        StringBuilder output = new StringBuilder();
        String line;
        try (InputStreamReader isr = new InputStreamReader(is, "UTF-8"); BufferedReader br = new BufferedReader(isr)) {
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

}
