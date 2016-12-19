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

package com.docdoku.cli.commands.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TestConfig {

    public static String HOST;
    public static String LOGIN;
    public static String PASSWORD;
    public static String WORKSPACE;
    public static String PORT;
    public static boolean SSL;

    static {
        HOST = System.getProperty("host") != null ? System.getProperty("host") : "localhost";
        PORT = System.getProperty("port") != null ? System.getProperty("port") : "8080";
        LOGIN = System.getProperty("login") != null ? System.getProperty("login") : "test";
        PASSWORD = System.getProperty("password") != null ? System.getProperty("password") : "test";
        WORKSPACE = System.getProperty("workspace") != null ? System.getProperty("workspace") : "test";
        SSL = System.getProperty("ssl") != null ? Boolean.valueOf(System.getProperty("ssl")) : false;
    }

    public static String[] getAuth() {

        List<String> args = new ArrayList<>();
        if (SSL) {
            args.add("--ssl");
        }

        return Stream.concat(
                args.stream(),
                Arrays.stream(new String[]{
                        "-u",
                        TestConfig.LOGIN,
                        "-p",
                        TestConfig.PASSWORD,
                        "-h",
                        TestConfig.HOST,
                        "-P",
                        TestConfig.PORT,
                        "-F",
                        "json"
                })
        ).toArray(String[]::new);

    }
}
