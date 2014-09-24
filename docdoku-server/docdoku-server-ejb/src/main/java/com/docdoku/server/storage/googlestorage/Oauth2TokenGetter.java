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

package com.docdoku.server.storage.googlestorage;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Asmae Chadid
 */
public class Oauth2TokenGetter {


    private HttpTransport HTTP_TRANSPORT;
    private JsonFactory JSON_FACTORY;

    private GoogleStorageProperties properties = new GoogleStorageProperties();

    public Oauth2TokenGetter() {
        try {
            JSON_FACTORY = new JacksonFactory();
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            e.getMessage();
        }
    }


    private void generateAuthorisationAccess() {

        AuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, properties.getClientId(), properties.getClientSecret(), Arrays.asList(properties.getScope()))
                .setAccessType("offline")
                .setApprovalPrompt("force").build();

        AuthorizationCodeRequestUrl authorizationCodeRequestUrl = flow.newAuthorizationUrl();
        authorizationCodeRequestUrl.setRedirectUri(properties.getRedirectUri()).build();
        authorizationCodeRequestUrl.setRedirectUri(properties.getRedirectUri()).build();
    }

    private void generateTokenResponse() throws IOException {
        String authorisationCode = properties.getAuthorisationCode();

        AuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, properties.getClientId(), properties.getClientSecret(), Arrays.asList(properties.getScope()))
                .setAccessType("offline")
                .setApprovalPrompt("force").build();

        flow.newTokenRequest(authorisationCode).setRedirectUri(properties.getRedirectUri()).execute();
    }

    public String getToken() throws IOException{

        Credential credential = new GoogleCredential.Builder().setJsonFactory(new JacksonFactory())
                .setClientSecrets(properties.getClientId(), properties.getClientSecret())
                .setTransport(HTTP_TRANSPORT).build();

        credential
                .setExpiresInSeconds(3600l)
                .setRefreshToken(properties.getRefreshToken());

        credential.refreshToken();
        return credential.getAccessToken();
    }


}
