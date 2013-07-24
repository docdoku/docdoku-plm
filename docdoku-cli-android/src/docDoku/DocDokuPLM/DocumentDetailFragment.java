package docDoku.DocDokuPLM;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

public class DocumentDetailFragment extends Fragment {

    private LinearLayout layout;
    private LayoutInflater inflater;
    private String[] nameValues;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view;
        view = inflater.inflate(R.layout.fragment_document_detail, null);
        layout = (LinearLayout) view.findViewById(R.id.linearLayout);

        this.inflater = inflater;

        if (nameValues != null){
            addNameValueRows();
        }

        return view;
    }

    public void setNameValues(String[] nameValues){
        this.nameValues = nameValues;
    }

    public void addNameValueRows(){
        if (nameValues.length % 2 == 0){
            for (int i=0; i<nameValues.length; i+=2){
                View row = inflater.inflate(R.layout.document_detail_row, null);
                TextView fieldName = (TextView) row.findViewById(R.id.fieldName);
                fieldName.setText(nameValues[i]);
                TextView fieldValue = (TextView) row.findViewById(R.id.fieldValue);
                fieldValue.setText(nameValues[i+1]);
                layout.addView(row);
            }
        }
        else{
            Log.e("docDoku.DocDokuPLM","ERROR: odd number of name/value pair elements in document description");
        }
    }

}
