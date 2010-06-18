

package fr.senioriales.stocks.gwt.client.ui.widget.util;

/**
 * DocdokuChecker provides check capabilities on strings.
 *
 */
public abstract interface DocdokuChecker {

    /**
     * Returns true if the expression parameters matches allowed input
     * @param expressionToCheck the expression to check
     * @return
     */
    boolean check(String expressionToCheck) ;

}
