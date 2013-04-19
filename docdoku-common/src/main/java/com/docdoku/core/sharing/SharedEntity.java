package com.docdoku.core.sharing;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Table(name="SHAREDENTITY")
@XmlSeeAlso({SharedDocument.class,SharedPart.class})
@Inheritance()
@Entity
@javax.persistence.IdClass(SharedEntityKey.class)
@NamedQueries({
        @NamedQuery(name="SharedEntity.findSharedEntityForGivenUuid", query="SELECT se FROM SharedEntity se WHERE se.uuid = :pUuid")
})
public class SharedEntity implements Serializable {

    @Id
    private String uuid;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Workspace workspace;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expireDate;

    private String password;

    public SharedEntity() {
    }


    public SharedEntity(Workspace workspace, User author, Date expireDate, String password) {
        this.workspace = workspace;
        this.uuid = UUID.randomUUID().toString();
        this.author = author;
        this.creationDate = new Date();
        this.expireDate = expireDate;
        if(password != null){
            try{
                this.password = md5Sum(password);
            }catch(NoSuchAlgorithmException pEx){
                System.err.println(pEx.getMessage());
            }
        }
    }

    public SharedEntity(Workspace workspace, User author) {
        this(workspace,author,null,null);
    }

    public SharedEntity(Workspace workspace, User author, Date expireDate) {
        this(workspace,author,expireDate,null);
    }

    public SharedEntity(Workspace workspace, User author, String password) {
        this(workspace,author,null,password);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SharedEntity that = (SharedEntity) o;

        if (expireDate != null ? !expireDate.equals(that.expireDate) : that.expireDate != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (expireDate != null ? expireDate.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    private static String md5Sum(String pText) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("MD5").digest(pText.getBytes());
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i < digest.length; i++) {
            String hex = Integer.toHexString(0xFF & digest[i]);
            if (hex.length() == 1) {
                hexString.append("0" + hex);
            } else {
                hexString.append(hex);
            }
        }
        return hexString.toString();
    }
}
