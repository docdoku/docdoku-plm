package docDoku.DocDokuPLM;

import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 24/07/13
 * Time: 14:47
 * To change this template use File | Settings | File Templates.
 */
public class HttpPutTask extends AsyncTask<String, Void, Boolean> {
    private static String baseUrl;
    private static byte[] id;
    private HttpPostListener httpPostListener;

    public HttpPutTask(HttpPostListener httpPostListener) {
        super();
        if (baseUrl == null){
            this.baseUrl = HttpGetTask.baseUrl;
            this.id = HttpGetTask.id;
        }
        this.httpPostListener = httpPostListener;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        Boolean result = false;
        String pURL = baseUrl + strings[0];
        Log.i("docDoku.DocDokuPLM", "Sending HttpPut request to url: " + pURL);

        try {
            URL url = new URL(pURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + new String(id, "US-ASCII"));
            conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            conn.setRequestMethod("PUT");
            conn.connect();

            int responseCode = conn.getResponseCode();

            conn.disconnect();

            Log.i("docDoku.DocDokuPLM","Response code: " + responseCode);
            if (responseCode == 200){
                result = true;
            }

        } catch (MalformedURLException e) {
            Log.e("docDoku.DocDokuPLM","ERROR: MalformedURLException");
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e("docDoku.DocDokuPLM","ERROR: ProtocolException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.e("docDoku.DocDokuPLM", "ERROR: UnsupportedEncodingException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("docDoku.DocDokuPLM","ERROR: IOException");
            e.printStackTrace();
            Log.e("docDoku.DocDokuPLM", "Exception message: " + e.getMessage());
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result){
        super.onPostExecute(result);
        if (httpPostListener != null){
            httpPostListener.onHttpPostResult(result);
        }
    }
}
