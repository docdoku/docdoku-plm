package com.docdoku.server.hooks;

import com.docdoku.core.hooks.Webhook;

public class SimpleWebhookRunner implements WebhookRunner {

    public SimpleWebhookRunner() {
    }

    @Override
    public void run(Webhook webhook, String email, String name, String subject, String content) {

    }
}
