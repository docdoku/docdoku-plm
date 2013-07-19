package docDoku.DocDokuPLM;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DocumentActivity extends FragmentActivity implements OnMenuSelected{

    private Document document;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        Intent intent = getIntent();
        document = (Document) intent.getSerializableExtra("document");

        //Fill out the main details of the general document information
        TextView reference = (TextView) findViewById(R.id.reference);
        reference.setText(document.getReference());
        TextView titre = (TextView) findViewById(R.id.titre);
        titre.setText(document.getTitle());
        TextView auteur = (TextView) findViewById(R.id.auteur);
        auteur.setText(document.getAuthor());
        TextView reservePar = (TextView) findViewById(R.id.reservePar);
        reservePar.setText(document.getReservedBy());

        LinearLayout generalites = (LinearLayout) findViewById(R.id.generalites);
        generalites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("DocDokuPMLAndroid", "Document general information selected");
                Intent intent = new Intent(DocumentActivity.this, DocumentDetailsActivity.class);
                intent.putExtra("document", document);
                intent.putExtra("page", 0);
                startActivity(intent);
            }
        });

        LinearLayout files = (LinearLayout) findViewById(R.id.fichiers);
        files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {;
                Log.i("DocDokuPMLAndroid", "Document files");
                Intent intent = new Intent(DocumentActivity.this, DocumentDetailsActivity.class);
                intent.putExtra("document", document);
                intent.putExtra("page", 1);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onMenuSelected(){
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.openDrawer(Gravity.LEFT);
    }

}