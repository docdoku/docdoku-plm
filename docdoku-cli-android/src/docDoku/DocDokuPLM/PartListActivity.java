package docDoku.DocDokuPLM;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class PartListActivity extends ActionBarActivity implements HttpGetListener {

    public static final String  LIST_MODE_EXTRA = "list mode";
    public static final int ALL_PARTS_LIST = 0;
    public static final int RECENTLY_VIEWED_LIST = 1;
    private static final String HISTORY_SIZE = "Part history size";
    private static final int MAX_PARTS_IN_HISTORY = 15;

    private ListView partListView;
    private LinkedHashSet<String> partKeyHistory;
    private View loading;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        partListView = (ListView) findViewById(R.id.elementList);
        loading = findViewById(R.id.loading);

        getPartHistory();

        Intent intent = getIntent();
        int listCode = intent.getIntExtra(LIST_MODE_EXTRA, ALL_PARTS_LIST);
        switch (listCode){
            case ALL_PARTS_LIST:
                new HttpGetTask(this).execute("workspaces/" + getCurrentWorkspace() + "/parts/");
                break;
            case RECENTLY_VIEWED_LIST:
                ((ViewGroup) loading.getParent()).removeView(loading);
                ArrayList<Part> partArray= new ArrayList<Part>();
                PartAdapter adapter = new PartAdapter(partArray);
                Iterator<String> iterator = partKeyHistory.iterator();
                while (iterator.hasNext()){
                    Part part = new Part(iterator.next());
                    partArray.add(part);
                    getPartInformation(part);
                }
                partListView.setAdapter(adapter);
                break;
        }
    }

    @Override
    public void onHttpGetResult(String result) {
        if (loading !=null){
            ((ViewGroup) loading.getParent()).removeView(loading);
            loading = null;
        }
        ArrayList<Part> partsArray = new ArrayList<Part>();
        try {
            JSONArray partsJSON = new JSONArray(result);
            for (int i=0; i<partsJSON.length(); i++){
                JSONObject partJSON = partsJSON.getJSONObject(i);
                Part part = new Part(partJSON.getString("partKey"));
                partsArray.add(updatePartFromJSON(partJSON,part));
            }
            partListView.setAdapter(new PartAdapter(partsArray));
        } catch (JSONException e) {
            Log.e("docdoku.DocDokuPLM", "Error handling json of workspace's parts");
            e.printStackTrace();
            Log.i("docdoku.DocDokuPLM", "Error message: " + e.getMessage());
        }
    }

    private Part updatePartFromJSON(JSONObject partJSON, Part part) throws JSONException {
        Object reservedBy = partJSON.get("checkOutUser");
        SimpleDateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.fullDateFormat));
        if (reservedBy != JSONObject.NULL){
            part.setCheckOutInformation(
                    ((JSONObject) reservedBy).getString("name"),
                    ((JSONObject) reservedBy).getString("login"),
                    dateFormat.format(new Date(Long.valueOf(partJSON.getString("checkOutDate"))))
            );
        }
        part.setPartDetails(
                partJSON.getString("number"),
                partJSON.getString("version"),
                partJSON.getString("name"),
                partJSON.getJSONObject("author").getString("name"),
                dateFormat.format(new Date(Long.valueOf(partJSON.getString("creationDate")))),
                partJSON.getString("description"),
                partJSON.getString("workflow"),
                partJSON.getString("lifeCycleState"),
                partJSON.getBoolean("standardPart"),
                partJSON.getString("workspaceId"),
                partJSON.getBoolean("publicShared")
        );
        JSONArray iterationsArray = partJSON.getJSONArray("partIterations");
        JSONArray attributes = iterationsArray.getJSONObject(iterationsArray.length()-1).getJSONArray("instanceAttributes");
        for (int i = 0; i<attributes.length(); i++){
            JSONObject attribute = attributes.getJSONObject(i);
            part.addAttribute(attribute.getString("name"),attribute.getString("value"));
        }
        return part;
    }

    private void getPartHistory(){
        partKeyHistory = new LinkedHashSet<String>();
        SharedPreferences preferences = getSharedPreferences(getCurrentWorkspace(), MODE_PRIVATE);
        int numPartsInHistory = preferences.getInt(HISTORY_SIZE, 0);
        for (int i = 0; i<numPartsInHistory; i++){
            Log.i("docdoku.DocDokuPLM","Retreiving key at position " + i + ": " + preferences.getString(Integer.toString(i),""));
            partKeyHistory.add(preferences.getString(Integer.toString(i), ""));
        }
    }

    private void updatePartHistory(String key){
        Log.i("docdoku.DocDokuPLM", "Adding part " + key +" to history");
        partKeyHistory.add(key);
        SharedPreferences preferences = getSharedPreferences(getCurrentWorkspace(), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Iterator<String> iterator = partKeyHistory.iterator();
        while (partKeyHistory.size() > MAX_PARTS_IN_HISTORY){
            iterator.next();
            iterator.remove();
        }
        editor.putInt(HISTORY_SIZE, partKeyHistory.size());
        int i = partKeyHistory.size()-1;
        while (iterator.hasNext()){
            String next = iterator.next();
            Log.i("docdoku.DocDokuPLM", "Storing key " + next + " in preferences at position " + i);
            editor.putString(Integer.toString(i), next);
            i--;
        }
        editor.commit();
    }

    private synchronized void getPartInformation(final Part part){
        new HttpGetTask(new HttpGetListener() {
            @Override
            public void onHttpGetResult(String result) {
                try {
                    JSONObject partJSON = new JSONObject(result);
                    updatePartFromJSON(partJSON, part);
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }).execute("workspaces/" + getCurrentWorkspace() + "/parts/" + part.getKey());
    }

    private class PartAdapter extends BaseAdapter{

        private ArrayList<Part> parts;
        private final LayoutInflater inflater;

        public PartAdapter(ArrayList<Part> parts){
            this.parts = parts;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return parts.size();
        }

        @Override
        public Object getItem(int i) {
            return parts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View partRowView = inflater.inflate(R.layout.adapter_part, null);
            final Part part = parts.get(i);
            TextView reference = (TextView) partRowView.findViewById(R.id.number);
            reference.setText(part.getKey());
            TextView reservedBy = (TextView) partRowView.findViewById(R.id.reservedBy);
            ImageView reservedPart = (ImageView) partRowView.findViewById(R.id.reservedPart);
            String reservedByName = part.getCheckOutUserName();
            if (reservedByName != null){
                String reservedByLogin = part.getCheckOutUserLogin();
                if (reservedByLogin.equals(getCurrentUserLogin())){
                    reservedPart.setImageResource(R.drawable.checked_out_current_user);
                }
                reservedBy.setText(reservedByName);
            }
            else{
                reservedBy.setText("");
                reservedPart.setImageResource(R.drawable.checked_in);
            }
            partRowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatePartHistory(part.getKey());
                    Intent intent = new Intent(PartListActivity.this, PartActivity.class);
                    intent.putExtra(PartActivity.PART_EXTRA,part);
                    startActivity(intent);
                }
            });
            return partRowView;
        }
    }
}