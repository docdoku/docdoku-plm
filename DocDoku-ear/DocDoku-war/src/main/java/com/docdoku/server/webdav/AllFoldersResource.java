package com.docdoku.server.webdav;

import java.util.Date;
import java.util.List;


import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import java.util.ArrayList;

public class AllFoldersResource implements PropFindableResource, CollectionResource{


	
	public AllFoldersResource() {

	}	
	
	@Override
	public Date getCreateDate() {
		// Unknown
		return null;
	}

	@Override
	public Object authenticate(String user, String pwd) {
		// always allow
		return user;
	}

	@Override
	public boolean authorise(Request arg0, Method arg1, Auth arg2) {
		// Always allow
		return true;
	}

	@Override
	public String checkRedirect(Request arg0) {
		// No redirects
		return null;
	}

	@Override
	public Date getModifiedDate() {
		// Unknown
		return null;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getRealm() {
		return "DocDoku REALM";
	}

	@Override
	public String getUniqueId() {
		return null;
	}

	@Override
	public Resource child(String name) {
		return null;
	}

	@Override
	public List<? extends Resource> getChildren() {
		List<Resource> resources=new ArrayList<Resource>();
                Resource res1=new FolderResource("folder1");
                Resource res2=new FolderResource("folder2");
                Resource res3=new FolderResource("folder3");
                resources.add(res1);
                resources.add(res2);
                resources.add(res3);
                return resources;
	}
}
