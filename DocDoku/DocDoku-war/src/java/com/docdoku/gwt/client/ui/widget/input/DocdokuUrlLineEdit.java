package fr.senioriales.stocks.gwt.client.ui.widget.input;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.VerticalPanel;
import fr.senioriales.stocks.gwt.client.ui.widget.util.URLChecker;


public class DocdokuUrlLineEdit extends Composite{

    private DocdokuLineEdit lineEdit ;
    private InlineLabel link ;

    public DocdokuUrlLineEdit() {
        lineEdit = new DocdokuLineEdit();
        lineEdit.setChecker(new URLChecker());
        link = new InlineLabel();
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(lineEdit) ;
        mainPanel.add(link);
        initWidget(mainPanel);
        lineEdit.setVisible(true);
        link.addStyleName("normalLinkAction");
        link.setVisible(false);
        link.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                Window.open(link.getText(), link.getText(), "");
            }
        });

    }

    public void setEnabled(boolean enabled) {
        lineEdit.setVisible(enabled);
        link.setVisible(!enabled);
    }

    public void setText(String text) {
        lineEdit.setText(text);
        link.setText(text);
    }

    public void removeListener(DocdokuLineEditListener listener) {
        lineEdit.removeListener(listener);
    }

    public String getText() {
        return lineEdit.getText();
    }

    public boolean containsAcceptableInput() {
        return lineEdit.containsAcceptableInput();
    }

    public void addListener(DocdokuLineEditListener listener) {
        lineEdit.addListener(listener);
    }





}
