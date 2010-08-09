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
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import javax.swing.ImageIcon.*;
import com.docdoku.core.util.FileIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScannerDevice implements ScannerListener {

    private Scanner mScanner;
    private ImageWriter mWriter;
    private ActionListener mCallbackAction;

    public ScannerDevice() {
        mScanner = Scanner.getDevice();
        mScanner.addListener(this);
    }

    public void select(String device) throws IOException {
        try {
            mScanner.select(device);
        } catch (ScannerIOException ex) {
            throw new IOException(ex);
        }
    }

    public String[] getDeviceNames() {
        String names[];
        try {
            names = mScanner.getDeviceNames();
        } catch (ScannerIOException ex) {
            names = new String[]{};
        }
        return names;
    }

    public void scan(File pScanFile, ActionListener pCallbackAction) throws Exception {
        mScanner.acquire();
        mCallbackAction = pCallbackAction;
        String extension = FileIO.getExtension(pScanFile);
        mWriter = ImageIO.getImageWritersByFormatName(extension).next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(pScanFile);
        mWriter.setOutput(ios);
        mWriter.prepareWriteSequence(null);
        System.out.println("BUSY: "+mScanner.isBusy());
        
    }

    @Override
    public void update(ScannerIOMetadata.Type type, ScannerIOMetadata metadata) {
        try {
            if (type.equals(ScannerIOMetadata.ACQUIRED)) {
                BufferedImage image = metadata.getImage();
                mWriter.writeToSequence(new IIOImage(image, null, null), null);
            } else if (type.equals(ScannerIOMetadata.NEGOTIATE)) {
                metadata.getDevice().setShowUserInterface(true);
                metadata.getDevice().setShowProgressBar(true);
            } else if (type.equals(ScannerIOMetadata.FILE)) {
                System.out.println("FILE");
            } else if (type.equals(ScannerIOMetadata.INFO)) {
                System.out.println("INFO");
            } else if (type.equals(ScannerIOMetadata.MEMORY)) {
                System.out.println("MEMORY");
            } else if (type.equals(ScannerIOMetadata.STATECHANGE)) {
                System.out.println("STATECHANGE");
                System.out.println(metadata.getStateStr());
                System.out.println("FINISHED : " + metadata.isFinished());
                if (metadata.isFinished()) {
                    mWriter.endWriteSequence();
                    ((ImageOutputStream) mWriter.getOutput()).close();
                    mWriter.dispose();
                    mCallbackAction.actionPerformed(new ActionEvent(this, 0, null));
                    mWriter = null;
                    mCallbackAction = null;
                }

            } else if (type.equals(ScannerIOMetadata.EXCEPTION)) {
                System.out.println("EXCEPTION");
            }
        } catch (IOException ex) {
        }
    }

    @Override
    protected void finalize() throws Throwable {
        //mScanner.
    }
}



