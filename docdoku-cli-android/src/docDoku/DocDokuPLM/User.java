package docDoku.DocDokuPLM;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class User {

    private String name;
    private String login;
    private String email;

    public User(String name, String email, String login){
        this.name = name;
        this.email = email;
        this.login = login;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getLogin(){
        return login;
    }
}
