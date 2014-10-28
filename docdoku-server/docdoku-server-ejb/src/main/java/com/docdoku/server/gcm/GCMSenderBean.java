package com.docdoku.server.gcm;

import com.docdoku.core.document.DocumentRevision;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


@Local(IGCMSenderLocal.class)
@Stateless(name = "GCMSenderBean")
public class GCMSenderBean implements IGCMSenderLocal {

    private static final String GCM_URL = "https://android.googleapis.com/gcm/send";
    private static final String CONF_PROPERTIES = "/com/docdoku/server/gcm/gcm.properties";
    private static final Properties CONF = new Properties();
    private static final Logger LOGGER = Logger.getLogger(GCMSenderBean.class.getName());

    @EJB
    private IUserManagerLocal userManager;

    static {
        InputStream inputStream = null;
        try {
            inputStream = GCMSenderBean.class.getResourceAsStream(CONF_PROPERTIES);
            CONF.load(inputStream);
        } catch (IOException e) {
            Logger.getLogger(GCMSenderBean.class.getName()).log(Level.WARNING, null, e);
        } finally {
            try{if(inputStream!=null){
                    inputStream.close();
            }}catch (IOException ignored){}
        }
    }

    @Override
    @Asynchronous
    public void sendStateNotification(GCMAccount[] pGCGcmAccounts, DocumentRevision documentRevision) {
        for(GCMAccount gcmAccount:pGCGcmAccounts){
            sendStateNotification(gcmAccount,documentRevision);
        }
    }

    @Override
    @Asynchronous
    public void sendIterationNotification(GCMAccount[] pGCGcmAccounts, DocumentRevision documentRevision) {
        for(GCMAccount gcmAccount:pGCGcmAccounts){
            sendIterationNotification(gcmAccount, documentRevision);
        }
    }

    private void sendStateNotification(GCMAccount gcmAccount, DocumentRevision documentRevision){
        JsonObjectBuilder data = Json.createObjectBuilder()
        .add("type","stateNotification")
        .add("workspaceId",documentRevision.getWorkspaceId())
        .add("documentMasterId",documentRevision.getId())
        .add("documentMasterVersion",documentRevision.getVersion())
        .add("documentMasterIteration", documentRevision.getLastIteration().getIteration())
        .add("hashCode", documentRevision.hashCode());
        LOGGER.info("gcm Sender : Sending state notification for the document " + documentRevision.getLastIteration());
        sendMessage(data.build(),gcmAccount);
    }


    private void sendIterationNotification(GCMAccount gcmAccount, DocumentRevision documentRevision) {
        JsonObjectBuilder data = Json.createObjectBuilder()
        .add("type","iterationNotification")
        .add("workspaceId",documentRevision.getWorkspaceId())
        .add("documentMasterId",documentRevision.getId())
        .add("documentMasterVersion",documentRevision.getVersion())
        .add("documentMasterIteration",documentRevision.getLastIteration().getIteration())
        .add("hashCode", documentRevision.hashCode());
        LOGGER.info("gcm Sender : Sending iteration notification for the document " + documentRevision.getLastIteration());
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
