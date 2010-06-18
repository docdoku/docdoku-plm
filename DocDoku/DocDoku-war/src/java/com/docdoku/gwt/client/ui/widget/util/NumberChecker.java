package fr.senioriales.stocks.gwt.client.ui.widget.util;


public class NumberChecker implements DocdokuChecker{

    public boolean check(String expressionToCheck) {
        return expressionToCheck.matches("^[0-9]+(\\.|,)?[0-9]*") ;
    }

}
