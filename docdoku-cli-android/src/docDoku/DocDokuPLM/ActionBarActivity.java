package docDoku.DocDokuPLM;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public abstract class ActionBarActivity extends FragmentActivity {

    protected static String currentUserLogin;

    protected String getCurrentWorkspace(){
        return MenuFragment.getCurrentWorkspace();
    }

    protected String getCurrentUserLogin(){
        return currentUserLogin;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)){
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }
                else{
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
                return true;
            case R.id.menu_users:
                Intent intent = new Intent(this, UserListActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.confirmDisconnect));
                builder.setNegativeButton(getResources().getString(R.string.no), null);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(), ConnectionActivity.class);
                        intent.putExtra(ConnectionActivity.ERASE_ID, true);
                        startActivity(intent);
                    }
                });
                builder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
