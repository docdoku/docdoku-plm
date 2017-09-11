package com.docdoku.core.hooks;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Table(name = "SNSWEBHOOKAPP")
@Entity
public class SNSWebhookApp extends WebhookApp {

    public static final String APP_NAME = "SNSWEBHOOK";

    private String topicArn;
    private String region;
    private String awsAccount;
    private String awsSecret;

    public SNSWebhookApp(String topicArn, String region, String awsAccount, String awsSecret) {
        this.topicArn = topicArn;
        this.region = region;
        this.awsAccount = awsAccount;
        this.awsSecret = awsSecret;
    }

    public SNSWebhookApp() {
    }

    @Override
    public List<WebhookAppParameter> getParameters() {
        List<WebhookAppParameter> parameters = new ArrayList<>();
        parameters.add(new WebhookAppParameter("topicArn", topicArn));
        parameters.add(new WebhookAppParameter("region", region));
        parameters.add(new WebhookAppParameter("awsAccount", awsAccount));
        parameters.add(new WebhookAppParameter("awsSecret", awsSecret));
        return parameters;
    }

    @Override
    public String getAppName() {
        return APP_NAME;
    }

    public String getTopicArn() {
        return topicArn;
    }

    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAwsAccount() {
        return awsAccount;
    }

    public void setAwsAccount(String awsAccount) {
        this.awsAccount = awsAccount;
    }

    public String getAwsSecret() {
        return awsSecret;
    }

    public void setAwsSecret(String awsSecret) {
        this.awsSecret = awsSecret;
    }
}