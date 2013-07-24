package docDoku.DocDokuPLM;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;


public class DocumentListActivity extends FragmentActivity implements OnMenuSelected{

    ListView documentListView;
    ArrayList<Document> documentList;
    DocumentArrayAdapter documentAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        Log.i("DocDokuPMLAndroid", "DocumentListActivity starting");

        documentListView = (ListView) findViewById(R.id.documentList);
        Button loadMoreButton = new Button(this);
        loadMoreButton.setText(R.string.chargerPlus);
        documentListView.addFooterView(loadMoreButton);
        documentList = new ArrayList<Document>();
        documentList.addAll(getTestArray(10));
        documentAdapter = new DocumentArrayAdapter(DocumentListActivity.this, 0, documentList);
        documentListView.setAdapter(documentAdapter);

        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Document> results = getTestArray(10);
                documentList.addAll(results);
                documentAdapter.notifyDataSetChanged();
            }
        });
    }



    @Override
    public void onMenuSelected(){
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    public ArrayList<Document> getTestArray(int size){
        ArrayList<Document> docArray = new ArrayList<Document>();
        for (int i=0; i<size; i++){
            Document document = new Document("Reference" + i);
            document.updateContent("workspace/dossier" + i, "Martin Devillers", "12-06-2013 14:03:59", "TypeDocument" + i, "TitreDocument" + i, "Martin Devillers", "08-07-2013 15:20:17", "", "Description du document " + i);
            String[] fichiers = {"Fichier1", "Fichier2", "Fichier3"};
            document.updateFiles(fichiers);
            docArray.add(document);
        }
        return docArray;
    }

}
