package com.docdoku.core.hooks;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "SNSWEBHOOK")
@Entity
public class SNSWebhook {

    @Id
    @OneToOne(orphanRemoval = true)
    private Webhook webhook;

    private String topicArn;
    private String region;
    private String awsAccount;
    private String awsSecret;

    public SNSWebhook(Webhook webhook, String topicArn, String region, String awsAccount, String awsSecret) {
        this.webhook = webhook;
        this.topicArn = topicArn;
        this.region = region;
        this.awsAccount = awsAccount;
        this.awsSecret = awsSecret;
    }

    public SNSWebhook() {
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