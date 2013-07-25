package docDoku.DocDokuPLM;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class DocumentListActivity extends ActionBarActivity implements HttpGetListener {

    public static final String LIST_MODE_EXTRA = "list mode";
    public static final String SEARCH_QUERY_EXTRA = "search query";
    public static final int ALL_DOCUMENTS_LIST = 0;
    public static final int RECENTLY_VIEWED_DOCUMENTS_LIST = 1;
    public static final int CHECKED_OUT_DOCUMENTS_LIST = 2;
    public static final int SEARCH_RESULTS_LIST = 3;

    ListView documentListView;
    AsyncTask documentQueryTask;
    ArrayList<Document> documentList;
    DocumentArrayAdapter documentAdapter;
    private View loading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        Log.i("DocDokuPMLAndroid", "DocumentListActivity starting");

        documentListView = (ListView) findViewById(R.id.elementList);
        loading = findViewById(R.id.loading);

        Intent intent = getIntent();
        int listType = intent.getIntExtra(LIST_MODE_EXTRA, 0);
        switch(listType){
            case ALL_DOCUMENTS_LIST:
                documentQueryTask = new HttpGetTask(this).execute("workspaces/" + getCurrentWorkspace() + "/search/id=/documents/");
                break;
            case RECENTLY_VIEWED_DOCUMENTS_LIST:
                ((ViewGroup) loading.getParent()).removeView(loading);
                break;
            case CHECKED_OUT_DOCUMENTS_LIST:
                new HttpGetTask(this).execute("workspaces/" + getCurrentWorkspace() + "/checkedouts/" + getCurrentUserLogin() + "/documents/");
                break;
            case SEARCH_RESULTS_LIST:
                new HttpGetTask(this).execute("workspaces/" + getCurrentWorkspace() + "/search/" + intent.getStringExtra(SEARCH_QUERY_EXTRA) + "/documents/");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(getResources().getString(R.string.documentSearchPrompt));
        final HttpGetListener httpGetListener = this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.i("docdoku.DocDokuPLM", "Document search query launched: " + s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                documentQueryTask.cancel(true);
                documentQueryTask = new HttpGetTask(httpGetListener).execute("workspaces/" + getCurrentWorkspace() + "/search/id=" + s + "/documents/");
                Log.i("docdoku.DocDokuPLM", "Document search query changed to: " + s);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onHttpGetResult(String result) {
        if (loading !=null){
            ((ViewGroup) loading.getParent()).removeView(loading);
            loading = null;
        }
        ArrayList<Document> docsArray = new ArrayList<Document>();
        try {
            JSONArray docsJSON = new JSONArray(result);
            for (int i=0; i<docsJSON.length(); i++){
                JSONObject docJSON = docsJSON.getJSONObject(i);
                Document doc = new Document(docJSON.getString("id"));
                doc.setStateChangeNotification(docJSON.getBoolean("stateSubscription"));
                doc.setIterationNotification(docJSON.getBoolean("iterationSubscription"));
                Object reservedBy = docJSON.get("checkOutUser");
                if (reservedBy != JSONObject.NULL){
                    doc.setCheckOutUserName(((JSONObject) reservedBy).getString("name"));
                    doc.setCheckOutUserLogin(((JSONObject) reservedBy).getString("login"));
                }
                updateDocumentFromJSON(docJSON,doc);
                docsArray.add(doc);
            }
            documentListView.setAdapter(new DocumentAdapter(docsArray, this));
        } catch (JSONException e) {
            Log.e("docdoku.DocDokuPLM", "Error handling json of workspace's documents");
            e.printStackTrace();
            Log.i("docdoku.DocDokuPLM", "Error message: " + e.getMessage());
        }
    }

    private Document updateDocumentFromJSON(JSONObject documentJSON, Document document) throws JSONException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.fullDateFormat));
        document.setDocumentDetails(
                null,
                documentJSON.getJSONObject("author").getString("name"),
                dateFormat.format(new Date(Long.valueOf(documentJSON.getString("creationDate")))),
                documentJSON.getString("type"),
                documentJSON.getString("title"),
                documentJSON.getString("lifeCycleState"),
                documentJSON.getString("description")
        );
        return document;
    }

    private class DocumentAdapter extends BaseAdapter {

        private ArrayList<Document> documents;
        private LayoutInflater inflater;
        private Activity activity;

        public DocumentAdapter(ArrayList<Document> documents, Activity activity){
            this.documents = documents;
            this.activity = activity;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return documents.size();
        }

        @Override
        public Object getItem(int i) {
            return documents.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View documentRowView = inflater.inflate(R.layout.adapter_document, null);
            final Document doc = documents.get(i);
            TextView reference = (TextView) documentRowView.findViewById(R.id.reference);
            reference.setText(doc.getReference());
            TextView reservedBy = (TextView) documentRowView.findViewById(R.id.reservedBy);
            ImageView reservedDocument = (ImageView) documentRowView.findViewById(R.id.reservedDocument);
            String reservedByName = doc.getCheckOutUserName();
            if (reservedByName != null){
                String reservedByLogin = doc.getCheckOutUserLogin();
                if (reservedByLogin.equals(getCurrentUserLogin())){
                    reservedDocument.setImageResource(R.drawable.checked_out_current_user);
                }
                reservedBy.setText(reservedByName);
            }
            else{
                reservedBy.setText("");
                reservedDocument.setImageResource(R.drawable.checked_in);
            }
            View contentLink = documentRowView.findViewById(R.id.contentLink);
            contentLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseContext(), DocumentDetailsActivity.class);
                    intent.putExtra(DocumentDetailsActivity.DOCUMENT_EXTRA, doc);
                    startActivity(intent);
                }
            });
            CheckBox notifyStateChange = (CheckBox) documentRowView.findViewById(R.id.notifyStateChange);
            notifyStateChange.setChecked(doc.getStateChangeNotification());
            final CheckBox notifyIteration = (CheckBox) documentRowView.findViewById(R.id.notifyIteration);
            notifyIteration.setChecked(doc.getIterationNotification());
            notifyIteration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean b = notifyIteration.isChecked();
                    if (b) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.confirmSubscribeToIterationChangeNotification);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.i("docDoku.DocDokuPLM", "Subscribing to iteration change notification for document with reference " + doc.getReference());
                                new HttpPutTask(null).execute("workspaces/" + getCurrentWorkspace() + "/documents/" + doc.getReference() + "/notification/iterationChange/subscribe");
                            }
                        });
                        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                notifyIteration.setChecked(!b);
                            }
                        });
                        builder.create().show();
                    } else {
                    }
                }
            });
            return documentRowView;
        }
    }
}
