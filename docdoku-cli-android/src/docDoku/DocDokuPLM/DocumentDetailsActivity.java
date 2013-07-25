package docDoku.DocDokuPLM;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class DocumentDetailsActivity extends ActionBarActivity implements HttpPostListener{

    public static final String DOCUMENT_EXTRA = "document";

    private Document document;

    static final int NUM_PAGES = 5;
    private ViewPager pager;

    private Button checkInOutButton;
    private TextView checkOutUser;
    private ImageView checkOutLogo;
    private boolean checkedIn;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        Intent intent = getIntent();
        document = (Document) intent.getSerializableExtra(DOCUMENT_EXTRA);

        TextView documentReference = (TextView) findViewById(R.id.documentReference);
        documentReference.setText(document.getReference());
        TextView documentAuthor = (TextView) findViewById(R.id.documentAuthor);
        documentAuthor.setText(document.getAuthor());

        CheckBox notifyIteration = (CheckBox) findViewById(R.id.notifyIteration);
        notifyIteration.setChecked(document.getIterationNotification());
        CheckBox notifyStateChange = (CheckBox) findViewById(R.id.notifyStateChange);
        notifyStateChange.setChecked(document.getStateChangeNotification());

        checkOutLogo = (ImageView) findViewById(R.id.checkOutLogo);
        checkOutUser = (TextView) findViewById(R.id.checkOutUser);
        checkOutUser.setText(document.getCheckOutUserName());
        checkInOutButton = (Button) findViewById(R.id.checkInOutButton);
        if (document.getCheckOutUserLogin() != null){
            if (getCurrentUserLogin().equals(document.getCheckOutUserLogin())){
                setDocumentCheckedOutByCurrentUser();
            }
            else{
                checkOutLogo.setImageResource(R.drawable.checked_out_other_user);
                checkInOutButton.setVisibility(View.GONE);
            }
        }
        else{
            setDocumentCheckedIn();
        }
        
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new DocumentPagerAdapter(getSupportFragmentManager()));

        pager.setCurrentItem(intent.getIntExtra("page", 0));
    }

    private void setDocumentCheckedIn(){
        checkedIn = true;
        checkOutLogo.setImageResource(R.drawable.checked_in);
        checkInOutButton.setText(R.string.documentCheckOut);
        checkOutUser.setText(null);
        document.setCheckOutUserLogin(null);
        final Activity activity = this;
        final HttpPostListener httpPostListener = this;
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.documentCheckOutConfirm);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new HttpPutTask(httpPostListener).execute("workspaces/" + getCurrentWorkspace() + "/documents/" + document.getReference() + "/checkout/");
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.create().show();
            }
        });
    }

    public void setDocumentCheckedOutByCurrentUser(){
        checkedIn = false;
        checkOutLogo.setImageResource(R.drawable.checked_out_current_user);
        checkInOutButton.setText(R.string.documentCheckIn);
        checkOutUser.setText(getCurrentUserLogin());
        document.setCheckOutUserLogin(getCurrentUserLogin());
        final Activity activity = this;
        final HttpPostListener httpPostListener = this;
        checkInOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.documentCheckInConfirm);
                builder.setPositiveButton(R.string.documentDoCheckIn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new HttpPutTask(httpPostListener).execute("workspaces/" + getCurrentWorkspace() + "/documents/" + document.getReference() + "/checkin/");
                    }
                });
                builder.setNeutralButton(R.string.documentCancelCheckOut, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new HttpPutTask(httpPostListener).execute("workspaces/" + getCurrentWorkspace() + "/documents/" + document.getReference() + "/undocheckout/");
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.create().show();
            }
        });
    }

    @Override
    public void onHttpPostResult(boolean result) {
        Log.i("docDoku.DocDokuPLM", "Result of checkin/checkout: " + result);
        if (true){
            if (checkedIn){
                setDocumentCheckedOutByCurrentUser();
            }
            else{
                setDocumentCheckedIn();
            }
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.connectionError);
            builder.setPositiveButton(R.string.OK, null);
            builder.create().show();
        }
    }

    public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
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
                    resources.getString(R.string.dossierDocument), document.getFolder(),
                    resources.getString(R.string.documentReference), document.getReference(),
                    resources.getString(R.string.documentAuthor), document.getAuthor(),
                    resources.getString(R.string.dateCreationDocument), document.getCreationDate(),
                    resources.getString(R.string.typeDocument), document.getType(),
                    resources.getString(R.string.titreDocument), document.getTitle(),
                    resources.getString(R.string.reserveParDocument), document.getCheckOutUserName(),
                    resources.getString(R.string.dateReservationDocument), document.getReservationDate(),
                    resources.getString(R.string.etatCycleVieDocument), document.getLifeCycleState(),
                    resources.getString(R.string.descriptionDocument), document.getDescription()

            });
            return result;
        }
    }

    private class DocumentPagerAdapter extends FragmentPagerAdapter{

        public DocumentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new DocumentPageFragment(new String[0], new String[0]);
                    //return new DocumentPageFragment(getResources().getStringArray(R.array.partGeneralInformationFieldNames), part.getGeneralInformationValues());
                case 1:
                    return new DocumentPageFragment(new String[0], new String[0]);
                case 2:
                    return new DocumentPageFragment(new String[0], new String[0]);
                    //return new DocumentPageFragment(part.getAttributeNames(), part.getAttributeValues());
                case 3:
                    return new DocumentPageFragment(new String[0], new String[0]);
                case 4:
                    return new DocumentPageFragment(new String[0], new String[0]);
                case 5:
                    return new DocumentPageFragment(new String[0], new String[0]);
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position){
            switch (position){
                case 0:
                    return "Général";
                case 1:
                    return "Itération";
                case 2:
                    return "Attributs";
                case 3:
                    return "Fichiers";
                case 4:
                    return "Liens";
            }
            return "";
        }
    }

    private class DocumentPageFragment extends Fragment{

        private String[] names;
        private String[] values;

        public DocumentPageFragment(String[] names, String[] values){
            this.names = names;
            this.values = values;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            ViewGroup pageView;
            pageView = (ViewGroup) inflater.inflate(R.layout.fragment_detail_list, null);
            ViewGroup content = (ViewGroup) pageView.findViewById(R.id.content);

            if (names.length != values.length){
                Log.e("docdoku.DocDokuPLM","ERROR: Not the same number of names as values");
            }
            else{
                for (int i = 0; i< names.length; i++){
                    addNameValueRow(inflater,content, names[i],values[i]);
                }
            }

            return pageView;
        }

        private void addNameValueRow(LayoutInflater inflater, ViewGroup content, String name, String value){
            View row = inflater.inflate(R.layout.detail_row, null);
            TextView fieldName = (TextView) row.findViewById(R.id.fieldName);
            fieldName.setText(name);
            TextView fieldValue = (TextView) row.findViewById(R.id.fieldValue);
            fieldValue.setText(value);
            content.addView(row);
        }
    }

}

