package fr.senioriales.stocks.gwt.client.ui.widget.util;


public class URLChecker implements DocdokuChecker{

    public boolean check(String expressionToCheck) {
        return expressionToCheck.matches("^(ftp|http|https):\\/\\/(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?") ;
    }

}
