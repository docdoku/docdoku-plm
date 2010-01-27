package com.docdoku.client.ui.common;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.FileDataSource;
import javax.swing.ProgressMonitorInputStream;

public class ProgressMonitorFileDataSource extends FileDataSource {

	private String transfertMessage;
	private Component parent;
        
        public ProgressMonitorFileDataSource(Component parent, File file, String transfertMessage) {
		super(file);
		this.transfertMessage=transfertMessage;
                this.parent=parent;
	}

        
	public ProgressMonitorFileDataSource(File file, String transfertMessage) {
		super(file);
		this.transfertMessage=transfertMessage;
	}

	public ProgressMonitorFileDataSource(String name, String transfertMessage) {
		super(name);
		this.transfertMessage=transfertMessage;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ProgressMonitorInputStream(parent,transfertMessage,super.getInputStream());
	}
	
	

}
