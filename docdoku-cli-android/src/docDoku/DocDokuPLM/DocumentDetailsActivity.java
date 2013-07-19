package docDoku.DocDokuPLM;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class DocumentDetailsActivity extends FragmentActivity implements OnMenuSelected {

    private Document document;

    static final int NUM_ITEMS = 5;

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_details);

        Intent intent = getIntent();
        document = (Document) intent.getSerializableExtra("document");

        Log.i("DocDokuPMLAndroid", "DocumentDetailsActivity starting");

        mPager = (ViewPager) findViewById(R.id.pager);
        Log.i("DocDokuPMLAndroid","Creating the pager adapter");
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        Log.i("DocDokuPMLAndroid", "Setting the pager adapter for the ViewPager");
        mPager.setAdapter(mPagerAdapter);

        mPager.setCurrentItem(intent.getIntExtra("page",0));
    }

    @Override
    public void onMenuSelected(){
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0: return getDocumentGeneralDetailsFragment();
                case 1: return new DocumentFilesFragment();
                case 2: return new DocumentDetailFragment();
                case 3: return new DocumentDetailFragment();
                case 4: return new DocumentDetailFragment();
            }
            return null;
        }

        private Fragment getDocumentGeneralDetailsFragment(){
            DocumentDetailFragment result = new DocumentDetailFragment();
            Resources resources = getResources();
            result.setNameValues(new String[]{
                    resources.getString(R.string.dossierDocument), document.getDossier(),
                    resources.getString(R.string.referenceDocument), document.getReference(),
                    resources.getString(R.string.auteurDocument), document.getAuthor(),
                    resources.getString(R.string.dateCreationDocument), document.getDateCreation(),
                    resources.getString(R.string.typeDocument), document.getType(),
                    resources.getString(R.string.titreDocument), document.getTitle(),
                    resources.getString(R.string.reserveParDocument), document.getReservedBy(),
                    resources.getString(R.string.dateReservationDocument), document.getReservationDate(),
                    resources.getString(R.string.etatCycleVieDocument), document.getEtatDuCycleDeVie(),
                    resources.getString(R.string.descriptionDocument), document.getDescription()

            });
            return result;
        }
    }

}

