package fr.senioriales.stocks.gwt.client.ui.widget.input;

import java.util.EventListener;


public interface DocdokuLinesEditListener extends EventListener{

    /**
     * This method is called by a DocdokuLinesEdit whenever its input state change.
     * ie :
     * <ul>
     * <li>When changing from Acceptable input to Unacceptable input</li>
     * <li>When changing from Unacceptable input to Acceptable input</li>
     * </ul>
     * @param event
     */
    public void onInputStateChange(DocdokuLinesEditEvent event) ;

}
