package com.docdoku.server.hooks;


import com.docdoku.core.hooks.Webhook;

public class SNSWebhookRunner implements WebhookRunner {

    public SNSWebhookRunner() {
    }

    @Override
    public void run(Webhook webhook, String email, String name, String subject, String content) {
    }

}
