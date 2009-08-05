package com.docdoku.gwt.explorer.common;

import java.io.Serializable;

/**
 *
 * @author Florent GARIN
 */
@SuppressWarnings("serial")
public class UserDTO implements Serializable{

    private String workspaceId;
    private String login;
    private String name;
    private String email;

    private WorkspaceMembership membership;

    public UserDTO(){

    }

    public UserDTO(String workspaceId, String login, String name) {
        this.workspaceId=workspaceId;
        this.login=login;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getLogin() {
        return login;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceMembership getMembership() {
        return membership;
    }

    public void setMembership(WorkspaceMembership membership) {
        this.membership = membership;
    }



    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setLogin(String login) {
        this.login = login;
    }

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

}
