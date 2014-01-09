package com.docdoku.server.gcm;

import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.services.IGCMSenderLocal;
import com.docdoku.core.services.IUserManagerLocal;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;


@Local(IGCMSenderLocal.class)
@Stateless(name = "GCMSenderBean")
public class GCMSenderBean implements IGCMSenderLocal {

    private final static String GCM_URL = "https://android.googleapis.com/gcm/send";
    private final static String CONF_PROPERTIES = "/com/docdoku/server/gcm/gcm.properties";
    private final static Properties CONF = new Properties();
    private final static Logger LOGGER = Logger.getLogger(GCMSenderBean.class.getName());

    @EJB
    private IUserManagerLocal userManager;

    static {
        try {
            CONF.load(GCMSenderBean.class.getResourceAsStream(CONF_PROPERTIES));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Asynchronous
    public void sendStateNotification(GCMAccount[] pGCGcmAccounts, DocumentMaster pDocumentMaster) {
        for(GCMAccount gcmAccount:pGCGcmAccounts){
            sendStateNotification(gcmAccount,pDocumentMaster);
        }
    }

    @Override
    @Asynchronous
    public void sendIterationNotification(GCMAccount[] pGCGcmAccounts, DocumentMaster pDocumentMaster) {
        for(GCMAccount gcmAccount:pGCGcmAccounts){
            sendIterationNotification(gcmAccount, pDocumentMaster);
        }
    }

    private void sendStateNotification(GCMAccount gcmAccount, DocumentMaster documentMaster){
        JsonObjectBuilder data = Json.createObjectBuilder()
        .add("type","stateNotification")
        .add("workspaceId",documentMaster.getWorkspaceId())
        .add("documentMasterId",documentMaster.getId())
        .add("documentMasterVersion",documentMaster.getVersion())
        .add("documentMasterIteration", documentMaster.getLastIteration().getIteration())
        .add("hashCode", documentMaster.hashCode());
        LOGGER.info("gcm Sender : Sending state notification for the document " + documentMaster.getLastIteration());
        sendMessage(data.build(),gcmAccount);
    }


    private void sendIterationNotification(GCMAccount gcmAccount, DocumentMaster documentMaster) {
        JsonObjectBuilder data = Json.createObjectBuilder()
        .add("type","iterationNotification")
        .add("workspaceId",documentMaster.getWorkspaceId())
        .add("documentMasterId",documentMaster.getId())
        .add("documentMasterVersion",documentMaster.getVersion())
        .add("documentMasterIteration",documentMaster.getLastIteration().getIteration())
        .add("hashCode", documentMaster.hashCode());
        LOGGER.info("gcm Sender : Sending iteration notification for the document " + documentMaster.getLastIteration());
        sendMessage(data.build(),gcmAccount);
    }

    private void sendMessage(JsonObject message, GCMAccount gcmAccount) {

        try{
            String apiKey = CONF.getProperty("key");

            JsonObjectBuilder body = Json.createObjectBuilder();
            JsonArrayBuilder registrationIds = Json.createArrayBuilder();
            registrationIds.add(gcmAccount.getGcmId());

            body.add("registration_ids", registrationIds);
            body.add("data", message);

            URL url = new URL(GCM_URL);

            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "key="+apiKey);
            con.setRequestProperty("Content-Type","application/json; charset=utf-8");

            con.setDoOutput(true);
            con.setDoInput(true);

            OutputStreamWriter output = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            output.write(body.build().toString());
            output.flush();
            output.close();

            int responseCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
            LOGGER.info("gcm Sender : Response code is " + responseCode);
            LOGGER.info("gcm Sender : Response message is " + responseMessage);

        }catch(IOException e){
            LOGGER.info("gcm Sender : Failed to send message :  " + message.toString());
        }

    }

}
