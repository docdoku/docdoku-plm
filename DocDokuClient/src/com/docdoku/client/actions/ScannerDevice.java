/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.client.actions;

import java.io.File;
import java.awt.Window.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata.Type;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import javax.swing.ImageIcon.*;
import com.docdoku.client.data.Config;

public class ScannerDevice implements ScannerListener {

    private final Scanner mScanner;
    private static ScannerDevice sInstance;

    private ScannerDevice() {
        mScanner = Scanner.getDevice();
        if (mScanner != null) {
            mScanner.addListener(this);
        }
    }

    public static ScannerDevice getInstance(){
        if(sInstance==null)
            sInstance=new ScannerDevice();

        return sInstance;
    }

    public void select(String device) throws ScannerIOException {
        mScanner.select(device);
    }
    
    public String[] getDeviceNames() {
        String names[];
        try{
            names=mScanner.getDeviceNames();
        }catch(ScannerIOException ex){
            names=new String[]{};
        }
        return names;
    }

    /*
    public void ScannerActive(String Name, String Ext) throws Exception {
    while (!scanner.isBusy()) {
    scanner.acquire();
    writer = (ImageWriter) ImageIO.getImageWritersByFormatName(Ext).next();
    System.out.println(writer.getClass().getName());
    File home = new File(Config.LOCAL_CACHE_SCANN, MainModel.getInstance().getWorkspace() + File.separator + "documents");
    home.mkdirs();
    rsststring = new ResetString();
    tmp = new File(home, rsststring.FormatString(Name, Ext) + "." + rsststring.Extention());
    tmp.deleteOnExit();
    home.deleteOnExit();
    ImageOutputStream ios = ImageIO.createImageOutputStream(tmp);
    writer.setOutput(ios);
    writer.prepareWriteSequence(null);
    writeImage = true;
    Extention = Ext;
    }
    }
     */
    
     /*
    @Override
    public void update(ScannerIOMetadata.Type type, ScannerIOMetadata metadata) {

        if (type.equals(ScannerIOMetadata.ACQUIRED)) {
            try {
                BufferedImage image = metadata.getImage();
                if (Extention == "pdf") {
                    ImageIO.write(image, Extention, tmp);
                }

                System.out.println("Image!");
                if (writeImage) {
                    writer.writeToSequence(new IIOImage(image, null, null), null);
                    scanned = true;
                }
            } catch (IOException ex) {
                Logger.getLogger(ScannerDevice.class.getName()).log(Level.SEVERE, null, ex);
            }


        } else if (type.equals(ScannerIOMetadata.NEGOTIATE)) {
            ScannerDevice device = metadata.getDevice();
            try {
                device.setShowUserInterface(true);
                device.setShowProgressBar(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (type.equals(ScannerIOMetadata.STATECHANGE)) {
            System.out.println(metadata.getStateStr());
            if (metadata.isFinished()) {
                try {
                    writer.endWriteSequence();
                    ((ImageOutputStream) writer.getOutput()).close();
                    EditFilesPanel.closeapp();
                } catch (IOException ex) {
                    Logger.getLogger(ScannerDevice.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else if (type.equals(ScannerIOMetadata.EXCEPTION)) {
            metadata.getException().printStackTrace();
        }
    }
    */

    @Override
    protected void finalize() throws Throwable {
        //mScanner.
    }

    @Override
    public void update(Type type, ScannerIOMetadata siom) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}




