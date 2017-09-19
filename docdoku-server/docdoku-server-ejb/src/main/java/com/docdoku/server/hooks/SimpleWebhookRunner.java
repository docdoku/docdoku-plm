package com.docdoku.server.hooks;

import com.docdoku.core.hooks.SimpleWebhookApp;
import com.docdoku.core.hooks.Webhook;
import com.docdoku.server.converters.ConverterUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleWebhookRunner implements WebhookRunner {

    private static final Logger LOGGER = Logger.getLogger(SimpleWebhookRunner.class.getName());

    public SimpleWebhookRunner() {
    }

    @Override
    public void run(Webhook webhook, String login, String email, String name, String subject, String content) {

        SimpleWebhookApp webhookApp = (SimpleWebhookApp) webhook.getWebhookApp();
        String method = webhookApp.getMethod();
        String uri = webhookApp.getUri();
        String authorization = webhookApp.getAuthorization();

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpRequestBase request;

        try {

            switch (method) {
                case "POST":
                    request = new HttpPost(uri);
                    ((HttpPost) request).setEntity(getEntity(login, email, name, subject, content));
                    break;
                case "PUT":
                    request = new HttpPut(uri);
                    ((HttpPut) request).setEntity(getEntity(login, email, name, subject, content));
                    break;
                default:
                    LOGGER.log(Level.SEVERE, "Unsupported method " + method);
                    return;
            }

            request.addHeader("authorization", authorization);
            HttpResponse response = httpClient.execute(request);
            try (InputStream is = response.getEntity().getContent()) {
                String s = ConverterUtils.inputStreamToString(is);
                LOGGER.log(Level.INFO, "Webhook response status " + response.getStatusLine() + " \n\t " + s);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unsupported method " + method);
        } finally {
            LOGGER.log(Level.SEVERE, "Webhook runner terminated");
        }

    }

    public HttpEntity getEntity(String login, String email, String name, String subject, String content) throws UnsupportedEncodingException {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add("login", login);
        objectBuilder.add("email", email);
        objectBuilder.add("name", name);
        objectBuilder.add("subject", subject);
        objectBuilder.add("content", content);
        return new StringEntity(objectBuilder.build().toString());
    }
}
