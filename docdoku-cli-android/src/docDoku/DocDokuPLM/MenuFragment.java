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

        View.OnClickListener docClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DocumentListActivity.class);
                startActivity(intent);
            }
        };

        TextView docRecemmentConsultes = (TextView) view.findViewById(R.id.documentsRecemmentConsultes);
        docRecemmentConsultes.setOnClickListener(docClickListener);
        TextView docRecemmentModifies = (TextView) view.findViewById(R.id.documentsRecemmentModifies);
        docRecemmentModifies.setOnClickListener(docClickListener);

        View.OnClickListener artClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ArticleListActivity.class);
                startActivity(intent);
            }
        };
        TextView artRecemmentConsultes = (TextView) view.findViewById(R.id.articlesRecemmentConsultes);
        artRecemmentConsultes.setOnClickListener(artClickListener);
        TextView artRecemmentModifies = (TextView) view.findViewById(R.id.articlesRecemmentModifies);
        artRecemmentModifies.setOnClickListener(artClickListener);

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
}
