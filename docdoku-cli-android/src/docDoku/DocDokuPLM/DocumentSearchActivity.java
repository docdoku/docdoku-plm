package docDoku.DocDokuPLM;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;

public class DocumentSearchActivity extends FragmentActivity implements OnMenuSelected{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_search);
    }

    @Override
    public void onMenuSelected() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.openDrawer(Gravity.LEFT);
    }
}
