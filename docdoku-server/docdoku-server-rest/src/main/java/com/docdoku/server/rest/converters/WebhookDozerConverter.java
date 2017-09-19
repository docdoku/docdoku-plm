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

package com.docdoku.server.rest.converters;

import com.docdoku.core.hooks.SNSWebhookApp;
import com.docdoku.core.hooks.SimpleWebhookApp;
import com.docdoku.core.hooks.Webhook;
import com.docdoku.core.hooks.WebhookApp;
import com.docdoku.server.rest.dto.WebhookAppParameterDTO;
import com.docdoku.server.rest.dto.WebhookDTO;
import org.dozer.DozerConverter;

import java.util.ArrayList;
import java.util.List;


public class WebhookDozerConverter extends DozerConverter<Webhook, WebhookDTO> {


    public WebhookDozerConverter() {
        super(Webhook.class, WebhookDTO.class);
    }

    @Override
    public WebhookDTO convertTo(Webhook webhook, WebhookDTO webhookDTO) {

        if (webhook != null) {
            List<WebhookAppParameterDTO> parameters=new ArrayList<>();
            webhookDTO = new WebhookDTO(webhook.getId(),webhook.getName(), webhook.isActive(),parameters,webhook.getAppName());

            WebhookApp app = webhook.getWebhookApp();
            if(app instanceof SimpleWebhookApp){
                SimpleWebhookApp simpleApp = (SimpleWebhookApp)app;
                if(simpleApp.getMethod()!=null)
                    parameters.add(new WebhookAppParameterDTO("method", simpleApp.getMethod()));
                if(simpleApp.getUri()!=null)
                    parameters.add(new WebhookAppParameterDTO("uri", simpleApp.getUri()));
                if(simpleApp.getAuthorization()!=null)
                    parameters.add(new WebhookAppParameterDTO("authorization", simpleApp.getAuthorization()));

            }else if(app instanceof SNSWebhookApp){
                SNSWebhookApp snsApp = (SNSWebhookApp)app;
                if(snsApp.getTopicArn()!=null)
                    parameters.add(new WebhookAppParameterDTO("topicArn", snsApp.getTopicArn()));
                if(snsApp.getRegion()!=null)
                    parameters.add(new WebhookAppParameterDTO("region", snsApp.getRegion()));
                if(snsApp.getAwsAccount()!=null)
                    parameters.add(new WebhookAppParameterDTO("awsAccount", snsApp.getAwsAccount()));
                if(snsApp.getAwsSecret()!=null)
                    parameters.add(new WebhookAppParameterDTO("awsSecret", snsApp.getAwsSecret()));
            }

            return webhookDTO;
        }

        return null;
    }

    @Override
    public Webhook convertFrom(WebhookDTO webhookDTO, Webhook webhook) {
        return webhook;
    }

}
