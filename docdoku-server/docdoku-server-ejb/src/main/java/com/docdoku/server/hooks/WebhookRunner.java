package com.docdoku.server.hooks;

import com.docdoku.core.hooks.Webhook;

public interface WebhookRunner {
    void run(Webhook webhook, String email, String name, String subject, String content);
}
