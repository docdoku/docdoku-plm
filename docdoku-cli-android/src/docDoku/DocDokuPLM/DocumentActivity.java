package docDoku.DocDokuPLM;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class DocumentActivity extends ActionBarActivity{

    private Document document;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document1);

        Intent intent = getIntent();
        document = (Document) intent.getSerializableExtra("document");

        //Fill out the main details of the general document information
        TextView reference = (TextView) findViewById(R.id.reference);
        reference.setText(document.getReference());
        TextView titre = (TextView) findViewById(R.id.titre);
        titre.setText(document.getTitle());
        TextView auteur = (TextView) findViewById(R.id.reservedBy);
        auteur.setText(document.getAuthor());
        TextView reservePar = (TextView) findViewById(R.id.reservePar);
        reservePar.setText(document.getCheckOutUserName());

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

}