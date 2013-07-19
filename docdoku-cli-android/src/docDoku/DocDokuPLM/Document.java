package docDoku.DocDokuPLM;

import java.io.Serializable;

public class Document implements Serializable{

    private String reference;
    private String dossier;
    private String auteur;
    private String dateCreation;
    private String type;
    private String titre;
    private String reservePar;
    private String dateReservation;
    private String etatDuCycleDeVie;
    private String description;
    private String[] fichiers;

    private boolean iterationNotification;
    private boolean stateChangeNotification;

    public Document(String reference){
        this.reference = reference;
        iterationNotification = false;
        stateChangeNotification = false;
    }

    public void updateContent(String dossier, String auteur, String dateCreation, String type, String titre, String reservePar, String dateReservation, String etatDuCycleDeVie, String description){
        this.dossier = dossier;
        this.auteur = auteur;
        this.dateCreation = dateCreation;
        this.type = type;
        this.titre = titre;
        this.reservePar = reservePar;
        this.dateReservation = dateReservation;
        this.etatDuCycleDeVie = etatDuCycleDeVie;
        this.description = description;
    }

    public void updateFiles(String[] fichiers){
        this.fichiers = fichiers;
    }

    public String getReference(){
        return reference;
    }

    public String getAuthor(){
        return auteur;
    }

    public String getReservedBy(){
        return reservePar;
    }

    public String getReservationDate(){
        return dateReservation;
    }

    public String getTitle(){
        return titre;
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

    public String getDossier(){
        return dossier;
    }

    public String getDateCreation(){
        return dateCreation;
    }

    public String getType(){
        return type;
    }

    public String getEtatDuCycleDeVie(){
        return etatDuCycleDeVie;
    }

    public String getDescription(){
        return description;
    }

}
