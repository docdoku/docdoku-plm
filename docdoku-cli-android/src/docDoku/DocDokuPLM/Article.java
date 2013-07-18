package docDoku.DocDokuPLM;

import java.io.Serializable;

public class Article implements Serializable{

    private int numero;
    private String nom;
    private boolean standard;
    private char version;
    private String auteur;
    private String etatDuCycleDeVie;
    private String reservePar;
    private String dateReservation;
    private String description;

    public Article(int numero, char version){
        this.numero = numero;
        this.version = version;
    }

}
