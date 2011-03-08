/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *s
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

import com.docdoku.client.localization.I18N;
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
import javax.swing.JOptionPane;

public class ScannerDevice implements ScannerListener {

    private final Scanner mScanner;
    private ImageWriter mWriter;
    private ActionListener mCallbackAction;
    private String mCurrentDeviceName;
    private String mCurrentFormat;
    private boolean mScanned;
    private static ScannerDevice sInstance;

    private ScannerDevice() {
        mScanner = Scanner.getDevice();
    }

    public static ScannerDevice getInstance() {
        if (sInstance == null) {
            sInstance = new ScannerDevice();
            sInstance.mScanner.addListener(sInstance);
        }
        return sInstance;
    }

    public String getCurrentDeviceName() {
        return mCurrentDeviceName;
    }

    public String getCurrenFormat() {
        return mCurrentFormat;
    }

    public void select(String device) throws ScannerException {
        try {
            if (!device.equals(mCurrentDeviceName)) {
                mScanner.select(device);
                mCurrentDeviceName = device;
            }
        } catch (ScannerIOException ex) {
            throw new ScannerException(ex);
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

    public void scan(File pScanFile, ActionListener pCallbackAction) throws ScannerException, IOException {
        mScanned = false;
        mCallbackAction = pCallbackAction;

        String extension = FileIO.getExtension(pScanFile);
        mWriter = ImageIO.getImageWritersByFormatName(extension).next();
        mCurrentFormat=extension;
        ImageOutputStream ios = ImageIO.createImageOutputStream(pScanFile);
        mWriter.setOutput(ios);
        mWriter.prepareWriteSequence(null);
        try{
            mScanner.acquire();
        }catch (ScannerIOException ex) {
            throw new ScannerException(ex);
        }
    }

    @Override
    public void update(ScannerIOMetadata.Type type, ScannerIOMetadata metadata) {
        try {
            if (type.equals(ScannerIOMetadata.ACQUIRED)) {
                BufferedImage image = metadata.getImage();
                mWriter.writeToSequence(new IIOImage(image, null, null), null);
                mScanned = true;
            } else if (type.equals(ScannerIOMetadata.NEGOTIATE)) {
                metadata.getDevice().setShowUserInterface(true);
                metadata.getDevice().setShowProgressBar(true);
            } else if (type.equals(ScannerIOMetadata.FILE)) {
            } else if (type.equals(ScannerIOMetadata.INFO)) {
            } else if (type.equals(ScannerIOMetadata.MEMORY)) {
            } else if (type.equals(ScannerIOMetadata.STATECHANGE)) {
                if (metadata.isFinished()) {
                    mWriter.endWriteSequence();
                   if(mScanned){
                       try{
                        ((ImageOutputStream) mWriter.getOutput()).close();
                       }catch(IOException pEx){
                           //has probably already been closed
                       }
                    }
                    mWriter.dispose();
                    mCallbackAction.actionPerformed(new ActionEvent(this, 0, mScanned + ""));
                    mWriter = null;
                    mCallbackAction = null;
                }

            } else if (type.equals(ScannerIOMetadata.EXCEPTION)) {
                showErrorMessage(metadata.getException());
            }
        } catch (IOException ex) {
            showErrorMessage(ex);
        }
    }

    private void showErrorMessage(Exception pEx) {
        String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
        JOptionPane.showMessageDialog(null,
                message, I18N.BUNDLE.getString("Error_title"),
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
