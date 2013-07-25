package docDoku.DocDokuPLM;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class DocumentArrayAdapter extends ArrayAdapter<Document>{

    private Context context;
    private ArrayList<Document> documents;

    public DocumentArrayAdapter(Context context, int textViewResourceId, ArrayList<Document> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        documents = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup root){
        View view;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.adapter_document_v1, null);

        final Document document = documents.get(position);

        TextView reference = (TextView) view.findViewById(R.id.reference);
        reference.setText(document.getReference());
        reference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DocumentActivity.class);
                intent.putExtra("document", document);
                context.startActivity(intent);
            }
        });

        final ImageButton notifyIteration = (ImageButton) view.findViewById(R.id.notifyIteration);
        notifyIteration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (document.iterationNotificationEnabled()){
                    showDialog(R.string.confirmUnsubscribeToIterationChangeNotification, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            //désactiver les notifications d'itérations
                            document.setIterationNotification(false);
                            notifyIteration.setBackgroundDrawable(context.getResources().getDrawable(R.color.transparent));
                        }
                    });
                }
                else{
                    showDialog(R.string.confirmSubscribeToIterationChangeNotification, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            //activer les notifications d'itérations
                            document.setIterationNotification(true);
                            notifyIteration.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.red_circle));
                        }
                    });
                }
            }
        });

        final ImageButton notifyStateChange = (ImageButton) view.findViewById(R.id.notifyStateChange);
        notifyStateChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (document.stateChangeNotificationEnabled()){
                    showDialog(R.string.confirmerDesactiverNotificationsChangementEtat, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            //désactiver les notifications de changement d'état
                            document.setStateChangeNotification(false);
                            notifyStateChange.setBackgroundDrawable(context.getResources().getDrawable(R.color.transparent));
                        }
                    });
                }
                else{
                    showDialog(R.string.confirmerDesactiverNotificationsChangementEtat, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            //activer les notifications de changement d'état
                            document.setStateChangeNotification(true);
                            notifyStateChange.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.red_circle));
                        }
                    });
                }
            }
        });

        return view;
    }

    private final void showDialog(int messageId, DialogInterface.OnClickListener clickListener){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder
                .setMessage(messageId)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, clickListener)
                .setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
