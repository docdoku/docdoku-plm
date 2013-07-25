package docDoku.DocDokuPLM;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class Document implements Serializable{

    private String reference;
    private String folder;
    private String author;
    private String creationDate;
    private String type;
    private String title;
    private String checkOutUserName;
    private String checkOutUserLogin;
    private String dateReservation;
    private String lifeCycleState;
    private String description;
    private String[] fichiers;

    private boolean iterationNotification;
    private boolean stateChangeNotification;

    public Document(String reference){
        this.reference = reference;
        iterationNotification = false;
        stateChangeNotification = false;
    }

    public void setCheckOutUserName(String checkOutUserName){
        this.checkOutUserName = checkOutUserName;
    }

    public void setCheckOutUserLogin(String checkOutUserLogin){
        this.checkOutUserLogin = checkOutUserLogin;
    }

    public void setDocumentDetails(String folder, String author, String creationDate, String type, String title, String lifeCycleState, String description){
        this.folder = folder;
        this.author = author;
        this.creationDate = creationDate;
        this.type = type;
        this.title = title;
        this.lifeCycleState = lifeCycleState;
        this.description = description;
    }

    public void updateFiles(String[] fichiers){
        this.fichiers = fichiers;
    }

    public String getReference(){
        return reference;
    }

    public String getAuthor(){
        return author;
    }

    public String getCheckOutUserName(){
        return checkOutUserName;
    }

    public String getCheckOutUserLogin(){
        return checkOutUserLogin;
    }

    public String getReservationDate(){
        return dateReservation;
    }

    public String getTitle(){
        return title;
    }

    public boolean iterationNotificationEnabled(){
        return iterationNotification;
    }

    public void setIterationNotification(boolean set){
        iterationNotification = set;
    }

    public boolean stateChangeNotificationEnabled(){
        return stateChangeNotification;
    }

    public void setStateChangeNotification(boolean set){
        stateChangeNotification = set;
    }

    public String getFolder(){
        return folder;
    }

    public String getCreationDate(){
        return creationDate;
    }

    public String getType(){
        return type;
    }

    public String getLifeCycleState(){
        return lifeCycleState;
    }

    public String getDescription(){
        return description;
    }

    public boolean getIterationNotification(){
        return iterationNotification;
    }

    public boolean getStateChangeNotification(){
        return stateChangeNotification;
    }
}
