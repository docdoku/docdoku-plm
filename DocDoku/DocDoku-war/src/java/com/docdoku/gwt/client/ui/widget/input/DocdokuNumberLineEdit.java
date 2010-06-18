package fr.senioriales.stocks.gwt.client.ui.widget.input;


import com.google.gwt.user.client.ui.TextBox;
import fr.senioriales.stocks.gwt.client.ui.widget.util.NumberChecker;


public class DocdokuNumberLineEdit extends DocdokuLineEdit {

    public DocdokuNumberLineEdit() {
        super();
        setTextAlignment(TextBox.ALIGN_RIGHT);
        setChecker(new NumberChecker());
    }
}
