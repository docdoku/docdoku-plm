package docDoku.DocDokuPLM;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class TitleBarFragment extends Fragment{

    private OnMenuSelected callbackActivity;
    private RelativeLayout relativeLayout;
    private LayoutInflater inflater;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        //Make sure the activity to which the fragment is attached effectively implement OnMenuSelected interface
        try {
            callbackActivity = (OnMenuSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        relativeLayout = new RelativeLayout(getActivity());
        this.inflater = inflater;
        setNormalLayout();

        return relativeLayout;
    }

    private void setNormalLayout(){
        relativeLayout.removeAllViews();
        relativeLayout.addView(inflater.inflate(R.layout.fragment_title_bar, null));

        final ImageButton menu = (ImageButton) relativeLayout.findViewById(R.id.menuButton);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbackActivity.onMenuSelected();
            }
        });

        Spinner settings = (Spinner) relativeLayout.findViewById(R.id.settingsButton);
        settings.setAdapter(new SpinnerAdapter());
        final ImageButton search = (ImageButton) relativeLayout.findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSearchLayout();
            }
        });
    }

    private void setSearchLayout(){
        relativeLayout.removeAllViews();
        relativeLayout.addView(inflater.inflate(R.layout.fragment_search_bar, null));
        ImageButton cancelSearch = (ImageButton) relativeLayout.findViewById(R.id.cancelButton);
        cancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNormalLayout();
            }
        });
        ImageButton search = (ImageButton) relativeLayout.findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText searchField = (EditText) relativeLayout.findViewById(R.id.searchField);
                String search = searchField.getText().toString();
                Log.i("docDoku.DocDokuPLM", "Content of search field: " + search);
                //TODO search articles and documents
            }
        });
    }

    private class SpinnerAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_preferences));
            return imageView;
        }

        @Override
        public View getDropDownView(final int position, View convertView, ViewGroup parent) {
            TextView view;
            view = new TextView(getActivity());
            int paddingDP = 5;
            float density = getResources().getDisplayMetrics().density;
            int paddingPX = Math.round(paddingDP * density);
            view.setPadding(paddingPX, paddingPX, paddingPX, paddingPX);
            view.setTextSize(24);
            view.setText(getResources().getStringArray(R.array.settingsArray)[position]);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (position){
                        case 0: Log.i("docDoku.DocDokuPLM", "Settings: Liste des utilisateurs du workspace");
                            break;
                        case 1: Log.i("docDoku.DocDokuPLM", "Settings: deconnexion");
                            Intent intent = new Intent(getActivity(), ConnectionActivity.class);
                            intent.putExtra(ConnectionActivity.ERASE_ID, true);
                            startActivity(intent);
                            break;
                    }
                }
            });
            return view;
        }
    }

}
