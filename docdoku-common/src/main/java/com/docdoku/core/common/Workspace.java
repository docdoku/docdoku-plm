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

package com.docdoku.core.common;


import javax.persistence.*;
import java.io.Serializable;

/**
 * The context in which documents, workflow models, parts, products, templates and all
 * the other objects reside.  
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since V1.0
 */
@Table(name="WORKSPACE")
@javax.persistence.Entity
@NamedQueries({
        @NamedQuery(name="Workspace.findWorkspacesWhereUserIsActive", query="SELECT w FROM Workspace w WHERE EXISTS (SELECT u.workspace FROM WorkspaceUserMembership u WHERE u.workspace = w AND u.member.login = :userLogin) OR EXISTS (SELECT g FROM WorkspaceUserGroupMembership g WHERE g.workspace = w AND EXISTS (SELECT gr FROM UserGroup gr, User us WHERE us.workspace = gr.workspace AND g.workspace = gr.workspace AND us.login = :userLogin AND us member of gr.users))"),
        @NamedQuery(name="Workspace.findAllWorkspaces", query="SELECT w FROM Workspace w")        
})
public class Workspace implements Serializable, Cloneable {

    @Column(length=100)
    @javax.persistence.Id
    private String id="";
    
    @javax.persistence.ManyToOne(optional=false, fetch=FetchType.EAGER)
    private Account admin;

    @Lob
    private String description;    
    
    private boolean folderLocked;


    public Workspace(String pId, Account pAdmin, String pDescription, boolean pFolderLocked) {
        id = pId;
        admin = pAdmin;
        description = pDescription;
        folderLocked=pFolderLocked;
    }
    public Workspace(String pId) {
        id = pId;
    }
    public Workspace() {
    }
    
    public Account getAdmin() {
        return admin;
    }
    public void setAdmin(Account pAdmin) {
        admin = pAdmin;
    }

    public String getId() {
        return id;
    }
    public void setId(String pId){
        id=pId;
    }
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public boolean isFolderLocked() {
        return folderLocked;
    }
    public void setFolderLocked(boolean folderLocked) {
        this.folderLocked = folderLocked;
    }

    
    @Override
    public String toString() {
        return id;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof Workspace)){
            return false;
        }
        Workspace workspace = (Workspace) pObj;
        return workspace.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    

    @Override
    public Workspace clone() {
        try {
            return (Workspace) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
