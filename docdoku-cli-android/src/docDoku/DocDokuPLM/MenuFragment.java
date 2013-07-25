package docDoku.DocDokuPLM;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class MenuFragment extends Fragment {

    public static final String WORKSPACE_PREFERENCE = "workspace";
    private static String[] workspaces;
    private static String WORKSPACE;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_menu, container);

        RadioGroup workspace = (RadioGroup) view.findViewById(R.id.workspaceRadioGroup);
        if (workspaces != null){
            if (WORKSPACE == null){
                SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                WORKSPACE = preferences.getString(WORKSPACE_PREFERENCE,"");
                Log.i("docDoku.DocDokuPLM", "Loading workspace from last session: " + WORKSPACE);
            }
            addWorkspaces(workspaces, workspace);
        }
        else{
            Log.e("docDoku.DocDokuPLM","ERROR: No workspaces downloaded");
        }

        workspace.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton selectedWorkspace = (RadioButton) view.findViewById(radioGroup.getCheckedRadioButtonId());
                SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                WORKSPACE = selectedWorkspace.getText().toString();
                editor.putString(WORKSPACE_PREFERENCE, WORKSPACE);
                editor.commit();
            }
        });

        LinearLayout documentSearch = (LinearLayout) view.findViewById(R.id.documentSearch);
        documentSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DocumentSearchActivity.class);
                startActivity(intent);
            }
        });

        TextView docRecemmentConsultes = (TextView) view.findViewById(R.id.documentsRecemmentConsultes);
        docRecemmentConsultes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DocumentListActivity.class);
                intent.putExtra(DocumentListActivity.LIST_MODE_EXTRA,DocumentListActivity.RECENTLY_VIEWED_DOCUMENTS_LIST);
                startActivity(intent);
            }
        });
        TextView allDocuments = (TextView) view.findViewById(R.id.allDocuments);
        allDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DocumentListActivity.class);
                intent.putExtra(DocumentListActivity.LIST_MODE_EXTRA,DocumentListActivity.ALL_DOCUMENTS_LIST);
                startActivity(intent);
            }
        });
        TextView checkedOutDocuments = (TextView) view.findViewById(R.id.checkedOutDocuments);
        checkedOutDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DocumentListActivity.class);
                intent.putExtra(DocumentListActivity.LIST_MODE_EXTRA,DocumentListActivity.CHECKED_OUT_DOCUMENTS_LIST);
                startActivity(intent);
            }
        });

        TextView artRecemmentConsultes = (TextView) view.findViewById(R.id.articlesRecemmentConsultes);
        artRecemmentConsultes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PartListActivity.class);
                intent.putExtra(PartListActivity.LIST_MODE_EXTRA,PartListActivity.RECENTLY_VIEWED_LIST);
                startActivity(intent);
            }
        });
        TextView allParts = (TextView) view.findViewById(R.id.allParts);
        allParts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PartListActivity.class);
                intent.putExtra(PartListActivity.LIST_MODE_EXTRA, PartListActivity.ALL_PARTS_LIST);
                startActivity(intent);
            }
        });

        return view;
    }

    public void addWorkspaces(String[] workspaces, RadioGroup radioGroup){
        int numWorkspaces = workspaces.length;
        for (int i=0; i<numWorkspaces; i++){
            RadioButton radioButton;
            radioButton = new RadioButton(getActivity());
            radioButton.setText(workspaces[i]);
            radioGroup.addView(radioButton);
            if (workspaces[i].equals(WORKSPACE)){
                radioGroup.check(radioButton.getId());
            }
        }
    }

    public static void setWorkspaces(String[] setWorkspaces){
        workspaces = setWorkspaces;
    }

    public static String getCurrentWorkspace(){
        return WORKSPACE;
    }
}
