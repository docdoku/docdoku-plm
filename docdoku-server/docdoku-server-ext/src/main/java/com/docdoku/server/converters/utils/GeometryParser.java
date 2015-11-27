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

package com.docdoku.server.converters.utils;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeometryParser {

    private static final String CONF_PROPERTIES="/com/docdoku/server/converters/utils/conf.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(GeometryParser.class.getName());

    private GeometryParser() {
    }

    public static double[] calculateBox(File file) {

        try (InputStream inputStream = GeometryParser.class.getResourceAsStream(CONF_PROPERTIES)){

            CONF.load(inputStream);

            String nodeServerUrl = CONF.getProperty("nodeServerUrl");

            URL url = new URL(nodeServerUrl+"/box");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            JsonObjectBuilder body = Json.createObjectBuilder().add("filename",file.getAbsolutePath());
            con.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            wr.write(body.build().toString());
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String result = response.toString();

            JsonObject box = Json.createReader(new StringReader(result)).readObject();

            JsonObject min = box.getJsonObject("min");
            JsonObject max = box.getJsonObject("max");


            return new double[]{
                    min.getJsonNumber("x").doubleValue(),
                    min.getJsonNumber("y").doubleValue(),
                    min.getJsonNumber("z").doubleValue(),
                    max.getJsonNumber("x").doubleValue(),
                    max.getJsonNumber("y").doubleValue(),
                    max.getJsonNumber("z").doubleValue()
            };

        } catch (IOException | JsonException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }

        return new double[6];
    }
}
