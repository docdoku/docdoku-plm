/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class for all classes that allow the definition of permanent link to business objects
 * like documents or parts.
 *
 * @author Morgan Guimard
 */

@Table(name="SHAREDENTITY")
@XmlSeeAlso({SharedDocument.class,SharedPart.class})
@Inheritance()
@Entity
@javax.persistence.IdClass(SharedEntityKey.class)
@NamedQueries({
        @NamedQuery(name="SharedEntity.findSharedEntityForGivenUuid", query="SELECT se FROM SharedEntity se WHERE se.uuid = :pUuid")
})
public abstract class SharedEntity implements Serializable {

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
        this.expireDate = (expireDate!=null) ? (Date) expireDate.clone() : null;
        if(password != null){
            try{
                this.password = md5Sum(password);
            }catch(NoSuchAlgorithmException pEx){
                Logger.getLogger(SharedEntity.class.getName()).log(Level.FINEST, null, pEx);
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
        return (expireDate!=null) ? (Date) expireDate.clone() : null;
    }
    public void setExpireDate(Date expireDate) {
        this.expireDate = (expireDate!=null) ? (Date) expireDate.clone() : null;
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
        return (creationDate!=null) ? (Date) creationDate.clone() : null;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public Workspace getWorkspace() {
        return workspace;
    }
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SharedEntity that = (SharedEntity) o;

        if (expireDate != null ? !expireDate.equals(that.expireDate) : that.expireDate != null) {
            return false;
        }
        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) {
            return false;
        }

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
        StringBuilder hexString = new StringBuilder();
        for (byte aDigest : digest) {
            String hex = Integer.toHexString(0xFF & aDigest);
            if (hex.length() == 1) {
                hexString.append("0").append(hex);
            } else {
                hexString.append(hex);
            }
        }
        return hexString.toString();
    }
}
