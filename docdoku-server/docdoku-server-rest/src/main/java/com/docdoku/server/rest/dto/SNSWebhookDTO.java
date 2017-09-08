package com.docdoku.server.rest.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@ApiModel(value = "SNSWebhookDTO", description = "This class is a representation of a {@link com.docdoku.core.hooks.SNSWebhookAPP} entity")
public class SNSWebhookDTO implements Serializable {

    @ApiModelProperty(value = "SNS topicArn")
    private String topicArn;
    @ApiModelProperty(value = "SNS region")
    private String region;
    @ApiModelProperty(value = "SNS awsAccount")
    private String awsAccount;
    @ApiModelProperty(value = "SNS awsSecret")
    private String awsSecret;

    public SNSWebhookDTO() {
    }

    public SNSWebhookDTO(String topicArn, String region, String awsAccount, String awsSecret) {
        this.topicArn = topicArn;
        this.region = region;
        this.awsAccount = awsAccount;
        this.awsSecret = awsSecret;
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
