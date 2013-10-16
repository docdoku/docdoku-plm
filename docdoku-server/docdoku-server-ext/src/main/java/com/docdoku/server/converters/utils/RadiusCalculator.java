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

package com.docdoku.server.converters.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class RadiusCalculator {

    private final static String CONF_PROPERTIES="/com/docdoku/server/converters/utils/conf.properties";
    private final static Properties CONF = new Properties();

    public RadiusCalculator() {
    }

    public static double calculateRadius(File file){
        try{
            CONF.load(RadiusCalculator.class.getResourceAsStream(CONF_PROPERTIES));
            String nodeServerUrl = CONF.getProperty("nodeServerUrl");

            URL url = new URL(nodeServerUrl+"/radius");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            body.put("filename",file.getAbsolutePath());

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(body.toString());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String result = response.toString();

            JSONObject jsonResponse = new JSONObject(result);
            double radius = Double.parseDouble((String) jsonResponse.get("radius"));

            return radius;

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
