package fr.senioriales.stocks.gwt.client.ui.widget.util;

/**
 * A checker that accepts input if it is not empty
 */
public class NotEmptyChecker implements DocdokuChecker{

    public boolean check(String expressionToCheck) {
        return !expressionToCheck.trim().isEmpty();
    }
}
