package com.docdoku.server.gcm;

import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.services.IGCMSenderLocal;
import com.docdoku.core.services.IUserManagerLocal;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
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
        try {
            JSONObject data = new JSONObject();
            data.put("type","stateNotification");
            data.put("workspaceId",documentMaster.getWorkspaceId());
            data.put("documentMasterId",documentMaster.getId());
            data.put("documentMasterVersion",documentMaster.getVersion());
            data.put("documentMasterIteration", documentMaster.getLastIteration().getIteration());
            data.put("hashCode", documentMaster.hashCode());
            LOGGER.info("gcm Sender : Sending state notification for the document " + documentMaster.getLastIteration());
            sendMessage(data,gcmAccount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void sendIterationNotification(GCMAccount gcmAccount, DocumentMaster documentMaster) {
        try {
            JSONObject data = new JSONObject();
            data.put("type","iterationNotification");
            data.put("workspaceId",documentMaster.getWorkspaceId());
            data.put("documentMasterId",documentMaster.getId());
            data.put("documentMasterVersion",documentMaster.getVersion());
            data.put("documentMasterIteration",documentMaster.getLastIteration().getIteration());
            data.put("hashCode", documentMaster.hashCode());
            LOGGER.info("gcm Sender : Sending iteration notification for the document " + documentMaster.getLastIteration());
            sendMessage(data,gcmAccount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(JSONObject message, GCMAccount gcmAccount) {

        try{
            String apiKey = CONF.getProperty("key");

            JSONObject body = new JSONObject();
            JSONArray registrationIds = new JSONArray();
            registrationIds.put(0,gcmAccount.getGcmId());

            body.put("registration_ids",registrationIds);
            body.put("data",message);

            URL url = new URL(GCM_URL);

            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "key="+apiKey);
            con.setRequestProperty("Content-Type","application/json");

            con.setDoOutput(true);
            con.setDoInput(true);

            DataOutputStream output = new DataOutputStream(con.getOutputStream());
            output.writeBytes(body.toString());
            output.close();

            int responseCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
            LOGGER.info("gcm Sender : Response code is " + responseCode);
            LOGGER.info("gcm Sender : Response message is " + responseMessage);

        }catch(IOException e){
            LOGGER.info("gcm Sender : Failed to send message :  " + message.toString());
        } catch (JSONException e) {
            LOGGER.info("gcm Sender : JSON exception occurred :  " + message.toString());
        }

    }

}
