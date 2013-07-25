package docDoku.DocDokuPLM;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: martindevillers
 * Date: 22/07/13
 * To change this template use File | Settings | File Templates.
 */
public class Part implements Serializable{

    private String key;
    private String number;
    private String version;
    private String name;
    private String authorName;
    private String creationDate;
    private String description;
    private String checkOutUserName;
    private String chekOutUserId;
    private String chekOutDate;
    private String workflow;
    private String lifecycleState;
    private boolean standardPart;
    private String workspaceId;
    private boolean publicShared;
    private ArrayList<Attribute> attributes;

    public Part(String key){
        this.key = key;
        attributes = new ArrayList<Attribute>();
    }

    public void setCheckOutInformation(String checkOutUserName, String checkOutUserId, String checkOutDate){
        this.checkOutUserName = checkOutUserName;
        this.chekOutUserId = checkOutUserId;
        this.chekOutDate = checkOutDate;
    }

    public void setCheckOutUserName(String checkOutUserName){
        this.checkOutUserName = checkOutUserName;
    }

    public void setChekOutUserId(String chekOutUserId){
        this.chekOutUserId = chekOutUserId;
    }

    public void setPartDetails(String number, String version, String name, String authorName, String creationDate, String description, String workflow, String lifecycleState, boolean standardPart, String workspaceId, boolean publicShared){
        this.number = number;
        this.version = version;
        this.name = name;
        this.authorName = authorName;
        this.creationDate = creationDate;
        this.description = description;
        this.workflow = workflow;
        this.lifecycleState = lifecycleState;
        this.standardPart = standardPart;
        this.workspaceId = workspaceId;
        this.publicShared = publicShared;
    }

    public void addAttribute(String name, String value){
        attributes.add(new Attribute(name,value));
    }

    public String[] getGeneralInformationValues(){
        String[] generalInformationValues = new String[10];
        generalInformationValues[0] = number;
        generalInformationValues[1] = name;
        generalInformationValues[2] = Boolean.toString(standardPart);
        generalInformationValues[3] = version;
        generalInformationValues[4] = authorName;
        generalInformationValues[5] = creationDate;
        generalInformationValues[6] = lifecycleState;
        generalInformationValues[7] = checkOutUserName;
        generalInformationValues[8] = chekOutDate;
        generalInformationValues[9] = description;
        return generalInformationValues;
    }

    public String getCheckOutUserName(){
        return checkOutUserName;
    }

    public String getCheckOutUserLogin(){
        return chekOutUserId;
    }

    public String getKey(){
        return key;
    }

    public String getAuthorName(){
        return authorName;
    }

    public String[] getAttributeNames(){
        String[] attributeNames = new String[attributes.size()];
        for (int i = 0; i<attributeNames.length; i++){
            attributeNames[i] = attributes.get(i).attributeName;
        }
        return attributeNames;
    }
    public String[] getAttributeValues(){
        String[] attributeValues = new String[attributes.size()];
        for (int i = 0; i<attributeValues.length; i++){
            attributeValues[i] = attributes.get(i).attributeValue;
        }
        return attributeValues;
    }

    public class Attribute implements Serializable{

        public String attributeName;
        public String attributeValue;

        public Attribute(String name, String value){
            attributeName = name;
            attributeValue = value;
        }
    }
}
